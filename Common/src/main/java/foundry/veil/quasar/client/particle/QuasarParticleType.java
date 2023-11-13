package foundry.veil.quasar.client.particle;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;

public class QuasarParticleType extends ParticleType<QuasarParticleData> {
    public QuasarParticleType(boolean pOverrideLimiter, ParticleOptions.Deserializer<QuasarParticleData> pDeserializer) {
        super(pOverrideLimiter, pDeserializer);
    }

    public QuasarParticleType(){
        super(false, QuasarParticleData.DESERIALIZER);
    }

    @Override
    public Codec<QuasarParticleData> codec() {
        return QuasarParticleData.CODEC;
    }
}
