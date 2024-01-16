package foundry.veil.fabric;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import foundry.veil.Veil;
import foundry.veil.VeilClient;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.VeilVanillaShaders;
import foundry.veil.api.client.render.deferred.VeilDeferredRenderer;
import foundry.veil.fabric.util.FabricReloadListener;
import foundry.veil.impl.client.render.VeilUITooltipRenderer;
import foundry.veil.platform.registry.ParticleTypeRegistry;
import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.command.QuasarParticleCommand;
import foundry.veil.quasar.emitters.ParticleEmitter;
import foundry.veil.quasar.emitters.ParticleEmitterRegistry;
import foundry.veil.quasar.emitters.ParticleSystemManager;
import foundry.veil.util.VeilJsonListeners;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;


@ApiStatus.Internal
public class VeilFabricClient implements ClientModInitializer {

    public static final SuggestionProvider<FabricClientCommandSource> EMITTER_SUGGESTION_PROVIDER = (unused, builder) -> {
        return SharedSuggestionProvider.suggestResource(ParticleEmitterRegistry.getEmitterNames(), builder);
    };
    @Override
    public void onInitializeClient() {
        VeilClient.init();
        HudRenderCallback.EVENT.register((matrices, tickDelta) -> {
            Minecraft client = Minecraft.getInstance();
            VeilUITooltipRenderer.renderOverlay(client.gui, matrices, tickDelta, client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight());
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> VeilClient.tickClient(client.getFrameTime()));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(() -> VeilRenderSystem.renderer().getDeferredRenderer().reset()));

        KeyBindingHelper.registerKeyBinding(VeilClient.EDITOR_KEY);

        // Register test resource pack
        FabricLoader loader = FabricLoader.getInstance();
        if (Veil.DEBUG && loader.isDevelopmentEnvironment()) {
            ResourceManagerHelper.registerBuiltinResourcePack(Veil.veilPath("test_shaders"), loader.getModContainer(Veil.MODID).orElseThrow(), ResourcePackActivationType.NORMAL);
        }
        ResourceManagerHelper.registerBuiltinResourcePack(VeilDeferredRenderer.PACK_ID, loader.getModContainer(Veil.MODID).orElseThrow(), ResourcePackActivationType.DEFAULT_ENABLED);

        CoreShaderRegistrationCallback.EVENT.register(context -> VeilVanillaShaders.registerShaders(context::register));
        VeilJsonListeners.registerListeners((type, id, listener) -> ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new FabricReloadListener(Veil.veilPath(id), listener)));
        ParticleFactoryRegistry.getInstance().register(ParticleTypeRegistry.QUASAR_BASE.get(), (it) -> new QuasarParticle.Factory());
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            LiteralArgumentBuilder<FabricClientCommandSource> builder = LiteralArgumentBuilder.literal("quasar");
            builder.then(ClientCommandManager.argument("emitter", ResourceLocationArgument.id()).suggests(EMITTER_SUGGESTION_PROVIDER).then(ClientCommandManager.argument("position", Vec3Argument.vec3()).executes(context1 -> {
                ParticleEmitter emitter = ParticleEmitterRegistry.getEmitter(context1.getArgument("emitter", ResourceLocation.class)).instance();
                emitter.setPosition(Minecraft.getInstance().player.position());
                emitter.setLevel(Minecraft.getInstance().level);
                ParticleSystemManager.getInstance().addParticleSystem(emitter);
                return 1;
            })));
            dispatcher.register(builder);
        });
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            ParticleSystemManager.getInstance().tick();
        });
    }
}
