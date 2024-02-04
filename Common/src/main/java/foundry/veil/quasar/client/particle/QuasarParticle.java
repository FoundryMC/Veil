package foundry.veil.quasar.client.particle;

import com.mojang.logging.LogUtils;
import foundry.veil.quasar.data.ParticleSettings;
import foundry.veil.quasar.data.QuasarParticleData;
import foundry.veil.quasar.data.module.ParticleModuleData;
import foundry.veil.quasar.emitters.ParticleEmitter;
import foundry.veil.quasar.emitters.modules.particle.*;
import gg.moonflower.molangcompiler.api.MolangEnvironment;
import gg.moonflower.molangcompiler.api.MolangExpression;
import gg.moonflower.molangcompiler.api.MolangRuntime;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;

public class QuasarParticle {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<Holder<ParticleModuleData>> REPORTED_MODULES = new HashSet<>();

    private final ClientLevel level;
    private final RandomSource randomSource;
    private final QuasarParticleData data;
    private final ParticleSettings settings;
    private final ParticleEmitter parent;
    private final ParticleModuleSet modules;
    private final Vector3d position;
    private final Vector3d velocity;
    private final Vector3f rotation;
    private final BlockPos.MutableBlockPos blockPosition;
    private float scale;
    private final int lifetime;
    private int age;

    private final MolangRuntime environment;
    private final RenderData renderData;

    public QuasarParticle(ClientLevel level, QuasarParticleData data, ParticleSettings settings, @Nullable ParticleEmitter parent) {
        this.level = level;
        this.randomSource = RandomSource.create();
        this.data = data;
        this.settings = settings;
        this.parent = parent;
        this.modules = QuasarParticle.createModuleSet(data);
        this.position = new Vector3d();
        this.velocity = new Vector3d();
        this.rotation = new Vector3f();
        this.blockPosition = new BlockPos.MutableBlockPos();
        this.scale = settings.particleSize(this.randomSource);
        this.lifetime = settings.particleLifetime(this.randomSource);
        this.age = 0;

        this.renderData = new RenderData();
        this.environment = MolangRuntime.runtime()
                .loadLibrary("quasar", new QuasarParticleLibrary(this))
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
                .setQuery("scale", MolangExpression.of(this.renderData::getRenderScale))
                .setQuery("age", MolangExpression.of(this.renderData::getRenderAge))
                .setQuery("agePercent", MolangExpression.of(this.renderData::getAgePercent))
                .setQuery("lifetime", this.lifetime)
                .create();
        if (this.parent != null) {
            this.parent.particleAdded();
        }
    }

    @ApiStatus.Internal
    public void init() {
        for (InitParticleModule initModule : this.modules.getInitModules()) {
            initModule.init(this);
        }
        this.renderData.tick(this.position, this.rotation, this.scale);
    }

    @ApiStatus.Internal
    public static void clearErrors() {
        REPORTED_MODULES.clear();
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
        return builder.build();
    }

    public void tick() {
        this.renderData.tick(this.position, this.rotation, this.scale);
        for (UpdateParticleModule updateModule : this.modules.getUpdateModules()) {
            updateModule.update(this);
        }
        // TODO properly do forces
        for (ForceParticleModule updateModule : this.modules.getForceModules()) {
            updateModule.applyForce(this);
        }

        this.age++;
        if (this.age >= this.lifetime) {
            this.remove();
        }
    }

    public void render(float partialTicks) {
        for (RenderParticleModule renderModule : this.modules.getRenderModules()) {
            renderModule.render(this, partialTicks);
        }
        this.renderData.render(this.position, this.rotation, this.scale, this.age, this.lifetime, partialTicks);
    }

    public void onRemove() {
        this.parent.particleRemoved();
        for (ParticleModule module : this.modules.getAllModules()) {
            module.onRemove();
        }
    }

    public void remove() {
        this.age = Integer.MAX_VALUE;
    }

    public boolean isRemoved() {
        return this.age == Integer.MAX_VALUE;
    }

    public ClientLevel getLevel() {
        return this.level;
    }

    public RandomSource getRandomSource() {
        return this.randomSource;
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

    public float getScale() {
        return this.scale;
    }

    public int getAge() {
        return this.age;
    }

    public int getLifetime() {
        return this.settings.particleLifetime();
    }

    public RenderData getRenderData() {
        return this.renderData;
    }

    public MolangEnvironment getEnvironment() {
        return this.environment;
    }

    public void vectorToRotation(double x, double y, double z) {
        this.rotation.set((float) Math.asin(y), (float) Math.atan2(x, z), 0);
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
