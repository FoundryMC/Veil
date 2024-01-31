package foundry.veil.quasar.emitters.modules.particle.render.color;

import foundry.veil.quasar.client.particle.QuasarVanillaParticle;
import foundry.veil.quasar.emitters.modules.ModuleType;
import foundry.veil.quasar.emitters.modules.particle.render.RenderData;
import foundry.veil.quasar.util.ColorGradient;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector4f;

public class ColorOverTimeParticleModule extends ColorParticleModule {
    public static final Codec<ColorOverTimeParticleModule> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ColorGradient.CODEC.fieldOf("gradient").forGetter(ColorOverTimeParticleModule::getGradient)
            ).apply(instance, ColorOverTimeParticleModule::new));
    ColorGradient gradient;
    public ColorOverTimeParticleModule(ColorGradient gradient) {
        super(gradient.getColor(0));
        this.gradient = gradient;
    }

    public ColorGradient getGradient() {
        return gradient;
    }

    @Override
    public void apply(QuasarVanillaParticle particle, float partialTicks, RenderData data) {
        double life = (double) particle.getAge() / particle.getLifetime();
        Vector4f col = gradient.getColor((float) life);
        data.setColor(col.x(), col.y(), col.z(), col.w());
    }

    @Override
    public @NotNull ModuleType<?> getType() {
        return ModuleType.COLOR_OVER_LIFETIME;
    }
}
