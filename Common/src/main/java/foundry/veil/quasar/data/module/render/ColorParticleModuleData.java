package foundry.veil.quasar.data.module.render;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.api.molang.MolangExpressionCodec;
import foundry.veil.quasar.client.particle.ParticleModuleSet;
import foundry.veil.quasar.data.module.ModuleType;
import foundry.veil.quasar.data.module.ParticleModuleData;
import foundry.veil.quasar.emitters.modules.particle.InitParticleModule;
import foundry.veil.quasar.emitters.modules.particle.RenderParticleModule;
import foundry.veil.quasar.util.ColorGradient;
import gg.moonflower.molangcompiler.api.MolangExpression;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record ColorParticleModuleData(ColorGradient gradient,
                                      @Nullable MolangExpression interpolant) implements ParticleModuleData {

    public static final Codec<ColorParticleModuleData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ColorGradient.CODEC.fieldOf("gradient").forGetter(ColorParticleModuleData::gradient),
            MolangExpressionCodec.CODEC.optionalFieldOf("interpolant").forGetter(data -> Optional.ofNullable(data.interpolant()))
    ).apply(instance, (gradient, interpolant) -> new ColorParticleModuleData(gradient, interpolant.orElse(null))));


    @Override
    public void addModules(ParticleModuleSet.Builder builder) {
        if (this.gradient.isConstant() || this.interpolant == null) {
            builder.addModule((InitParticleModule) particle -> particle.getRenderData().setColor(this.gradient.getColor(0.0F)));
        } else {
            builder.addModule((RenderParticleModule) (particle, partialTicks) -> {
                float percentage = particle.getEnvironment().safeResolve(this.interpolant);
                particle.getRenderData().setColor(this.gradient.getColor(percentage));
            });
        }
    }

    @Override
    public ModuleType<?> getType() {
        return ModuleType.INIT_COLOR;
    }
}
