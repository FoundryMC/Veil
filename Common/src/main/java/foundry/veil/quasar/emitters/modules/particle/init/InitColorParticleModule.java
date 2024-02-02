package foundry.veil.quasar.emitters.modules.particle.init;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.quasar.client.particle.QuasarVanillaParticle;
import foundry.veil.quasar.data.module.ModuleType;
import foundry.veil.quasar.util.CodecUtil;
import org.joml.Vector4fc;

public record InitColorParticleModule(Vector4fc color) implements InitParticleModule {

    public static final Codec<InitColorParticleModule> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    CodecUtil.VECTOR4F_CODEC.fieldOf("color").forGetter(InitColorParticleModule::color)
            ).apply(instance, InitColorParticleModule::new));

    @Override
    public void run(QuasarVanillaParticle particle) {
        particle.setColor(this.color);
    }

    @Override
    public ModuleType<?> getType() {
        return ModuleType.INIT_COLOR;
    }

}
