package foundry.veil.api.quasar.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.api.util.CodecUtil;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public record ParticleSettings(float particleSpeed,
                               float particleSize,
                               float particleSizeVariation,
                               int particleLifetime,
                               float particleLifetimeVariation,
                               Vector3fc initialDirection,
                               boolean randomInitialDirection,
                               Vector3fc initialRotation,
                               boolean randomInitialRotation,
                               boolean randomSpeed,
                               boolean randomSize,
                               boolean randomLifetime) {

    public static final Codec<ParticleSettings> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("particle_speed").forGetter(ParticleSettings::particleSpeed),
            Codec.FLOAT.fieldOf("base_particle_size").forGetter(ParticleSettings::particleSize),
            Codec.FLOAT.optionalFieldOf("particle_size_variation", 0f).forGetter(ParticleSettings::particleSizeVariation),
            Codec.INT.fieldOf("particle_lifetime").forGetter(ParticleSettings::particleLifetime),
            Codec.FLOAT.optionalFieldOf("particle_lifetime_variation", 0f).forGetter(ParticleSettings::particleLifetimeVariation),
            CodecUtil.VECTOR3FC_CODEC.optionalFieldOf("initial_direction", new Vector3f(1)).forGetter(ParticleSettings::initialDirection),
            Codec.BOOL.optionalFieldOf("random_initial_direction", false).forGetter(ParticleSettings::randomInitialDirection),
            CodecUtil.VECTOR3FC_CODEC.optionalFieldOf("initial_rotation", new Vector3f(0)).forGetter(ParticleSettings::initialRotation),
            Codec.BOOL.optionalFieldOf("random_initial_rotation", false).forGetter(ParticleSettings::randomInitialRotation),
            Codec.BOOL.optionalFieldOf("random_speed", false).forGetter(ParticleSettings::randomSpeed),
            Codec.BOOL.optionalFieldOf("random_size", false).forGetter(ParticleSettings::randomSize),
            Codec.BOOL.optionalFieldOf("random_lifetime", false).forGetter(ParticleSettings::randomLifetime)
    ).apply(instance, ParticleSettings::new));
    public static final Codec<Holder<ParticleSettings>> CODEC = RegistryFileCodec.create(QuasarParticles.PARTICLE_SETTINGS, DIRECT_CODEC);

    public float particleSpeed(RandomSource random) {
        return this.randomSpeed ? this.particleSpeed + (random.nextFloat() * 0.5f - 0.5f) * this.particleSpeed : this.particleSpeed;
    }

    public float particleSize(RandomSource random) {
        return this.randomSize ? this.particleSize + random.nextFloat() * this.particleSizeVariation : this.particleSize;
    }

    public int particleLifetime(RandomSource random) {
        return this.randomLifetime ? this.particleLifetime + (int) (random.nextFloat() * this.particleLifetimeVariation) : this.particleLifetime;
    }

    public Vector3fc initialDirection(RandomSource random) {
        return this.randomInitialDirection ? this.initialDirection.mul(random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1, new Vector3f()) : this.initialDirection;
    }

    /**
     * @since 4.3.0
     */
    public Vector3fc initialRotation(RandomSource random) {
        return this.randomInitialRotation ? this.initialRotation.mul(random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1, new Vector3f()) : this.initialRotation;
    }

    public Vector3f particleDirection(RandomSource random) {
        return this.initialDirection(random).mul(this.particleSpeed(random), new Vector3f());
    }

    public @Nullable ResourceLocation getRegistryId() {
        return QuasarParticles.registryAccess().registry(QuasarParticles.PARTICLE_SETTINGS).map(registry -> registry.getKey(this)).orElse(null);
    }
}
