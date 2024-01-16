package foundry.veil.quasar.emitters.modules.particle.render.color;

import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.emitters.modules.ModuleType;
import foundry.veil.quasar.emitters.modules.particle.render.RenderData;
import foundry.veil.quasar.emitters.modules.particle.render.RenderParticleModule;
import foundry.veil.quasar.util.CodecUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector4f;
import org.joml.Vector4fc;

public class ColorParticleModule implements RenderParticleModule {
    public static final Codec<ColorParticleModule> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    CodecUtil.VECTOR4F_CODEC.fieldOf("color").forGetter(ColorParticleModule::getColor)
            ).apply(instance, ColorParticleModule::new));
    Vector4fc color;
    public Vector4fc getColor() {
        return color;
    }

    public ColorParticleModule(Vector4fc color) {
        this.color = color;
    }
    @Override
    public void apply(QuasarParticle particle, float partialTicks, RenderData data) {
        data.setRGBA(color.x(), color.y(), color.z(), color.w());
    }

    @NotNull
    @Override
    public ModuleType<?> getType() {
        return ModuleType.COLOR;
    }

}
