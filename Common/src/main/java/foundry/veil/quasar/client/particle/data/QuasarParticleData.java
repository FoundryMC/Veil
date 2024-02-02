package foundry.veil.quasar.client.particle.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.quasar.client.particle.QuasarVanillaParticle;
import foundry.veil.quasar.data.module.ParticleModuleData;
import foundry.veil.quasar.emitters.ParticleEmitter;
import foundry.veil.quasar.emitters.modules.emitter.settings.ParticleSettings;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Data that is passed to each particle when it is created.
 *
 * @see QuasarVanillaParticle
 * @see foundry.veil.quasar.emitters.ParticleContext
 * <p>
 * This class is used to store all the data that is passed to each particle when it is created.
 * This includes the particle settings, whether or not the particle should collide with blocks,
 * whether or not the particle should face its velocity, and the list of sub emitters.
 * This class also stores the list of particle modules that are applied to each particle.
 * These modules are used to modify the particle's behavior.
 * The list of particle modules includes init modules, render modules, update modules, and collision modules.
 * Init modules are applied when the particle is created.
 * Render modules are applied when the particle is rendered.
 * Update modules are applied every tick.
 * Collision modules are applied when the particle collides with a block.
 * This class also stores the list of particle forces that are applied to each particle.
 * These forces are used to modify the particle's velocity.
 */
public class QuasarParticleData {

    public static final Codec<QuasarParticleData> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("should_collide", true).forGetter(QuasarParticleData::shouldCollide),
            Codec.BOOL.optionalFieldOf("face_velocity", false).forGetter(QuasarParticleData::getFaceVelocity),
            Codec.FLOAT.optionalFieldOf("velocity_stretch_factor", 0.0f).forGetter(QuasarParticleData::getVelocityStretchFactor),
            ParticleModuleData.INIT_CODEC.listOf().fieldOf("init_modules").forGetter(QuasarParticleData::getInitModules),
            ParticleModuleData.UPDATE_CODEC.listOf().fieldOf("update_modules").forGetter(QuasarParticleData::getUpdateModules),
            ParticleModuleData.UPDATE_CODEC.listOf().fieldOf("collision_modules").forGetter(QuasarParticleData::getCollisionModules),
            ParticleModuleData.UPDATE_CODEC.listOf().fieldOf("forces").forGetter(QuasarParticleData::getForceModules),
            ParticleModuleData.RENDER_CODEC.listOf().fieldOf("render_modules").forGetter(QuasarParticleData::getRenderModules),
            SpriteData.CODEC.optionalFieldOf("sprite_data", SpriteData.BLANK).forGetter(QuasarParticleData::getSpriteData),
            Codec.STRING.fieldOf("render_style").xmap(QuasarVanillaParticle.RenderStyle::valueOf, QuasarVanillaParticle.RenderStyle::name).orElse(QuasarVanillaParticle.RenderStyle.BILLBOARD).forGetter(QuasarParticleData::getRenderStyle)
    ).apply(instance, QuasarParticleData::new));

    @Deprecated
    public Holder<ParticleSettings> particleSettings;
    public boolean shouldCollide = true;
    public boolean faceVelocity = false;
    public float velocityStretchFactor = 0;
    //    private final List<ResourceLocation> subEmitters = new ArrayList<>();
    private final List<Holder<ParticleModuleData>> initModules;
    private final List<Holder<ParticleModuleData>> updateModules;
    private final List<Holder<ParticleModuleData>> collisionModules;
    private final List<Holder<ParticleModuleData>> forceModules;
    private final List<Holder<ParticleModuleData>> renderModules;
    public SpriteData spriteData;
    public QuasarVanillaParticle.RenderStyle renderStyle;
    public ParticleRenderType renderType;
    @Deprecated
    public ParticleEmitter parentEmitter;

    public QuasarParticleData(boolean shouldCollide, boolean faceVelocity, float velocityStretchFactor, List<Holder<ParticleModuleData>> initModules, List<Holder<ParticleModuleData>> updateModules, List<Holder<ParticleModuleData>> collisionModules, List<Holder<ParticleModuleData>> forceModules, List<Holder<ParticleModuleData>> renderModules, SpriteData spriteData, QuasarVanillaParticle.RenderStyle renderStyle) {
        this.shouldCollide = shouldCollide;
        this.faceVelocity = faceVelocity;
        this.velocityStretchFactor = velocityStretchFactor;
        this.initModules = initModules;
        this.updateModules = updateModules;
        this.collisionModules = collisionModules;
        this.forceModules = forceModules;
        this.renderModules = renderModules;
        this.spriteData = spriteData;
        this.renderStyle = renderStyle;
        this.renderType = new QuasarParticleRenderType().setTexture(spriteData.sprite());
    }

//    public QuasarParticleData(ParticleSettings particleSettings) {
//        this.particleSettings = particleSettings;
//    }
//
//    public QuasarParticleData(ParticleSettings particleSettings, boolean shouldCollide) {
//        this.particleSettings = particleSettings;
//        this.shouldCollide = shouldCollide;
//    }
//
//    public QuasarParticleData(ParticleSettings particleSettings, boolean shouldCollide, boolean faceVelocity) {
//        this.particleSettings = particleSettings;
//        this.shouldCollide = shouldCollide;
//        this.faceVelocity = faceVelocity;
//    }
//
//    public QuasarParticleData(ParticleSettings particleSettings, boolean shouldCollide, boolean faceVelocity, float velocityStretchFactor) {
//        this.particleSettings = particleSettings;
//        this.shouldCollide = shouldCollide;
//        this.faceVelocity = faceVelocity;
//        this.velocityStretchFactor = velocityStretchFactor;
//    }
//
//    public QuasarParticleData(boolean shouldCollide, boolean faceVelocity, float velocityStretchFactor) {
//        this.particleSettings = null;
//        this.shouldCollide = shouldCollide;
//        this.faceVelocity = faceVelocity;
//        this.velocityStretchFactor = velocityStretchFactor;
//    }
//
//    public QuasarParticleData() {
//        this(null, false, false, 0.0f);
//    }

    public ResourceLocation getRegistryId() {
        return QuasarParticleDataRegistry.getDataId(this);
    }

    public SpriteData getSpriteData() {
        return spriteData;
    }

    public QuasarVanillaParticle.RenderStyle getRenderStyle() {
        return renderStyle;
    }
//
//    @Deprecated
//    public void addInitModule(InitParticleModule module) {
//        initModules.add(module);
//    }
//
//    @Deprecated
//    public void addInitModules(InitParticleModule... modules) {
//        initModules.addAll(Arrays.asList(modules));
//    }
//
//    @Deprecated
//    public void addRenderModule(RenderParticleModule module) {
//        renderModules.add(module);
//    }
//
//    @Deprecated
//    public void addRenderModules(RenderParticleModule... modules) {
//        renderModules.addAll(Arrays.asList(modules));
//    }
//
//    @Deprecated
//    public void addUpdateModule(UpdateParticleModule module) {
//        updateModules.add(module);
//    }
//
//    @Deprecated
//    public void addUpdateModules(UpdateParticleModule... modules) {
//        updateModules.addAll(Arrays.asList(modules));
//    }
//
//    @Deprecated
//    public void addCollisionModule(CollisionParticleModule module) {
//        collisionModules.add(module);
//    }
//
//    @Deprecated
//    public void addCollisionModules(CollisionParticleModule... modules) {
//        collisionModules.addAll(Arrays.asList(modules));
//    }
//
//    @Deprecated
//    public void addForce(AbstractParticleForce force) {
//        forces.add(force);
//    }
//
//    @Deprecated
//    public void addForces(AbstractParticleForce... forces) {
//        this.forces.addAll(Arrays.asList(forces));
//    }
//
//    @Deprecated
//    public void addSubEmitter(ResourceLocation emitter) {
//        subEmitters.add(emitter);
//    }
//
//    @Deprecated
//    public void addSubEmitters(ResourceLocation... emitters) {
//        subEmitters.addAll(Arrays.asList(emitters));
//    }


    public Holder<ParticleSettings> getParticleSettings() {
        return particleSettings;
    }

    public boolean shouldCollide() {
        return shouldCollide;
    }

    public boolean getFaceVelocity() {
        return faceVelocity;
    }

    public float getVelocityStretchFactor() {
        return this.velocityStretchFactor;
    }

    public List<Holder<ParticleModuleData>> getInitModules() {
        return this.initModules;
    }

    public List<Holder<ParticleModuleData>> getUpdateModules() {
        return this.updateModules;
    }

    public List<Holder<ParticleModuleData>> getCollisionModules() {
        return this.collisionModules;
    }

    public List<Holder<ParticleModuleData>> getForceModules() {
        return this.forceModules;
    }

    public List<Holder<ParticleModuleData>> getRenderModules() {
        return this.renderModules;
    }

//    @Deprecated
//    public void removeForces(AbstractParticleForce[] forces) {
//        this.forces.removeAll(Arrays.asList(forces));
//    }

    @Deprecated
    public QuasarParticleData instance() {
//        QuasarParticleData data = new QuasarParticleData(particleSettings, shouldCollide, faceVelocity, velocityStretchFactor);
//        data.initModules = initModules.stream().filter(Objects::nonNull).map(InitParticleModule::copy).collect(Collectors.toList());
//        data.updateModules = updateModules;
//        data.renderModules = renderModules;
//        data.collisionModules = collisionModules;
//        data.forces = forces.stream().filter(Objects::nonNull).map(AbstractParticleForce::copy).collect(Collectors.toList());
//        data.spriteData = spriteData;
//        data.renderStyle = renderStyle;
//        data.renderType = renderType;
//        data.parentEmitter = parentEmitter;
//        return data;
        return this;
    }

    @Deprecated
    public void setFaceVelocity(boolean face) {
        this.faceVelocity = face;
    }
}