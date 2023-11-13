package foundry.veil.quasar.emitters.modules.emitter.settings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

public class EmissionParticleSettings {
    public static final Codec<EmissionParticleSettings> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
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
        ).apply(instance, EmissionParticleSettings::new);
    });

    // this is sometimes null idk how
    public ResourceLocation registryName;
    RandomSource randomSource = new LegacyRandomSource(69L);
    float particleSpeed;
    float baseParticleSize;
    float particleSizeVariation;
    int particleLifetime;
    float particleLifetimeVariation;
    Supplier<Vec3> initialDirection;
    boolean randomInitialDirection;
    boolean randomInitialRotation;
    boolean randomSpeed;
    boolean randomSize;
    boolean randomLifetime;

    private EmissionParticleSettings(RandomSource randomSource, float particleSpeed, float baseParticleSize, float particleSizeVariation, int particleLifetime, float particleLifetimeVariation, Supplier<Vec3> initialDirection, boolean randomInitialDirection, boolean randomInitialRotation, boolean randomSpeed, boolean randomSize, boolean randomLifetime) {
        this.randomSource = randomSource;
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

    public EmissionParticleSettings instance(){
        EmissionParticleSettings instance = new EmissionParticleSettings(randomSource, particleSpeed, baseParticleSize, particleSizeVariation, particleLifetime, particleLifetimeVariation, initialDirection, randomInitialDirection, randomInitialRotation, randomSpeed, randomSize, randomLifetime);
        instance.registryName = registryName;
        return instance;
    }

    public ResourceLocation getRegistryId() {
        return registryName;
    }

    public float getParticleSpeed() {
        return randomSpeed ? particleSpeed + (randomSource.nextFloat() * 0.5f - 0.5f) * particleSpeed : particleSpeed;
    }

    public float getParticleSize(){
        return randomSize ? baseParticleSize + randomSource.nextFloat() * particleSizeVariation : baseParticleSize;
    }

    public int getParticleLifetime(){
        return randomLifetime ? particleLifetime + (int)(randomSource.nextFloat() * particleLifetimeVariation) : particleLifetime;
    }

    public Vec3 getInitialDirection(){
        return randomInitialDirection ? initialDirection.get().multiply(randomSource.nextFloat() * 2 - 1, randomSource.nextFloat() * 2 - 1, randomSource.nextFloat() * 2 - 1) : initialDirection.get();
    }

    public boolean isRandomInitialDirection() {
        return randomInitialDirection;
    }

    public boolean isRandomInitialRotation() {
        return randomInitialRotation;
    }

    public boolean isRandomSpeed() {
        return randomSpeed;
    }

    public boolean isRandomSize() {
        return randomSize;
    }

    public boolean isRandomLifetime() {
        return randomLifetime;
    }

    public float getParticleSizeVariation() {
        return particleSizeVariation;
    }

    public float getParticleLifetimeVariation() {
        return particleLifetimeVariation;
    }

    public float getBaseParticleSize() {
        return baseParticleSize;
    }

    public int getBaseParticleLifetime() {
        return particleLifetime;
    }

    public void setBaseParticleLifetime(int particleLifetime) {
        this.particleLifetime = particleLifetime;
    }

    public RandomSource getRandomSource() {
        return randomSource;
    }

    public float getBaseParticleSpeed() {
        return particleSpeed;
    }

    public Supplier<Vec3> getInitialDirectionSupplier() {
        return initialDirection;
    }

    public void setInitialDirection(Vec3 scale) {
        this.initialDirection = () -> scale;
    }

    public void setParticleSize(float scale) {
        this.baseParticleSize = scale;
    }

    public static class Builder {
        private RandomSource randomSource;
        private float particleSpeed = 0;
        private float baseParticleSize = 0;
        private float particleSizeVariation = 0;
        private int particleLifetime = 0;
        private float particleLifetimeVariation = 0;
        private Supplier<Vec3> initialDirection;
        private boolean randomInitialDirection = false;
        private boolean randomInitialRotation = false;
        private boolean randomSpeed = false;
        private boolean randomSize = false;
        private boolean randomLifetime = false;

        public Builder setRandomSource(RandomSource randomSource) {
            this.randomSource = randomSource;
            return this;
        }

        public Builder setParticleSpeed(float particleSpeed) {
            this.particleSpeed = particleSpeed;
            return this;
        }

        public Builder setBaseParticleSize(float baseParticleSize) {
            this.baseParticleSize = baseParticleSize;
            return this;
        }

        public Builder setParticleSizeVariation(float particleSizeVariation) {
            this.particleSizeVariation = particleSizeVariation;
            return this;
        }

        public Builder setParticleLifetime(int particleLifetime) {
            this.particleLifetime = particleLifetime;
            return this;
        }

        public Builder setParticleLifetimeVariation(float particleLifetimeVariation) {
            this.particleLifetimeVariation = particleLifetimeVariation;
            return this;
        }

        public Builder setInitialDirection(Supplier<Vec3> initialDirection) {
            this.initialDirection = initialDirection;
            return this;
        }

        public Builder setRandomInitialDirection(boolean randomInitialDirection) {
            this.randomInitialDirection = randomInitialDirection;
            return this;
        }

        public Builder setRandomInitialRotation(boolean randomInitialRotation) {
            this.randomInitialRotation = randomInitialRotation;
            return this;
        }

        public Builder setRandomSpeed(boolean randomSpeed) {
            this.randomSpeed = randomSpeed;
            return this;
        }

        public Builder setRandomSize(boolean randomSize) {
            this.randomSize = randomSize;
            return this;
        }

        public Builder setRandomLifetime(boolean randomLifetime) {
            this.randomLifetime = randomLifetime;
            return this;
        }

        public EmissionParticleSettings build() {
            return new EmissionParticleSettings(randomSource, particleSpeed, baseParticleSize, particleSizeVariation, particleLifetime, particleLifetimeVariation, initialDirection, randomInitialDirection, randomInitialRotation, randomSpeed, randomSize, randomLifetime);
        }
    }
}
