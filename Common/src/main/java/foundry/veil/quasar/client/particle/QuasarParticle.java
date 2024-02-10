package foundry.veil.quasar.client.particle;

import com.google.common.base.Suppliers;
import foundry.veil.api.TickTaskScheduler;
import foundry.veil.quasar.data.ParticleSettings;
import foundry.veil.quasar.data.QuasarParticleData;
import foundry.veil.quasar.emitters.modules.particle.*;
import gg.moonflower.molangcompiler.api.MolangEnvironment;
import gg.moonflower.molangcompiler.api.MolangExpression;
import gg.moonflower.molangcompiler.api.MolangRuntime;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public class QuasarParticle {

    private static final double MAXIMUM_COLLISION_VELOCITY_SQUARED = Mth.square(100.0D);

    private final ClientLevel level;
    private final RandomSource randomSource;
    private final TickTaskScheduler scheduler;
    private final QuasarParticleData data;
    private final ParticleSettings settings;
    private final ParticleEmitter parent;
    private final ParticleModuleSet modules;
    private final Vector3d position;
    private final Vector3d velocity;
    private final Vector3f rotation;
    private final BlockPos.MutableBlockPos blockPosition;
    private final boolean hasCollision;
    private float radius;
    private final int lifetime;
    private int age;
    private AABB boundingBox;
    private boolean stoppedByCollision;

    private final Supplier<MolangRuntime> environment;
    private final RenderData renderData;

    public QuasarParticle(ClientLevel level, RandomSource randomSource, TickTaskScheduler scheduler, QuasarParticleData data, ParticleModuleSet modules, ParticleSettings settings, ParticleEmitter parent) {
        this.level = level;
        this.randomSource = randomSource;
        this.scheduler = scheduler;
        this.data = data;
        this.settings = settings;
        this.parent = parent;
        this.modules = modules;
        this.position = new Vector3d();
        this.velocity = new Vector3d();
        this.rotation = new Vector3f();
        this.blockPosition = new BlockPos.MutableBlockPos();
        this.hasCollision = this.modules.getCollisionModules().length > 0;
        this.radius = settings.particleSize(this.randomSource);
        this.lifetime = settings.particleLifetime(this.randomSource);
        this.age = 0;

        this.renderData = new RenderData();
        // Don't create the environment if the particle never uses it
        this.environment = Suppliers.memoize(() -> MolangRuntime.runtime()
                .setQuery("x", MolangExpression.of(() -> (float) this.renderData.getRenderPosition().x()))
                .setQuery("y", MolangExpression.of(() -> (float) this.renderData.getRenderPosition().y()))
                .setQuery("z", MolangExpression.of(() -> (float) this.renderData.getRenderPosition().z()))
                .setQuery("velX", MolangExpression.of(() -> (float) this.velocity.x()))
                .setQuery("velY", MolangExpression.of(() -> (float) this.velocity.y()))
                .setQuery("velZ", MolangExpression.of(() -> (float) this.velocity.z()))
                .setQuery("speedSq", MolangExpression.of(() -> (float) this.velocity.lengthSquared()))
                .setQuery("speed", MolangExpression.of(() -> (float) this.velocity.length()))
                .setQuery("xRot", MolangExpression.of(() -> (float) Math.toDegrees(this.renderData.getRenderRotation().x())))
                .setQuery("yRot", MolangExpression.of(() -> (float) Math.toDegrees(this.renderData.getRenderRotation().y())))
                .setQuery("zRot", MolangExpression.of(() -> (float) Math.toDegrees(this.renderData.getRenderRotation().z())))
                .setQuery("scale", MolangExpression.of(this.renderData::getRenderRadius))
                .setQuery("age", MolangExpression.of(this.renderData::getRenderAge))
                .setQuery("agePercent", MolangExpression.of(this.renderData::getAgePercent))
                .setQuery("lifetime", this.lifetime)
                .create());
    }

    private void move(double dx, double dy, double dz) {
        if (this.stoppedByCollision || (dx == 0.0D && dy == 0.0D && dz == 0.0D)) {
            return;
        }

        AABB box = this.getBoundingBox();
        double d0 = dx;
        double d1 = dy;
        double d2 = dz;
        if (this.hasCollision && dx * dx + dy * dy + dz * dz < MAXIMUM_COLLISION_VELOCITY_SQUARED) {
            Vec3 vec3 = Entity.collideBoundingBox(null, new Vec3(dx, dy, dz), box, this.level, List.of());
            dx = vec3.x;
            dy = vec3.y;
            dz = vec3.z;
        }

        if (dx != 0.0D || dy != 0.0D || dz != 0.0D) {
            this.position.add(dx, dy, dz);
            this.updateBoundingBox();
        }

        if (!this.hasCollision) {
            return;
        }

        List<Entity> entities = this.level.getEntities(null, box);
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity livingEntity && livingEntity.isAlive()) {
                this.stoppedByCollision = true;
                break;
            }
        }

        if (Math.abs(d1) >= (double) 1.0E-5F && Math.abs(dy) < (double) 1.0E-5F) {
            this.stoppedByCollision = true;
        }

        if (d0 != dx) {
            this.velocity.x = 0;
            this.stoppedByCollision = true;
        }

        if (d1 != dy) {
            this.velocity.y = 0;
            this.stoppedByCollision = true;
        }

        if (d2 != dz) {
            this.velocity.z = 0;
            this.stoppedByCollision = true;
        }

        // Notify listeners
        if (this.stoppedByCollision) {
            for (CollisionParticleModule collisionParticle : this.modules.getCollisionModules()) {
                collisionParticle.collide(this);
            }
        }
    }

    private void updateBoundingBox() {
        double r = this.radius / 2.0;
        this.boundingBox = new AABB(this.position.x - r, this.position.y - r, this.position.z - r, this.position.x + r, this.position.y + r, this.position.z + r);
    }

    private int getLightColor() {
        BlockPos pos = this.getBlockPosition();
        return this.level.hasChunkAt(pos) ? LevelRenderer.getLightColor(this.level, pos) : 0;
    }

    @ApiStatus.Internal
    public void init() {
        for (InitParticleModule initModule : this.modules.getInitModules()) {
            initModule.init(this);
        }
        this.renderData.tick(this, this.getLightColor());
        this.updateBoundingBox();
    }

    @ApiStatus.Internal
    public void tick() {
        this.renderData.tick(this, this.getLightColor());
        this.modules.updateEnabled();
        for (UpdateParticleModule updateModule : this.modules.getUpdateModules()) {
            updateModule.update(this);
        }
        // TODO properly do forces
        for (ForceParticleModule updateModule : this.modules.getForceModules()) {
            updateModule.applyForce(this);
        }

        // TODO make this a module
        if (this.data.faceVelocity()) {
            Vector3d normalizedMotion = this.velocity.normalize(new Vector3d());
            this.rotation.x = (float) Mth.atan2(normalizedMotion.y, Math.sqrt(normalizedMotion.x * normalizedMotion.x + normalizedMotion.z * normalizedMotion.z));
            this.rotation.y = (float) Mth.atan2(normalizedMotion.x, normalizedMotion.z);
            if (this.data.renderStyle() == RenderData.RenderStyle.BILLBOARD) {
                this.rotation.y += (float) (Math.PI / 2.0);
            }
        }

        this.move(this.velocity.x, this.velocity.y, this.velocity.z);

        this.age++;
        if (this.age >= this.lifetime) {
            this.remove();
        }
    }

    @ApiStatus.Internal
    public void render(float partialTicks) {
        Iterator<RenderParticleModule> iterator = this.modules.getEnabledRenderModules();
        while (iterator.hasNext()) {
            iterator.next().render(this, partialTicks);
        }
        this.renderData.render(this, partialTicks);
    }

    @ApiStatus.Internal
    public void onRemove() {
        for (ParticleModule module : this.modules.getAllModules()) {
            module.onRemove();
        }
    }

    public void remove() {
        this.age = Integer.MIN_VALUE;
    }

    public boolean isRemoved() {
        return this.age < 0;
    }

    public ClientLevel getLevel() {
        return this.level;
    }

    public RandomSource getRandomSource() {
        return this.randomSource;
    }

    public TickTaskScheduler getScheduler() {
        return this.scheduler;
    }

    public QuasarParticleData getData() {
        return this.data;
    }

    public ParticleSettings getSettings() {
        return this.settings;
    }

    public ParticleEmitter getParent() {
        return this.parent;
    }

    public ParticleModuleSet getModules() {
        return this.modules;
    }

    public Vector3d getPosition() {
        return this.position;
    }

    public BlockPos getBlockPosition() {
        return this.blockPosition.set(this.position.x, this.position.y, this.position.z);
    }

    public Vector3d getVelocity() {
        return this.velocity;
    }

    public BlockState getBlockStateInOrUnder() {
        BlockState in = this.level.getBlockState(BlockPos.containing(this.position.x, this.position.y + 0.5, this.position.z));
        if (!in.isAir()) {
            return in;
        }

        return this.level.getBlockState(BlockPos.containing(this.position.x, this.position.y - 0.5, this.position.z));
    }

    public Vector3f getRotation() {
        return this.rotation;
    }

    public float getRadius() {
        return this.radius;
    }

    public int getAge() {
        return this.age;
    }

    public int getLifetime() {
        return this.settings.particleLifetime();
    }

    public AABB getBoundingBox() {
        return this.boundingBox;
    }

    public RenderData getRenderData() {
        return this.renderData;
    }

    public MolangEnvironment getEnvironment() {
        return this.environment.get();
    }

    public void vectorToRotation(double x, double y, double z) {
        this.rotation.set((float) Math.asin(y), (float) Math.atan2(x, z), 0);
    }

    public void setRadius(float radius) {
        this.radius = radius;
        this.updateBoundingBox();
    }

    public void setAge(int age) {
        this.age = age;
    }
}
