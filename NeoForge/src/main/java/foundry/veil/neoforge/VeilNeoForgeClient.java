package foundry.veil.neoforge;

import foundry.veil.Veil;
import foundry.veil.VeilClient;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.VeilVanillaShaders;
import foundry.veil.api.client.render.deferred.VeilDeferredRenderer;
import foundry.veil.impl.client.render.VeilUITooltipRenderer;
import foundry.veil.neoforge.event.NeoForgeVeilRegisterFixedBuffersEvent;
import foundry.veil.neoforge.event.NeoForgeVeilRendererEvent;
import foundry.veil.util.VeilJsonListeners;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiOverlaysEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.gui.overlay.VanillaGuiOverlay;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import org.jetbrains.annotations.ApiStatus;

import java.nio.file.Path;

@ApiStatus.Internal
public class VeilNeoForgeClient {

    public static void init(IEventBus modEventBus) {
        VeilClient.init();

        modEventBus.addListener(VeilNeoForgeClient::registerKeys);
        modEventBus.addListener(VeilNeoForgeClient::registerGuiOverlays);
        modEventBus.addListener(VeilNeoForgeClient::registerListeners);
        modEventBus.addListener(VeilNeoForgeClient::registerShaders);
        modEventBus.addListener(VeilNeoForgeClient::addPackFinders);
    }

    private static void registerListeners(RegisterClientReloadListenersEvent event) {
        VeilClient.initRenderer();
        VeilJsonListeners.registerListeners((type, id, listener) -> event.registerReloadListener(listener));
        NeoForge.EVENT_BUS.post(new NeoForgeVeilRendererEvent(VeilRenderSystem.renderer()));
        NeoForge.EVENT_BUS.post(new NeoForgeVeilRegisterFixedBuffersEvent(NeoForgeRenderTypeStageHandler::register));
    }

    private static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(VeilClient.EDITOR_KEY);
    }

    private static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), Veil.veilPath("uitooltip"), VeilUITooltipRenderer::renderOverlay);
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
