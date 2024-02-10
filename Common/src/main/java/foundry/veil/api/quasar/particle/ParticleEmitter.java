package foundry.veil.api.quasar.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import foundry.veil.api.TickTaskScheduler;
import foundry.veil.api.quasar.data.EmitterSettings;
import foundry.veil.api.quasar.data.ParticleEmitterData;
import foundry.veil.api.quasar.data.QuasarParticleData;
import foundry.veil.api.quasar.data.module.ParticleModuleData;
import foundry.veil.api.quasar.emitters.module.update.FaceVelocityModule;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
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
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;

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

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<Holder<ParticleModuleData>> REPORTED_MODULES = new HashSet<>();

    private final ParticleSystemManager particleManager;
    private final ClientLevel level;
    private final ParticleEmitterData emitterData;
    private final ParticleModuleSet modules;
    private final RandomSource randomSource;
    private final Vector3d position;
    private final List<QuasarParticle> particles;

    @Nullable
    private Entity attachedEntity;
    private CompletableFuture<?> spawnTask;
    private CompletableFuture<?> removeTask;
    private boolean removed;

    ParticleEmitter(ParticleSystemManager particleManager, ClientLevel level, ParticleEmitterData data) {
        this.particleManager = particleManager;
        this.level = level;
        this.emitterData = data;
        this.modules = createModuleSet(data.particleData());
        this.randomSource = RandomSource.create();
        this.position = new Vector3d();
        this.particles = new ArrayList<>();

        TickTaskScheduler scheduler = particleManager.getScheduler();
        this.spawnTask = scheduler.scheduleAtFixedRate(this::spawn, 0, data.rate());
        this.reset();
    }

    @ApiStatus.Internal
    public static void clearErrors() {
        REPORTED_MODULES.clear();
    }

    private void spawn() {
        int count = Math.min(this.emitterData.maxParticles(), this.emitterData.count());
        this.particleManager.reserve(count);

        for (int i = 0; i < count; i++) {
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

            QuasarParticle particle = new QuasarParticle(this.level, this.randomSource, this.particleManager.getScheduler(), this.emitterData.particleData(), this.modules.copy(), this.emitterData.emitterSettings().particleSettings(), this);
            particle.getPosition().set(particlePos);
            particle.getVelocity().set(particleDirection);
            particle.init();
            this.particles.add(particle);
        }
    }

    private static ParticleModuleSet createModuleSet(QuasarParticleData data) {
        ParticleModuleSet.Builder builder = ParticleModuleSet.builder();
        data.allModules().forEach(module -> {
            if (!module.isBound()) {
                if (REPORTED_MODULES.add(module)) {
                    LOGGER.error("Unknown module: {}", (module instanceof Holder.Reference<ParticleModuleData> ref ? ref.key().location() : module.getClass().getName()));
                }
                return;
            }
            module.value().addModules(builder);
        });
        if (data.faceVelocity()) {
            builder.addModule(new FaceVelocityModule());
        }
        return builder.build();
    }

    private void expire() {
        if (this.emitterData.loop()) {
            this.reset();
        } else {
            this.remove();
        }
    }

    private void cancelTasks() {
        if (this.spawnTask != null) {
            this.spawnTask.cancel(false);
            this.spawnTask = null;
        }
        if (this.removeTask != null) {
            this.removeTask.cancel(false);
            this.removeTask = null;
        }
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
                this.remove();
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

//        if (this.removed) {
//            this.cancelTasks();
//        } else {
//            // Let particles finish before removing the emitter
//            if (this.age > this.emitterData.maxLifetime()) {
//                if (this.emitterData.loop()) {
//                    this.reset();
//                } else {
//                    this.remove();
//                }
//            }
//        }
    }

    // TODO move to renderer
    @ApiStatus.Internal
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, Camera camera, float partialTicks) {
        Vec3 projectedView = camera.getPosition();
        QuasarParticleData particleData = this.emitterData.particleData();
        RenderData.RenderStyle renderStyle = particleData.renderStyle();

        Vector3f renderOffset = new Vector3f();
        RenderType lastRenderType = null;
        VertexConsumer builder = null;
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

            RenderType renderType = renderData.getRenderType();
            if (!renderType.equals(lastRenderType)) {
                lastRenderType = renderType;
                builder = bufferSource.getBuffer(renderType);

                TextureAtlasSprite sprite = renderData.getAtlasSprite();
                if (sprite != null) {
                    builder = sprite.wrap(builder);
                }
            }

            renderStyle.render(poseStack, particle, renderData, renderOffset, builder, 1, partialTicks);
        }
    }

    @ApiStatus.Internal
    void onRemoved() {
        this.cancelTasks();
        for (QuasarParticle particle : this.particles) {
            particle.onRemove();
        }
        this.particles.clear();
    }

    /**
     * Attempts to remove the oldest specified number of particles.
     *
     * @param count The number of particles to attempt to remove
     * @return The number of particles removed
     */
    public int trim(int count) {
        int i;
        int removeCount = Math.min(count, this.particles.size());
        for (i = 0; i < removeCount; i++) {
            this.particles.get(i).remove();
        }
        return i;
    }

    /**
     * Marks this emitter to be removed next tick.
     */
    public void remove() {
        this.removed = true;
        this.cancelTasks();
    }

    /**
     * Resets the emitter to its initial state
     */
    public void reset() {
        this.removed = false;
        if (this.removeTask != null) {
            this.removeTask.cancel(false);
        }
        this.removeTask = this.particleManager.getScheduler().schedule(this::expire, this.emitterData.maxLifetime());
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
        if (entity != null) {
            this.position.set(entity.getX(), entity.getY(), entity.getZ());
        }
    }
}
