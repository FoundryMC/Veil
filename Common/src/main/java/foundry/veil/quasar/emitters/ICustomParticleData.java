package foundry.veil.quasar.emitters;

import com.mojang.serialization.Codec;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleOptions.Deserializer;
import net.minecraft.core.particles.ParticleType;

public interface ICustomParticleData<T extends ParticleOptions> {

    Deserializer<T> getDeserializer();

    Codec<T> getCodec(ParticleType<T> type);

    public default ParticleType<T> createType() {
        return new ParticleType<T>(false, getDeserializer()) {

            @Override
            public Codec<T> codec() {
                return ICustomParticleData.this.getCodec(this);
            }
        };
    }


}