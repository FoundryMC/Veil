package foundry.veil.quasar.emitters.modules.particle.render.color;

import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.emitters.modules.ModuleType;
import foundry.veil.quasar.emitters.modules.particle.render.RenderData;
import foundry.veil.quasar.util.ColorGradient;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector4f;

public class ColorOverTimeModule extends ColorModule {
    public static final Codec<ColorOverTimeModule> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ColorGradient.CODEC.fieldOf("gradient").forGetter(ColorOverTimeModule::getGradient)
            ).apply(instance, ColorOverTimeModule::new));
    ColorGradient gradient;
    public ColorOverTimeModule(ColorGradient gradient) {
        super(gradient.getColor(0));
        this.gradient = gradient;
    }

    public ColorGradient getGradient() {
        return gradient;
    }

    @Override
    public void apply(QuasarParticle particle, float partialTicks, RenderData data) {
        double life = (double) particle.getAge() / particle.getLifetime();
        Vector4f col = gradient.getColor((float) life);
        data.setRGBA(col.x(), col.y(), col.z(), col.w());
    }

    @Override
    public @NotNull ModuleType<?> getType() {
        return ModuleType.COLOR_OVER_LIFETIME;
    }
}
