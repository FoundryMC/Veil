package foundry.veil.quasar.emitters.modules.emitter.settings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

public class EmissionParticleSettings {

    public static final Codec<EmissionParticleSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("particle_speed").forGetter(EmissionParticleSettings::getParticleSpeed),
            Codec.FLOAT.fieldOf("base_particle_size").forGetter(EmissionParticleSettings::getParticleSize),
            Codec.FLOAT.fieldOf("particle_size_variation").forGetter(EmissionParticleSettings::getParticleSizeVariation),
            Codec.INT.fieldOf("particle_lifetime").forGetter(EmissionParticleSettings::getParticleLifetime),
            Codec.FLOAT.fieldOf("particle_lifetime_variation").forGetter(EmissionParticleSettings::getParticleLifetimeVariation),
            Vec3.CODEC.fieldOf("initial_direction").forGetter(EmissionParticleSettings::getInitialDirection),
            Codec.BOOL.fieldOf("random_initial_direction").forGetter(EmissionParticleSettings::isRandomInitialDirection),
            Codec.BOOL.fieldOf("random_initial_rotation").forGetter(EmissionParticleSettings::isRandomInitialRotation),
            Codec.BOOL.fieldOf("random_speed").forGetter(EmissionParticleSettings::isRandomSpeed),
            Codec.BOOL.fieldOf("random_size").forGetter(EmissionParticleSettings::isRandomSize),
            Codec.BOOL.fieldOf("random_lifetime").forGetter(EmissionParticleSettings::isRandomLifetime)
    ).apply(instance, EmissionParticleSettings::new));

    private final RandomSource randomSource = new LegacyRandomSource(69L);
    private final float particleSpeed;
    private final float baseParticleSize;
    private final float particleSizeVariation;
    private final int particleLifetime;
    private final float particleLifetimeVariation;
    private final Supplier<Vec3> initialDirection;
    private final boolean randomInitialDirection;
    private final boolean randomInitialRotation;
    private final boolean randomSpeed;
    private final boolean randomSize;
    private final boolean randomLifetime;

    private EmissionParticleSettings(float particleSpeed, float baseParticleSize, float particleSizeVariation, int particleLifetime, float particleLifetimeVariation, Supplier<Vec3> initialDirection, boolean randomInitialDirection, boolean randomInitialRotation, boolean randomSpeed, boolean randomSize, boolean randomLifetime) {
        this.particleSpeed = particleSpeed;
        this.baseParticleSize = baseParticleSize;
        this.particleSizeVariation = particleSizeVariation;
        this.particleLifetime = particleLifetime;
        this.particleLifetimeVariation = particleLifetimeVariation;
        this.initialDirection = initialDirection;
        this.randomInitialDirection = randomInitialDirection;
        this.randomInitialRotation = randomInitialRotation;
        this.randomSpeed = randomSpeed;
        this.randomSize = randomSize;
        this.randomLifetime = randomLifetime;
    }

    private EmissionParticleSettings(float particleSpeed, float baseParticleSize, float particleSizeVariation, int particleLifetime, float particleLifetimeVariation, Vec3 initialDirection, boolean randomInitialDirection, boolean randomInitialRotation, boolean randomSpeed, boolean randomSize, boolean randomLifetime) {
        this.particleSpeed = particleSpeed;
        this.baseParticleSize = baseParticleSize;
        this.particleSizeVariation = particleSizeVariation;
        this.particleLifetime = particleLifetime;
        this.particleLifetimeVariation = particleLifetimeVariation;
        this.initialDirection = () -> initialDirection;
        this.randomInitialDirection = randomInitialDirection;
        this.randomInitialRotation = randomInitialRotation;
        this.randomSpeed = randomSpeed;
        this.randomSize = randomSize;
        this.randomLifetime = randomLifetime;
    }

    public EmissionParticleSettings instance() {
        return new EmissionParticleSettings(this.particleSpeed, this.baseParticleSize, this.particleSizeVariation, this.particleLifetime, this.particleLifetimeVariation, this.initialDirection, this.randomInitialDirection, this.randomInitialRotation, this.randomSpeed, this.randomSize, this.randomLifetime);
    }

    public ResourceLocation getRegistryId() {
        return EmitterSettingsRegistry.getParticleSettingsId(this);
    }

    public float getParticleSpeed() {
        return this.randomSpeed ? this.particleSpeed + (this.randomSource.nextFloat() * 0.5f - 0.5f) * this.particleSpeed : this.particleSpeed;
    }

    public float getParticleSize() {
        return this.randomSize ? this.baseParticleSize + this.randomSource.nextFloat() * this.particleSizeVariation : this.baseParticleSize;
    }

    public int getParticleLifetime() {
        return this.randomLifetime ? this.particleLifetime + (int) (this.randomSource.nextFloat() * this.particleLifetimeVariation) : this.particleLifetime;
    }

    public Vec3 getInitialDirection() {
        return this.randomInitialDirection ? this.initialDirection.get().multiply(this.randomSource.nextFloat() * 2 - 1, this.randomSource.nextFloat() * 2 - 1, this.randomSource.nextFloat() * 2 - 1) : this.initialDirection.get();
    }

    public boolean isRandomInitialDirection() {
        return this.randomInitialDirection;
    }

    public boolean isRandomInitialRotation() {
        return this.randomInitialRotation;
    }

    public boolean isRandomSpeed() {
        return this.randomSpeed;
    }

    public boolean isRandomSize() {
        return this.randomSize;
    }

    public boolean isRandomLifetime() {
        return this.randomLifetime;
    }

    public float getParticleSizeVariation() {
        return this.particleSizeVariation;
    }

    public float getParticleLifetimeVariation() {
        return this.particleLifetimeVariation;
    }

    public float getBaseParticleSize() {
        return this.baseParticleSize;
    }

    public int getBaseParticleLifetime() {
        return this.particleLifetime;
    }

    public RandomSource getRandomSource() {
        return this.randomSource;
    }

    public float getBaseParticleSpeed() {
        return this.particleSpeed;
    }

    public Supplier<Vec3> getInitialDirectionSupplier() {
        return this.initialDirection;
    }
}
