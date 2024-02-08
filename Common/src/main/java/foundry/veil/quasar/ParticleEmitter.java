package foundry.veil.quasar;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import foundry.veil.api.client.render.VeilRenderType;
import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.client.particle.QuasarVanillaParticle;
import foundry.veil.quasar.client.particle.RenderData;
import foundry.veil.quasar.data.EmitterSettings;
import foundry.veil.quasar.data.ParticleEmitterData;
import foundry.veil.quasar.data.QuasarParticleData;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

    private final ParticleSystemManager particleManager;
    private final ClientLevel level;
    private final ParticleEmitterData emitterData;
    private final RandomSource randomSource;
    private final Vector3d position;
    private final List<QuasarParticle> particles;
    @Nullable
    private Entity attachedEntity;

    private boolean removed;

    /**
     * Current number of ticks the emitter has been active for
     */
    private int age;

    ParticleEmitter(ParticleSystemManager particleManager, ClientLevel level, ParticleEmitterData data) {
        this.particleManager = particleManager;
        this.level = level;
        this.emitterData = data;
        this.randomSource = RandomSource.create();
        this.position = new Vector3d();
        this.particles = new ArrayList<>(data.maxParticles());
    }

    private void run() {
        // apply spread

//        if (emitterModule.getCurrentLifetime() == 0) {
//            this.active = true;
//        }
        EmitterSettings emitterSettings = this.emitterData.emitterSettings();
        Vector3dc particlePos = emitterSettings.emitterShapeSettings().getPos(this.randomSource, this.position);
        Vector3fc particleDirection = emitterSettings.particleSettings().particleDirection(this.randomSource);
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

        QuasarParticle particle = new QuasarParticle(this.level, this.randomSource, this.emitterData.particleData(), this.emitterData.emitterSettings().particleSettings(), this);
        particle.getPosition().set(particlePos);
        particle.getVelocity().set(particleDirection);
        particle.init();
        this.particles.add(particle);
//        Minecraft.getInstance().particleEngine.add(new QuasarVanillaParticle(this.emitterData.particleData(), this.emitterData.emitterSettings().particleSettings(), this, this.level, particlePos.x(), particlePos.y(), particlePos.z(), particleDirection.x(), particleDirection.y(), particleDirection.z()));
    }

    /**
     * Tick the emitter. This is run to track the basic functionality of the emitter.
     */
    public void tick() {
        if (this.attachedEntity != null) {
            if (this.attachedEntity.isAlive()) {
                Vec3 pos = this.attachedEntity.position();
                this.position.set(pos.x, pos.y, pos.z);
            } else {
                this.attachedEntity = null;
            }
        }

        Iterator<QuasarParticle> iterator = this.particles.iterator();
        while (iterator.hasNext()) {
            QuasarParticle particle = iterator.next();
            particle.tick();
            if (particle.isRemoved()) {
                iterator.remove();
                particle.onRemove();
            }
        }

        // Let particles finish before removing the emitter
        if (!this.removed) {
            if (this.age % this.emitterData.rate() == 0) {
                int count = Math.min(this.emitterData.maxParticles(), (int) Math.ceil(this.emitterData.count() * this.particleManager.getSpawnScale()));
                for (int i = 0; i < count; i++) {
                    this.run();
                }
            }

            if (this.age > this.emitterData.maxLifetime()) {
                if (this.emitterData.loop()) {
                    this.age = 0;
                } else {
                    this.remove();
                }
            }
        }
        this.age++;
    }

    // TODO move to renderer
    @ApiStatus.Internal
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, Camera camera, float partialTicks) {
        Vec3 projectedView = camera.getPosition();
        QuasarParticleData particleData = this.emitterData.particleData();
        QuasarVanillaParticle.RenderStyle renderStyle = particleData.renderStyle();

        Vector3f renderOffset = new Vector3f();
        Vector3d motionDirection = new Vector3d();
        for (QuasarParticle particle : this.particles) {
            RenderData renderData = particle.getRenderData();

            particle.render(partialTicks);
//        double ageMultiplier = 1; //1 - Math.pow(Mth.clamp(age + partialTicks, 0, lifetime), 3) / Math.pow(lifetime, 3);
//        float lX = (float) (Mth.lerp(partialTicks, this.xo, this.x));
//        float lY = (float) (Mth.lerp(partialTicks, this.yo, this.y));
//        float lZ = (float) (Mth.lerp(partialTicks, this.zo, this.z));
//        float lerpedYaw = Mth.lerp(partialTicks, this.oYaw, this.yaw);
//        float lerpedPitch = Mth.lerp(partialTicks, this.oPitch, this.pitch);
//        float lerpedRoll = Mth.lerp(partialTicks, this.oRoll, this.roll);
//        if (!this.renderData.getTrails().isEmpty()) {
//            if (this.trails.isEmpty()) {
//                this.renderData.getTrails().forEach(trail -> {
//                    Trail tr = new Trail(MathUtil.colorFromVec4f(trail.getTrailColor()), (ageScale) -> trail.getTrailWidthModifier().modify(ageScale, ageMultiplier));
//                    tr.setBillboard(trail.getBillboard());
//                    tr.setLength(trail.getTrailLength());
//                    tr.setFrequency(trail.getTrailFrequency());
//                    tr.setTilingMode(trail.getTilingMode());
//                    tr.setTexture(trail.getTrailTexture());
//                    tr.setParentRotation(trail.getParentRotation());
//                    tr.pushRotatedPoint(new Vec3(this.xo, this.yo, this.zo), new Vec3(lerpedYaw, lerpedPitch, lerpedRoll));
//                    this.trails.add(tr);
//                });
//            }
//            this.trails.forEach(trail -> {
//                trail.pushRotatedPoint(new Vec3(lX, lY, lZ), new Vec3(lerpedYaw, lerpedPitch, lerpedRoll));
//                PoseStack ps = new PoseStack();
//                ps.pushPose();
//                ps.translate(-projectedView.x(), -projectedView.y(), -projectedView.z());
//                trail.render(ps, Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderTypeRegistry.translucentNoCull(trail.getTexture())), this.emissive ? LightTexture.FULL_BRIGHT : this.getLightColor(partialTicks));
//                ps.popPose();
//            });
//        }
            renderData.renderTrails(poseStack, bufferSource, projectedView, LightTexture.FULL_BRIGHT);

            Vector3dc renderPosition = renderData.getRenderPosition();
            renderOffset.set(
                    (float) (renderPosition.x() - projectedView.x()),
                    (float) (renderPosition.y() - projectedView.y()),
                    (float) (renderPosition.z() - projectedView.z()));
            particle.getVelocity().normalize(motionDirection);
            VertexConsumer builder = bufferSource.getBuffer(VeilRenderType.quasarParticle(renderData.getTexture()));
            renderStyle.render(poseStack, particle, renderData, renderOffset, motionDirection, particle.getLightColor(), builder, 1, partialTicks);
        }
    }

    @ApiStatus.Internal
    void onRemoved() {
        for (QuasarParticle particle : this.particles) {
            particle.onRemove();
        }
        this.particles.clear();
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
        return this.removed && this.particles.isEmpty();
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

    public int getParticleCount() {
        return this.particles.size();
    }

    /**
     * @return The entity this emitter is attached to and will apply
     */
    public @Nullable Entity getAttachedEntity() {
        return this.attachedEntity;
    }

    @Deprecated
    public void setPosition(Vec3 position) {
        this.position.set(position.x, position.y, position.z);
    }

    public void setPosition(Vector3dc position) {
        this.position.set(position);
    }

    public void setAttachedEntity(@Nullable Entity entity) {
        this.attachedEntity = entity;
    }
}
