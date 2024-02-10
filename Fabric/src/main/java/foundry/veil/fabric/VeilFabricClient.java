package foundry.veil.fabric;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import foundry.veil.Veil;
import foundry.veil.VeilClient;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.VeilVanillaShaders;
import foundry.veil.api.client.render.deferred.VeilDeferredRenderer;
import foundry.veil.fabric.util.FabricReloadListener;
import foundry.veil.impl.client.render.VeilUITooltipRenderer;
import foundry.veil.quasar.client.particle.ParticleEmitter;
import foundry.veil.quasar.client.particle.ParticleSystemManager;
import foundry.veil.quasar.data.QuasarParticles;
import foundry.veil.util.VeilJsonListeners;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class VeilFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        VeilClient.init();
        HudRenderCallback.EVENT.register((matrices, tickDelta) -> {
            Minecraft client = Minecraft.getInstance();
            VeilUITooltipRenderer.renderOverlay(client.gui, matrices, tickDelta, client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight());
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> VeilClient.tickClient(client.getFrameTime()));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(() -> {
            VeilRenderSystem.renderer().getDeferredRenderer().reset();
        }));

        KeyBindingHelper.registerKeyBinding(VeilClient.EDITOR_KEY);

        // Register test resource pack
        FabricLoader loader = FabricLoader.getInstance();
        ModContainer container = loader.getModContainer(Veil.MODID).orElseThrow();
        if (Veil.DEBUG && loader.isDevelopmentEnvironment()) {
            ResourceManagerHelper.registerBuiltinResourcePack(Veil.veilPath("test_shaders"), container, ResourcePackActivationType.NORMAL);
            ResourceManagerHelper.registerBuiltinResourcePack(Veil.veilPath("test_particles"), container, ResourcePackActivationType.NORMAL);
        }
        ResourceManagerHelper.registerBuiltinResourcePack(VeilDeferredRenderer.PACK_ID, container, ResourcePackActivationType.DEFAULT_ENABLED);

        CoreShaderRegistrationCallback.EVENT.register(context -> VeilVanillaShaders.registerShaders(context::register));
        VeilJsonListeners.registerListeners((type, id, listener) -> ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new FabricReloadListener(Veil.veilPath(id), listener)));
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            LiteralArgumentBuilder<FabricClientCommandSource> builder = LiteralArgumentBuilder.literal("quasar");
            builder.then(ClientCommandManager.argument("emitter", ResourceLocationArgument.id()).suggests(QuasarParticles.emitterSuggestionProvider()).then(ClientCommandManager.argument("position", Vec3Argument.vec3()).executes(ctx -> {
                ResourceLocation id = ctx.getArgument("emitter", ResourceLocation.class);

                FabricClientCommandSource source = ctx.getSource();
                ParticleSystemManager particleManager = VeilRenderSystem.renderer().getParticleManager();
                ParticleEmitter instance = particleManager.createEmitter(id);
                if (instance == null) {
                    source.sendError(Component.literal("Unknown emitter: " + id));
                    return 0;
                }

                WorldCoordinates pos = ctx.getArgument("position", WorldCoordinates.class);
                instance.setPosition(pos.getPosition(source.getEntity().createCommandSourceStack()));
                particleManager.addParticleSystem(instance);
                source.sendFeedback(Component.literal("Spawned " + id));
                return 1;
            })));
            dispatcher.register(builder);
        });
        ClientTickEvents.START_WORLD_TICK.register(client -> VeilRenderSystem.renderer().getParticleManager().tick());
    }
}
