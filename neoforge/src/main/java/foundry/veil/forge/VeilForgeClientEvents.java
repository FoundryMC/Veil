package foundry.veil.forge;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import foundry.veil.Veil;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.VeilRenderer;
import foundry.veil.api.client.render.dynamicbuffer.DynamicBufferType;
import foundry.veil.api.client.util.Easing;
import foundry.veil.api.quasar.data.QuasarParticles;
import foundry.veil.api.quasar.particle.ParticleEmitter;
import foundry.veil.api.quasar.particle.ParticleSystemManager;
import foundry.veil.forge.event.ForgeFreeNativeResourcesEvent;
import foundry.veil.impl.ClientEnumArgument;
import foundry.veil.impl.client.VeilClientSchedulerImpl;
import foundry.veil.impl.client.imgui.VeilImGuiCompat;
import foundry.veil.impl.network.VeilClientServerFlags;
import foundry.veil.api.screenshake.type.GlobalScreenShake;
import foundry.veil.api.screenshake.type.LocalScreenShake;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import org.jetbrains.annotations.ApiStatus;

import java.util.Locale;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

@ApiStatus.Internal
@EventBusSubscriber(modid = Veil.MODID, value = Dist.CLIENT)
public class VeilForgeClientEvents {

    @SubscribeEvent
    public static void clientDisconnected(ClientPlayerNetworkEvent.LoggingOut event) {
        VeilRenderSystem.renderer().getLightRenderer().free();
        VeilClientServerFlags.onDisconnect();
    }

    @SubscribeEvent
    public static void keyPressed(InputEvent.Key event) {
        if (Veil.IMGUIMC && event.getAction() == GLFW_PRESS && VeilImGuiCompat.EDITOR_KEY.matches(event.getKey(), event.getScanCode())) {
            VeilRenderSystem.renderer().getEditorManager().toggle();
        }
    }

    @SubscribeEvent
    public static void mousePressed(InputEvent.MouseButton.Pre event) {
        if (Veil.IMGUIMC && event.getAction() == GLFW_PRESS && VeilImGuiCompat.EDITOR_KEY.matchesMouse(event.getButton())) {
            VeilRenderSystem.renderer().getEditorManager().toggle();
        }
    }

    @SubscribeEvent
    public static void registerClientCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        LiteralArgumentBuilder<CommandSourceStack> quasarBuilder = Commands.literal("quasar");
        quasarBuilder.then(Commands.argument("emitter", ResourceLocationArgument.id()).suggests(QuasarParticles.emitterSuggestionProvider()).then(Commands.argument("position", Vec3Argument.vec3()).executes(ctx -> {
            ResourceLocation id = ResourceLocationArgument.getId(ctx, "emitter");

            CommandSourceStack source = ctx.getSource();
            ParticleSystemManager particleManager = VeilRenderSystem.renderer().getParticleManager();
            ParticleEmitter instance = particleManager.createEmitter(id);
            if (instance == null) {
                source.sendFailure(Component.literal("Unknown emitter: " + id));
                return 0;
            }

            WorldCoordinates coordinates = ctx.getArgument("position", WorldCoordinates.class);
            Vec3 pos = coordinates.getPosition(source);
            instance.setPosition(pos.x, pos.y, pos.z);
            particleManager.addParticleSystem(instance);
            source.sendSuccess(() -> Component.literal("Spawned " + id), true);
            return 1;
        })));
        dispatcher.register(quasarBuilder);

        if (Veil.platform().isDevelopmentEnvironment()) {
            ResourceLocation bufferId = Veil.veilPath("forced");
            LiteralArgumentBuilder<CommandSourceStack> debugBuilder = Commands.literal("veilc");
            debugBuilder.then(Commands.literal("buffers")
                    .then(Commands.literal("enable")
                            .then(Commands.argument("buffer", ClientEnumArgument.enumArgument(DynamicBufferType.class)).executes(ctx -> {
                                DynamicBufferType value = ctx.getArgument("buffer", DynamicBufferType.class);
                                VeilRenderer renderer = VeilRenderSystem.renderer();
                                renderer.enableBuffers(bufferId, value);
                                ctx.getSource().sendSuccess(() -> Component.translatable("commands.veil.buffers.enable", value.name().toLowerCase(Locale.ROOT)), true);
                                return Command.SINGLE_SUCCESS;
                            }))
                            .then(Commands.literal("all").executes(ctx -> {
                                DynamicBufferType[] values = DynamicBufferType.values();
                                VeilRenderSystem.renderer().enableBuffers(bufferId, values);
                                ctx.getSource().sendSuccess(() -> Component.translatable("commands.veil.buffers.enable.all"), true);
                                return values.length;
                            }))
                    )
                    .then(Commands.literal("disable")
                            .then(Commands.argument("buffer", ClientEnumArgument.enumArgument(DynamicBufferType.class)).executes(ctx -> {
                                DynamicBufferType value = ctx.getArgument("buffer", DynamicBufferType.class);
                                VeilRenderSystem.renderer().disableBuffers(bufferId, value);
                                ctx.getSource().sendSuccess(() -> Component.translatable("commands.veil.buffers.disable", value.name().toLowerCase(Locale.ROOT)), true);
                                return Command.SINGLE_SUCCESS;
                            }))
                            .then(Commands.literal("all").executes(ctx -> {
                                DynamicBufferType[] values = DynamicBufferType.values();
                                VeilRenderSystem.renderer().disableBuffers(bufferId, values);
                                ctx.getSource().sendSuccess(() -> Component.translatable("commands.veil.buffers.disable.all"), true);
                                return values.length;
                            }))
                    ));
            dispatcher.register(debugBuilder);

            LiteralArgumentBuilder<CommandSourceStack> shakeBuilder = Commands.literal("shake");
            shakeBuilder
                    .then(Commands.literal("local")
                            .then(Commands.argument("expression", StringArgumentType.string()).then(Commands.argument("position", Vec3Argument.vec3()).then(Commands.argument("length", IntegerArgumentType.integer(0)).then(Commands.argument("radius", FloatArgumentType.floatArg(0))
                                    .executes(ctx -> {
                                        try {
                                            VeilRenderer renderer = VeilRenderSystem.renderer();
                                            renderer.getScreenShakeManager().addScreenShake(new LocalScreenShake(StringArgumentType.getString(ctx, "expression"), Vec3Argument.getVec3(ctx, "position"), IntegerArgumentType.getInteger(ctx, "length"), FloatArgumentType.getFloat(ctx, "radius"), Easing.LINEAR));

                                            return Command.SINGLE_SUCCESS;
                                        } catch (Exception e) {
                                            return 0;
                                        }
                                    })
                            ))))
                    )
                    .then(Commands.literal("global")
                            .then(Commands.argument("expression", StringArgumentType.string()).then(Commands.argument("length", IntegerArgumentType.integer(0))
                            .executes(ctx -> {
                                try {
                                    VeilRenderer renderer = VeilRenderSystem.renderer();
                                    renderer.getScreenShakeManager().addScreenShake(new GlobalScreenShake(StringArgumentType.getString(ctx, "expression"), ctx.getArgument("length", Integer.class)));

                                    return Command.SINGLE_SUCCESS;
                                } catch (Exception e) {
                                    return 0;
                                }
                            })))
                    );
            dispatcher.register(shakeBuilder);
        }
    }

    @SubscribeEvent
    public static void clientTick(ClientTickEvent.Pre event) {
        VeilClientSchedulerImpl.tick();
    }

    @SubscribeEvent
    public static void clientStopping(ForgeFreeNativeResourcesEvent event) {
        VeilClientSchedulerImpl.shutdown();
    }
}
