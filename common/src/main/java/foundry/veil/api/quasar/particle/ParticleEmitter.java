package foundry.veil.api.quasar.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import foundry.veil.Veil;
import foundry.veil.api.TickTaskScheduler;
import foundry.veil.api.client.render.MatrixStack;
import foundry.veil.api.quasar.data.*;
import foundry.veil.api.quasar.data.module.CodeModule;
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
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

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
@ApiStatus.NonExtendable
public class ParticleEmitter {

    private static final Set<Holder<ParticleModuleData>> REPORTED_MODULES = new HashSet<>();

    protected final ParticleSystemManager particleManager;
    private final ClientLevel level;
    private final ParticleEmitterData emitterData;
    private final List<ParticleModuleData> modules;
    private final List<ParticleModuleData> modulesView;
    private final RandomSource randomSource;
    private final Vector3d position;
    private final Vector3d offset;
    private final List<QuasarParticle> particles;

    private int maxLifetime;
    private boolean loop;
    private int rate;
    private int count;
    private int maxParticles;
    private List<EmitterShapeSettings> emitterShapeSettings;
    private ParticleSettings particleSettings;
    private boolean forceSpawn;
    protected QuasarParticleData particleData;

    @Nullable
    private Entity attachedEntity;
    protected CompletableFuture<?> spawnTask;
    protected CompletableFuture<?> removeTask;
    private boolean removed;

    protected ParticleEmitter(ParticleSystemManager particleManager, ClientLevel level, ParticleEmitterData data) {
        this.particleManager = particleManager;
        this.level = level;
        this.emitterData = data;
        this.modules = createModuleSet(data.particleData());
        this.modulesView = Collections.unmodifiableList(this.modules);
        this.randomSource = RandomSource.create();
        this.position = new Vector3d();
        this.offset = new Vector3d();
        this.particles = new LinkedList<>();

        this.maxLifetime = data.maxLifetime();
        this.loop = data.loop();
        this.rate = data.rate();
        this.count = data.count();
        this.maxParticles = data.maxParticles();
        EmitterSettings emitterSettings = data.emitterSettings();
        this.emitterShapeSettings = new ArrayList<>(emitterSettings.emitterShapeSettings());
        this.particleSettings = emitterSettings.particleSettings();
        this.forceSpawn = emitterSettings.forceSpawn();
        this.particleData = data.particleData();

        // Perform the removal task first so no more particles are spawned
        this.reset();
        TickTaskScheduler scheduler = particleManager.getScheduler();
        this.spawnTask = scheduler.scheduleAtFixedRate(this::spawn, 1, data.rate()).toCompletableFuture();
    }

    @ApiStatus.Internal
    void onAdd() {
        this.spawn();
    }

    @ApiStatus.Internal
    public static void clearErrors() {
        REPORTED_MODULES.clear();
    }

    protected void spawn() {
        int count = Math.min(this.maxParticles, this.count);
        this.particleManager.reserve(count);

        for (int i = 0; i < count; i++) {
            Vector3dc particlePos = this.emitterShapeSettings.get(i % this.emitterShapeSettings.size()).getPos(this.randomSource, this.position);
            Vector3fc particleDirection = this.particleSettings.particleDirection(this.randomSource);
            Vector3fc particleRotation = this.particleSettings.initialRotation(this.randomSource).mul(Mth.DEG_TO_RAD, new Vector3f());

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

            ParticleModuleSet.Builder builder = ParticleModuleSet.builder();
            for (ParticleModuleData module : this.modules) {
                module.addModules(builder);
            }
            if (this.particleData.faceVelocity()) {
                builder.addModule(new FaceVelocityModule());
            }

            QuasarParticle particle = new QuasarParticle(this.level, this.randomSource, this.particleManager.getScheduler(), this.particleData, builder.build(), this.particleSettings, this);
            particle.getPosition().set(particlePos);
            particle.getVelocity().set(particleDirection);
            particle.getRotation().set(particleRotation);
            particle.init();
            this.particles.add(particle);
        }
    }

    protected static List<ParticleModuleData> createModuleSet(QuasarParticleData data) {
        List<Holder<ParticleModuleData>> allModules = data.modules();
        ArrayList<ParticleModuleData> list = new ArrayList<>(allModules.size());
        for (Holder<ParticleModuleData> module : allModules) {
            if (!module.isBound()) {
                if (REPORTED_MODULES.add(module)) {
                    Veil.LOGGER.error("Unknown module: {}", (module instanceof Holder.Reference<ParticleModuleData> ref ? ref.key().location() : module.getClass().getName()));
                }
                continue;
            }
            list.add(module.value());
        }
        list.trimToSize();
        return list;
    }

    private void expire() {
        if (this.loop) {
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

    @ApiStatus.Internal
    protected void tick() {
        this.position.set(0);
        if (this.attachedEntity != null) {
            if (this.attachedEntity.isAlive()) {
                Vec3 pos = this.attachedEntity.position();
                this.position.set(pos.x, pos.y, pos.z);
            } else {
                this.attachedEntity = null;
                this.remove();
            }
        }

        this.position.add(this.offset);
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
    public void render(MatrixStack matrixStack, MultiBufferSource bufferSource, Camera camera, float partialTicks) {
        if (this.particles.isEmpty()) {
            return;
        }

        RenderStyle renderStyle = this.particleData.renderStyle();
        if (!renderStyle.setup(this.particles.size())) {
            return;
        }

        Vec3 projectedView = camera.getPosition();
        Vector3f renderOffset = new Vector3f();
        RenderType lastRenderType = null;
        VertexConsumer builder = null;

        for (QuasarParticle particle : this.particles) {
            RenderData renderData = particle.getRenderData();

            particle.render(partialTicks);

            renderData.renderTrails(matrixStack, bufferSource, projectedView, LightTexture.FULL_BRIGHT, partialTicks);

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

            renderStyle.render(matrixStack, particle, renderData, renderOffset, builder, 1, partialTicks);
        }
        renderStyle.clear();
    }

    @ApiStatus.Internal
    void onRemoved() {
        this.remove();
        Iterator<QuasarParticle> iterator = this.particles.iterator();
        while (iterator.hasNext()) {
            iterator.next().onRemove();
            iterator.remove();
        }
    }

    /**
     * <p>Adds a custom module with user code that is added to all particles spawned after this is called.</p>
     * <p>The module cannot be serialized and does not affect the state of any other emitters.</p>
     *
     * @param module The module to add
     */
    public void addCodeModule(CodeModule module) {
        this.addModule(module);
    }

    /**
     * Adds a new module to this emitter.
     *
     * @param module The module to add
     * @since 4.3.0
     */
    public void addModule(ParticleModuleData module) {
        this.modules.add(module);
    }

    /**
     * Removes a module from this emitter.
     *
     * @param module The module to remove
     * @since 4.3.0
     */
    public void removeModule(ParticleModuleData module) {
        this.modules.remove(module);
    }

    /**
     * Attempts to remove the oldest specified number of particles.
     *
     * @param count The number of particles to attempt to remove
     * @return The number of particles removed
     */
    public int trim(int count) {
        // Don't allow high-priority particles to be trimmed
        if (this.forceSpawn) {
            return 0;
        }

        int removeCount;
        Iterator<QuasarParticle> iterator = this.particles.iterator();
        for (removeCount = 0; iterator.hasNext() && removeCount < count; removeCount++) {
            iterator.next().onRemove();
            iterator.remove();
        }
        return removeCount;
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
        this.removeTask = this.particleManager.getScheduler().schedule(this::expire, this.maxLifetime).toCompletableFuture();
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

    public int getMaxLifetime() {
        return this.maxLifetime;
    }

    public boolean isLoop() {
        return this.loop;
    }

    public int getRate() {
        return this.rate;
    }

    public int getCount() {
        return this.count;
    }

    public int getMaxParticles() {
        return this.maxParticles;
    }

    public List<EmitterShapeSettings> getEmitterShapeSettings() {
        return this.emitterShapeSettings;
    }

    public ParticleSettings getParticleSettings() {
        return this.particleSettings;
    }

    public boolean isForceSpawn() {
        return this.forceSpawn;
    }

    public QuasarParticleData getParticleData() {
        return this.particleData;
    }

    /**
     * @return The entity this emitter is attached to and will apply
     */
    public @Nullable Entity getAttachedEntity() {
        return this.attachedEntity;
    }

    /**
     * Sets the position of the emitter relative to the origin of the world or attached entity.
     *
     * @param position The position
     */
    public void setPosition(Vec3 position) {
        this.setPosition(position.x, position.y, position.z);
    }

    /**
     * Sets the position of the emitter relative to the origin of the world or attached entity.
     *
     * @param position The position
     */
    public void setPosition(Vector3dc position) {
        this.setPosition(position.x(), position.y(), position.z());
    }

    /**
     * Sets the position of the emitter relative to the origin of the world or attached entity.
     *
     * @param x The x position
     * @param y The y position
     * @param z The z position
     */
    public void setPosition(double x, double y, double z) {
        this.offset.set(x, y, z);
        if (this.attachedEntity != null) {
            this.position.set(this.attachedEntity.getX(), this.attachedEntity.getY(), this.attachedEntity.getZ()).add(this.offset);
        } else {
            this.position.set(this.offset);
        }
    }

    public void setMaxLifetime(int maxLifetime) {
        this.maxLifetime = maxLifetime;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setMaxParticles(int maxParticles) {
        this.maxParticles = maxParticles;
    }

    public void setEmitterShapeSettings(List<EmitterShapeSettings> emitterShapeSettings) {
        this.emitterShapeSettings = emitterShapeSettings;
    }

    public void setParticleSettings(ParticleSettings particleSettings) {
        this.particleSettings = particleSettings;
    }

    public void setForceSpawn(boolean forceSpawn) {
        this.forceSpawn = forceSpawn;
    }

    public void setParticleData(QuasarParticleData particleData) {
        this.particleData = particleData;
    }

    /**
     * @since 4.3.0
     */
    @UnmodifiableView
    public List<ParticleModuleData> getModules() {
        return this.modulesView;
    }

    /**
     * Sets the origin of the emitter position to match the specified entity.
     * That means the value set by {@link #setPosition(double, double, double)} will not be interpreted as an offset from the entity position.
     *
     * @param entity The entity to attach to
     */
    public void setAttachedEntity(@Nullable Entity entity) {
        this.attachedEntity = entity;
        if (entity != null) {
            this.position.set(entity.getX(), entity.getY(), entity.getZ()).add(this.offset);
        } else {
            this.position.set(this.offset);
        }
    }
}
