package foundry.veil.quasar.emitters;

import foundry.veil.quasar.client.particle.QuasarVanillaParticle;
import foundry.veil.quasar.data.EmitterSettings;
import foundry.veil.quasar.data.ParticleEmitterData;
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
    private final ParticleEmitterData emitterData;

    private boolean removed;
    private int particleCount = 0;

    /**
     * Current number of ticks the emitter has been active for
     */
    private int age;

    public ParticleEmitter(ClientLevel level, ParticleEmitterData data) {
        this.level = level;
        this.emitterData = data;
        this.position = new Vector3d();
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
        return this.emitterData.getRegistryId();
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

    public ParticleEmitterData getData() {
        return this.emitterData;
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

    private void run() {
        // apply spread

//        if (emitterModule.getCurrentLifetime() == 0) {
//            this.active = true;
//        }
        RandomSource random = this.level.random;
        EmitterSettings emitterSettings = this.emitterData.emitterSettings();
        Vector3dc particlePos = emitterSettings.emitterShapeSettings().getPos(random, this.position);
        Vector3fc particleDirection = emitterSettings.particleSettings().particleDirection(random);
        // TODO
//        this.getParticleData().getInitModules().stream().filter(force -> force instanceof InitialVelocityForce).forEach(f -> {
//            InitialVelocityForce force = (InitialVelocityForce) f;
//            if (force.takesParentRotation()) {
//                Vector3fc rotation = emitterSettings.emitterShapeSettings().value().getRotation();
//                force.velocityDirection = force.velocityDirection
//                        .xRot((float) -Math.toRadians(rotation.x()))
//                        .yRot((float) -Math.toRadians(rotation.y()))
//                        .zRot((float) -Math.toRadians(rotation.z()));
//            }
//        });
        Minecraft.getInstance().particleEngine.add(new QuasarVanillaParticle(this.emitterData.particleData(), this.emitterData.emitterSettings().particleSettings(), this, this.level, particlePos.x(), particlePos.y(), particlePos.z(), particleDirection.x(), particleDirection.y(), particleDirection.z()));
    }

    /**
     * Tick the emitter. This is run to track the basic functionality of the emitter.
     */
    public void tick() {
        if (this.age % this.emitterData.rate() == 0) {
            int count = (int) (this.emitterData.count() * ParticleSystemManager.getInstance().getSpawnScale());
            for (int i = 0; i < count; i++) {
                this.run();
            }
        }
        this.age++;
        if (this.age >= this.emitterData.maxLifetime()) {
            if (this.emitterData.loop()) {
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
