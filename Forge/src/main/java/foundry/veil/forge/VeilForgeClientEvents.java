package foundry.veil.forge;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import foundry.veil.Veil;
import foundry.veil.VeilClient;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.quasar.data.QuasarParticles;
import foundry.veil.quasar.emitters.ParticleEmitter;
import foundry.veil.quasar.emitters.ParticleSystemManager;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
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
    private static final SuggestionProvider<CommandSourceStack> EMITTER_SUGGESTION_PROVIDER = (unused, builder) -> {
        return SharedSuggestionProvider.suggestResource(QuasarParticles.registryAccess().registryOrThrow(QuasarParticles.EMITTER).keySet(), builder);
    };
    @SubscribeEvent
    public static void registerClientCommands(RegisterClientCommandsEvent event){
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("quasar");
        builder.then(Commands.argument("emitter", ResourceLocationArgument.id()).suggests(EMITTER_SUGGESTION_PROVIDER).then(Commands.argument("position", Vec3Argument.vec3()).executes(context1 -> {
            ResourceLocation id = ResourceLocationArgument.getId(context1, "emitter");

            ParticleEmitter emitter = ParticleSystemManager.getInstance().createEmitter(context1.getSource().getUnsidedLevel(), id);
            if(emitter == null) {
                context1.getSource().sendFailure(Component.literal("Unknown emitter: " + id));
                return 0;
            }
            emitter.setPosition(Vec3Argument.getVec3(context1, "position"));
            ParticleSystemManager.getInstance().addParticleSystem(emitter);
            return 1;
        })));
        event.getDispatcher().register(builder);
    }

    @SubscribeEvent
    public void tick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.START && event.side.isClient()) {
            ParticleSystemManager.getInstance().tick();
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
