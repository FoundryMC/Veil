package foundry.veil.quasar.command;

import foundry.veil.quasar.emitters.ParticleEmitter;
import foundry.veil.quasar.emitters.ParticleEmitterRegistry;
import foundry.veil.quasar.emitters.ParticleSystemManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;

public class QuasarParticleCommand implements Command<CommandSourceStack> {
    public static QuasarParticleCommand CMD = new QuasarParticleCommand();
    private static final SuggestionProvider<CommandSourceStack> EMITTER_SUGGESTION_PROVIDER = (unused, builder) -> {
        return SharedSuggestionProvider.suggestResource(ParticleEmitterRegistry.getEmitterNames(), builder);
    };

    public LiteralArgumentBuilder<CommandSourceStack> register() {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("quasar");
        builder.then(Commands.argument("emitter", ResourceLocationArgument.id()).suggests(EMITTER_SUGGESTION_PROVIDER).then(Commands.argument("position", Vec3Argument.vec3()).executes(context1 -> {
            ParticleEmitter emitter = ParticleEmitterRegistry.getEmitter(ResourceLocationArgument.getId(context1, "emitter")).instance();
            emitter.setPosition(Vec3Argument.getVec3(context1, "position"));
            emitter.setLevel(Minecraft.getInstance().level);
            ParticleSystemManager.getInstance().addParticleSystem(emitter);
            return 0;
        })));
        return builder;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return 0;
    }
}
