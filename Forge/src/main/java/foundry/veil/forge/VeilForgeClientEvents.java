package foundry.veil.forge;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import foundry.veil.Veil;
import foundry.veil.VeilClient;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.quasar.client.particle.ParticleEmitter;
import foundry.veil.quasar.client.particle.ParticleSystemManager;
import foundry.veil.quasar.data.QuasarParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.ApiStatus;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

@ApiStatus.Internal
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = Veil.MODID, value = Dist.CLIENT)
public class VeilForgeClientEvents {

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            VeilClient.tickClient(Minecraft.getInstance().getFrameTime());
        }
    }

    @SubscribeEvent
    public static void keyPressed(InputEvent.Key event) {
        if (event.getAction() == GLFW_PRESS && VeilClient.EDITOR_KEY.matches(event.getKey(), event.getScanCode())) {
            VeilRenderSystem.renderer().getEditorManager().toggle();
        }
    }

    @SubscribeEvent
    public static void registerClientCommands(RegisterClientCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("quasar");
        builder.then(Commands.argument("emitter", ResourceLocationArgument.id()).suggests(QuasarParticles.emitterSuggestionProvider()).then(Commands.argument("position", Vec3Argument.vec3()).executes(ctx -> {
            ResourceLocation id = ResourceLocationArgument.getId(ctx, "emitter");

            CommandSourceStack source = ctx.getSource();
            ParticleSystemManager particleManager = VeilRenderSystem.renderer().getParticleManager();
            ParticleEmitter instance = particleManager.createEmitter(id);
            if (instance == null) {
                source.sendFailure(Component.literal("Unknown emitter: " + id));
                return 0;
            }

            WorldCoordinates pos = ctx.getArgument("position", WorldCoordinates.class);
            instance.setPosition(pos.getPosition(source));
            particleManager.addParticleSystem(instance);
            source.sendSuccess(() -> Component.literal("Spawned " + id), true);
            return 1;
        })));
        event.getDispatcher().register(builder);
    }

    @SubscribeEvent
    public void tick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.START && event.side.isClient()) {
            VeilRenderSystem.renderer().getParticleManager().tick();
        }
    }

    @SubscribeEvent
    public static void mousePressed(InputEvent.MouseButton event) {
        if (event.getAction() == GLFW_PRESS && VeilClient.EDITOR_KEY.matchesMouse(event.getButton())) {
            VeilRenderSystem.renderer().getEditorManager().toggle();
        }
    }

    @SubscribeEvent
    public static void leaveGame(ClientPlayerNetworkEvent.LoggingOut event) {
        VeilRenderSystem.renderer().getDeferredRenderer().reset();
    }
}
