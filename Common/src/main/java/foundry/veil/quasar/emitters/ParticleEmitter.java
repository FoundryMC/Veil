package foundry.veil.quasar.emitters;

import foundry.veil.quasar.client.particle.QuasarVanillaParticle;
import foundry.veil.quasar.client.particle.data.QuasarParticleData;
import foundry.veil.quasar.data.ParticleEmitterData;
import foundry.veil.quasar.emitters.modules.emitter.settings.EmitterSettingsModuleData;
import foundry.veil.quasar.emitters.modules.particle.init.forces.InitialVelocityForce;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3fc;

/*
 *  TODO:
 *   ✅ REQUIRED EmitterModule that controls lifetime, loop, rate, count, position
 *   ✅ OPTIONAL EmitterSettingsModule that controls randomization, emission area, emission shape, emission direction, emission speed, onEdge, linked entity, live position, etc
 *   OPTIONAL EmitterEntityModule that controls entity-specific settings
 *   OPTIONAL EmitterParticleModule that controls particle-specific settings
 *   OPTIONAL EmitterBlockEntityModule that controls block entity-specific settings
 *   OPTIONAL SubEmitterModule that controls sub-emitters
 *   OPTIONAL OnSpawnAction that can be set to run when a particle is spawned
 *   OPTIONAL OnDeathAction that can be set to run when a particle dies
 *   OPTIONAL OnUpdateAction that can be set to run when a particle is updated
 *   OPTIONAL OnRenderAction that can be set to run when a particle is rendered
 */
public class ParticleEmitter {

    private final ClientLevel level;
    private final Vector3d position;
    private final ParticleEmitterData data;
    private final QuasarParticleData particleData;

    private boolean removed;
    private int particleCount = 0;

    /**
     * Current number of ticks the emitter has been active for
     */
    private int age;

    public ParticleEmitter(ClientLevel level, ParticleEmitterData data) {
        this.level = level;
        this.data = data;
        this.position = new Vector3d();
        this.particleData = data.getParticleData();
    }

    /**
     * Marks this emitter to be removed next tick.
     */
    public void remove() {
        this.removed = true;
    }

    /**
     * Resets the emitter to its initial state
     */
    public void reset() {
        this.age = 0;
        this.removed = false;
    }

    public @Nullable ResourceLocation getRegistryName() {
        return ParticleEmitterRegistry.getEmitterId(this.data);
    }

    /**
     * Whether the emitter has completed its lifetime
     */
    public boolean isRemoved() {
        return this.removed;
    }

    /**
     * Position of the emitter
     */
    public Vector3d getPosition() {
        return this.position;
    }

    /**
     * Number of ticks the emitter has been active for
     */
    public int getAge() {
        return this.age;
    }

    public int getParticleCount() {
        return this.particleCount;
    }

    @Deprecated
    public void setPosition(Vec3 position) {
        this.position.set(position.x, position.y, position.z);
    }

    public void setPosition(Vector3dc position) {
        this.position.set(position);
    }

    @Deprecated
    public QuasarParticleData getParticleData() {
        return this.data.getParticleData();
    }

    private void run() {
        // apply spread

//        if (emitterModule.getCurrentLifetime() == 0) {
//            this.active = true;
//        }
        RandomSource random = this.level.random;
        EmitterSettingsModuleData emitterSettingsModule = this.data.getEmitterSettingsModule().value();
        Vector3dc particlePos = emitterSettingsModule.emitterShapeSettings().value().getPos(random, this.position);
        Vector3fc particleDirection = emitterSettingsModule.emissionParticleSettings().value().particleDirection(random);
        QuasarParticleData data = this.data.getParticleData().instance();
        data.parentEmitter = this;
        data.getInitModules().stream().filter(force -> force instanceof InitialVelocityForce).forEach(f -> {
            InitialVelocityForce force = (InitialVelocityForce) f;
            if (force.takesParentRotation()) {
                Vector3fc rotation = emitterSettingsModule.emitterShapeSettings().value().getRotation();
                force.velocityDirection = force.velocityDirection
                        .xRot((float) -Math.toRadians(rotation.x()))
                        .yRot((float) -Math.toRadians(rotation.y()))
                        .zRot((float) -Math.toRadians(rotation.z()));
            }
        });
        Minecraft.getInstance().particleEngine.add(new QuasarVanillaParticle(data, this.level, particlePos.x(), particlePos.y(), particlePos.z(), particleDirection.x(), particleDirection.y(), particleDirection.z()));
    }

    /**
     * Tick the emitter. This is run to track the basic functionality of the emitter.
     */
    public void tick() {
        if (this.age % this.data.getRate() == 0) {
            int count = (int) (this.data.getCount() * ParticleSystemManager.getInstance().getSpawnScale());
            for (int i = 0; i < count; i++) {
                this.run();
            }
        }
        this.age++;
        if (this.age >= this.data.getMaxLifetime()) {
            if (this.data.isLoop()) {
                this.age = 0;
            } else {
                this.remove();
            }
        }
    }

    @ApiStatus.Internal
    public void particleAdded() {
        this.particleCount++;
    }

    @ApiStatus.Internal
    public void particleRemoved() {
        this.particleCount--;
    }
}
