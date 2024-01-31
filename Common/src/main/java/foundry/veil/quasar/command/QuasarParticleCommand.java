package foundry.veil.quasar.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import foundry.veil.quasar.data.ParticleEmitterData;
import foundry.veil.quasar.emitters.ParticleEmitter;
import foundry.veil.quasar.emitters.ParticleEmitterRegistry;
import foundry.veil.quasar.emitters.ParticleSystemManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class QuasarParticleCommand {

    private static final SuggestionProvider<CommandSourceStack> EMITTER_SUGGlESTION_PROVIDER = (unused, builder) -> SharedSuggestionProvider.suggestResource(ParticleEmitterRegistry.getEmitters().stream(), builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("quasar").then(Commands.argument("emitter", ResourceLocationArgument.id()).suggests(EMITTER_SUGGlESTION_PROVIDER).then(Commands.argument("position", Vec3Argument.vec3()).executes(ctx -> {
            ResourceLocation id = ResourceLocationArgument.getId(ctx, "emitter");
            ParticleEmitterData emitter = ParticleEmitterRegistry.getEmitter(id);
            if (emitter == null) {
                ctx.getSource().sendFailure(Component.literal("Unknown emitter: " + id));
                return 0;
            }

            ParticleEmitter instance = new ParticleEmitter(ctx.getSource().getLevel(), emitter);
            instance.setPosition(Vec3Argument.getVec3(ctx, "position"));
            ParticleSystemManager.getInstance().addParticleSystem(instance);
            return 1;
        }))));
    }
}
