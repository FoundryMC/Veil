package foundry.veil.forge;

import foundry.veil.Veil;
import foundry.veil.VeilClient;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.VeilVanillaShaders;
import foundry.veil.api.client.render.deferred.VeilDeferredRenderer;
import foundry.veil.forge.event.ForgeVeilRegisterFixedBuffersEvent;
import foundry.veil.forge.event.ForgeVeilRendererEvent;
import foundry.veil.impl.client.render.VeilUITooltipRenderer;
import foundry.veil.util.VeilJsonListeners;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import org.jetbrains.annotations.ApiStatus;

import java.nio.file.Path;

@ApiStatus.Internal
public class VeilForgeClient {

    public static void init() {
        VeilClient.init();

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(VeilForgeClient::registerKeys);
        modEventBus.addListener(VeilForgeClient::registerGuiOverlays);
        modEventBus.addListener(VeilForgeClient::registerListeners);
        modEventBus.addListener(VeilForgeClient::registerShaders);
        modEventBus.addListener(VeilForgeClient::addPackFinders);
    }

    private static void registerListeners(RegisterClientReloadListenersEvent event) {
        VeilClient.initRenderer();
        VeilJsonListeners.registerListeners((type, id, listener) -> event.registerReloadListener(listener));
        MinecraftForge.EVENT_BUS.post(new ForgeVeilRendererEvent(VeilRenderSystem.renderer()));
        MinecraftForge.EVENT_BUS.post(new ForgeVeilRegisterFixedBuffersEvent(ForgeRenderTypeStageHandler::register));
    }

    private static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(VeilClient.EDITOR_KEY);
    }

    private static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "uitooltip", VeilUITooltipRenderer::renderOverlay);
    }

    private static void registerShaders(RegisterShadersEvent event) {
        try {
            VeilVanillaShaders.registerShaders((id, vertexFormat, loadCallback) -> event.registerShader(new ShaderInstance(event.getResourceProvider(), id, vertexFormat), loadCallback));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void addPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {

            // Register test resource pack
            if (Veil.DEBUG && !FMLLoader.isProduction()) {
                registerBuiltinPack(event, Veil.veilPath("test_shaders"));
                registerBuiltinPack(event, Veil.veilPath("test_particles"));
            }

            // TODO make this pack enabled by default
            registerBuiltinPack(event, VeilDeferredRenderer.PACK_ID);
        }
    }

    private static void registerBuiltinPack(AddPackFindersEvent event, ResourceLocation id) {
        Path resourcePath = ModList.get().getModFileById(Veil.MODID).getFile().findResource("resourcepacks/" + id.getPath());
        Pack pack = Pack.readMetaAndCreate(id.toString(), Component.literal(id.getNamespace() + "/" + id.getPath()), false,
                new Pack.ResourcesSupplier() {
                    @Override
                    public PackResources openPrimary(String s) {
                        return new PathPackResources(s, resourcePath, false);
                    }

                    @Override
                    public PackResources openFull(String s, Pack.Info info) {
                        return new PathPackResources(s, resourcePath, false);
                    }
                }, PackType.CLIENT_RESOURCES, Pack.Position.BOTTOM, PackSource.BUILT_IN);
        event.addRepositorySource(packConsumer -> packConsumer.accept(pack));
    }
}
