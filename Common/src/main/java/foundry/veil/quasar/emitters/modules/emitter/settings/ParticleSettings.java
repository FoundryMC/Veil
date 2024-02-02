package foundry.veil.quasar.emitters.modules.emitter.settings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.quasar.data.DynamicParticleDataRegistry;
import foundry.veil.quasar.util.CodecUtil;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public record ParticleSettings(float particleSpeed,
                               float particleSize,
                               float particleSizeVariation,
                               int particleLifetime,
                               float particleLifetimeVariation,
                               Vector3fc initialDirection,
                               boolean randomInitialDirection,
                               boolean randomInitialRotation,
                               boolean randomSpeed,
                               boolean randomSize,
                               boolean randomLifetime) {

    public static final Codec<ParticleSettings> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("particle_speed").forGetter(ParticleSettings::particleSpeed),
            Codec.FLOAT.fieldOf("base_particle_size").forGetter(ParticleSettings::particleSize),
            Codec.FLOAT.fieldOf("particle_size_variation").forGetter(ParticleSettings::particleSizeVariation),
            Codec.INT.fieldOf("particle_lifetime").forGetter(ParticleSettings::particleLifetime),
            Codec.FLOAT.fieldOf("particle_lifetime_variation").forGetter(ParticleSettings::particleLifetimeVariation),
            CodecUtil.VECTOR3F_CODEC.fieldOf("initial_direction").forGetter(ParticleSettings::initialDirection),
            Codec.BOOL.fieldOf("random_initial_direction").forGetter(ParticleSettings::randomInitialDirection),
            Codec.BOOL.fieldOf("random_initial_rotation").forGetter(ParticleSettings::randomInitialRotation),
            Codec.BOOL.fieldOf("random_speed").forGetter(ParticleSettings::randomSpeed),
            Codec.BOOL.fieldOf("random_size").forGetter(ParticleSettings::randomSize),
            Codec.BOOL.fieldOf("random_lifetime").forGetter(ParticleSettings::randomLifetime)
    ).apply(instance, ParticleSettings::new));
    public static final Codec<Holder<ParticleSettings>> CODEC = RegistryFileCodec.create(DynamicParticleDataRegistry.PARTICLE_SETTINGS, DIRECT_CODEC);

    private static final RandomSource random = new LegacyRandomSource(69L);

    public ResourceLocation getRegistryId() {
        return EmitterSettingsRegistry.getParticleSettingsId(this);
    }

    @Override
    public float particleSpeed() {
        return this.randomSpeed ? this.particleSpeed + (random.nextFloat() * 0.5f - 0.5f) * this.particleSpeed : this.particleSpeed;
    }

    @Override
    public float particleSize() {
        return this.randomSize ? this.particleSize + random.nextFloat() * this.particleSizeVariation : this.particleSize;
    }

    @Override
    public int particleLifetime() {
        return this.randomLifetime ? this.particleLifetime + (int) (random.nextFloat() * this.particleLifetimeVariation) : this.particleLifetime;
    }

    @Override
    public Vector3fc initialDirection() {
        return this.randomInitialDirection ? this.initialDirection.mul(random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1, new Vector3f()) : this.initialDirection;
    }

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

    public Vector3f particleDirection(RandomSource random) {
        return this.initialDirection(random).mul(this.particleSpeed(), new Vector3f());
    }
}
