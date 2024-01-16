package foundry.veil.quasar.client.particle.data;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.emitters.ICustomParticleData;
import foundry.veil.quasar.emitters.ParticleEmitter;
import foundry.veil.quasar.emitters.modules.emitter.settings.EmissionParticleSettings;
import foundry.veil.quasar.emitters.modules.particle.init.InitParticleModule;
import foundry.veil.quasar.emitters.modules.particle.init.InitModuleRegistry;
import foundry.veil.quasar.emitters.modules.particle.render.RenderParticleModule;
import foundry.veil.quasar.emitters.modules.particle.render.RenderModuleRegistry;
import foundry.veil.quasar.emitters.modules.particle.update.UpdateParticleModule;
import foundry.veil.quasar.emitters.modules.particle.update.UpdateModuleRegistry;
import foundry.veil.quasar.emitters.modules.particle.update.collsion.CollisionParticleModule;
import foundry.veil.quasar.emitters.modules.particle.update.forces.AbstractParticleForce;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static foundry.veil.platform.registry.ParticleTypeRegistry.QUASAR_BASE;

/**
 * Data that is passed to each particle when it is created.
 *
 * @see ICustomParticleData
 * @see QuasarParticle
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
public class QuasarParticleData implements ICustomParticleData<QuasarParticleData>, ParticleOptions {
    public static final Codec<QuasarParticleData> CODEC = RecordCodecBuilder.create(i ->
            i.group(
                    Codec.BOOL.optionalFieldOf("should_collide", true).forGetter(QuasarParticleData::shouldCollide),
                    Codec.BOOL.optionalFieldOf("face_velocity", false).forGetter(QuasarParticleData::getFaceVelocity),
                    Codec.FLOAT.optionalFieldOf("velocity_stretch_factor", 0.0f).forGetter(QuasarParticleData::getVelocityStretchFactor),
                    ResourceLocation.CODEC.listOf().fieldOf("init_modules").xmap(
                            r -> r.stream().map(InitModuleRegistry::getModule).collect(Collectors.toList()),
                            r -> r.stream().map(InitModuleRegistry::getModuleId).collect(Collectors.toList())
                    ).forGetter(QuasarParticleData::getInitModules),
                    ResourceLocation.CODEC.listOf().fieldOf("update_modules").xmap(
                            r -> r.stream().map(UpdateModuleRegistry::getModule).collect(Collectors.toList()),
                            r -> r.stream().map(UpdateModuleRegistry::getModuleId).collect(Collectors.toList())
                    ).forGetter(QuasarParticleData::getUpdateModules),
                    ResourceLocation.CODEC.listOf().fieldOf("collision_modules").orElse(List.of()).xmap(
                            r -> {
                                return r.stream().map(f -> (CollisionParticleModule) UpdateModuleRegistry.getModule(f)).collect(Collectors.toList());
                            },
                            r -> {
                                return r.stream().map(f -> UpdateModuleRegistry.getModuleId((CollisionParticleModule) f)).collect(Collectors.toList());
                            }
                    ).forGetter(QuasarParticleData::getCollisionModules),
                    ResourceLocation.CODEC.listOf().fieldOf("forces").xmap(
                            r -> {
                                return r.stream().map(f -> (AbstractParticleForce) UpdateModuleRegistry.getModule(f)).collect(Collectors.toList());
                            },
                            r -> {
                                return r.stream().map(f -> UpdateModuleRegistry.getModuleId((UpdateParticleModule) f)).collect(Collectors.toList());
                            }
                    ).forGetter(QuasarParticleData::getForces),
                    ResourceLocation.CODEC.listOf().fieldOf("render_modules").xmap(
                            r -> r.stream().map(RenderModuleRegistry::getModule).collect(Collectors.toList()),
                            r -> r.stream().map(RenderModuleRegistry::getModuleId).collect(Collectors.toList())
                    ).forGetter(QuasarParticleData::getRenderModules),
                    SpriteData.CODEC.fieldOf("sprite_data").orElse(SpriteData.BLANK).forGetter(QuasarParticleData::getSpriteData),
                    Codec.STRING.fieldOf("render_style").orElse("BILLBOARD").xmap(QuasarParticle.RenderStyle::valueOf, QuasarParticle.RenderStyle::name).forGetter(QuasarParticleData::getRenderStyle)
            ).apply(i, (shouldCollide, faceVelocity, velocityStretchFactor, initModules, updateModules, collisionModules, forces, renderModules, spriteData, style) -> {
                        QuasarParticleData data = new QuasarParticleData(shouldCollide, faceVelocity, velocityStretchFactor);
                        data.initModules = initModules;
                        data.updateModules = updateModules;
                        data.collisionModules = collisionModules;
                        data.renderModules = renderModules;
                        data.forces = forces;
                        data.spriteData = spriteData;
                        data.renderStyle = style;
                        data.renderType = new QuasarParticleRenderType().setTexture(spriteData.getSprite());
                return data;
                    }
            )
    );

    public ResourceLocation registryId;
    public SpriteData spriteData;
    public QuasarParticle.RenderStyle renderStyle;
    public EmissionParticleSettings particleSettings;
    public boolean shouldCollide = true;
    public boolean faceVelocity = false;
    public float velocityStretchFactor = 0;
    public List<ResourceLocation> subEmitters = new ArrayList<>();
    public List<AbstractParticleForce> forces = new ArrayList<>();
    public List<InitParticleModule> initModules = new ArrayList<>();
    public List<RenderParticleModule> renderModules = new ArrayList<>();
    public List<UpdateParticleModule> updateModules = new ArrayList<>();
    public List<CollisionParticleModule> collisionModules = new ArrayList<>();
    public ParticleRenderType renderType;
    public ParticleEmitter parentEmitter;


    public QuasarParticleData(EmissionParticleSettings particleSettings) {
        this.particleSettings = particleSettings;
    }

    public QuasarParticleData(EmissionParticleSettings particleSettings, boolean shouldCollide) {
        this.particleSettings = particleSettings;
        this.shouldCollide = shouldCollide;
    }

    public QuasarParticleData(EmissionParticleSettings particleSettings, boolean shouldCollide, boolean faceVelocity) {
        this.particleSettings = particleSettings;
        this.shouldCollide = shouldCollide;
        this.faceVelocity = faceVelocity;
    }

    public QuasarParticleData(EmissionParticleSettings particleSettings, boolean shouldCollide, boolean faceVelocity, float velocityStretchFactor) {
        this.particleSettings = particleSettings;
        this.shouldCollide = shouldCollide;
        this.faceVelocity = faceVelocity;
        this.velocityStretchFactor = velocityStretchFactor;
    }

    public QuasarParticleData(boolean shouldCollide, boolean faceVelocity, float velocityStretchFactor) {
        this.particleSettings = null;
        this.shouldCollide = shouldCollide;
        this.faceVelocity = faceVelocity;
        this.velocityStretchFactor = velocityStretchFactor;
    }

    public QuasarParticleData() {
        this(null, false, false, 0.0f);
    }

    public ResourceLocation getRegistryId() {
        return registryId;
    }

    public SpriteData getSpriteData() {
        return spriteData;
    }

    public QuasarParticle.RenderStyle getRenderStyle() {
        return renderStyle;
    }

    public void addInitModule(InitParticleModule module) {
        initModules.add(module);
    }

    public void addInitModules(InitParticleModule... modules) {
        initModules.addAll(Arrays.asList(modules));
    }

    public void addRenderModule(RenderParticleModule module) {
        renderModules.add(module);
    }

    public void addRenderModules(RenderParticleModule... modules) {
        renderModules.addAll(Arrays.asList(modules));
    }

    public void addUpdateModule(UpdateParticleModule module) {
        updateModules.add(module);
    }

    public void addUpdateModules(UpdateParticleModule... modules) {
        updateModules.addAll(Arrays.asList(modules));
    }

    public void addCollisionModule(CollisionParticleModule module) {
        collisionModules.add(module);
    }

    public void addCollisionModules(CollisionParticleModule... modules) {
        collisionModules.addAll(Arrays.asList(modules));
    }

    public void addForce(AbstractParticleForce force) {
        forces.add(force);
    }

    public void addForces(AbstractParticleForce... forces) {
        this.forces.addAll(Arrays.asList(forces));
    }

    public void addSubEmitter(ResourceLocation emitter) {
        subEmitters.add(emitter);
    }

    public void addSubEmitters(ResourceLocation... emitters) {
        subEmitters.addAll(Arrays.asList(emitters));
    }


    public EmissionParticleSettings getParticleSettings() {
        return particleSettings;
    }

    public void setParticleSettings(EmissionParticleSettings particleSettings) {
        this.particleSettings = particleSettings;
    }

    public boolean shouldCollide() {
        return shouldCollide;
    }

    public boolean getFaceVelocity() {
        return faceVelocity;
    }

    public float getVelocityStretchFactor() {
        return velocityStretchFactor;
    }

    public List<ResourceLocation> getSubEmitters() {
        return subEmitters;
    }

    public List<AbstractParticleForce> getForces() {
        return forces;
    }

    public List<InitParticleModule> getInitModules() {
        return initModules;
    }

    public List<RenderParticleModule> getRenderModules() {
        return renderModules;
    }

    public List<UpdateParticleModule> getUpdateModules() {
        return updateModules;
    }

    public List<CollisionParticleModule> getCollisionModules() {
        return collisionModules;
    }








    /*
     * MOJANG SHIT
     */

    @Override
    public Codec<QuasarParticleData> getCodec(ParticleType<QuasarParticleData> type) {
        return CODEC;
    }


    @Override
    public ParticleType<? extends ParticleOptions> getType() {
        return QUASAR_BASE.get();
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer) {
    }

    @Override
    public String writeToString() {
        return "";
    }

    @Override
    public Deserializer<QuasarParticleData> getDeserializer() {
        return DESERIALIZER;
    }

    public static final Deserializer<QuasarParticleData> DESERIALIZER = new Deserializer<QuasarParticleData>() {
        @Override
        public QuasarParticleData fromCommand(ParticleType<QuasarParticleData> type, StringReader reader) throws CommandSyntaxException {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().create("Don't use this");
        }

        @Override
        public QuasarParticleData fromNetwork(ParticleType<QuasarParticleData> type, FriendlyByteBuf buffer) {
            return new QuasarParticleData(null);
        }
    };

    public void removeForces(AbstractParticleForce[] forces) {
        this.forces.removeAll(Arrays.asList(forces));
    }

    public QuasarParticleData instance() {
        QuasarParticleData data = new QuasarParticleData(particleSettings, shouldCollide, faceVelocity, velocityStretchFactor);
        data.initModules = initModules.stream().map(InitParticleModule::copy).filter(Objects::nonNull).collect(Collectors.toList());
        data.updateModules = updateModules;
        data.renderModules = renderModules;
        data.collisionModules = collisionModules;
        data.forces = forces.stream().map(AbstractParticleForce::copy).filter(Objects::nonNull).map(s -> (AbstractParticleForce) s).collect(Collectors.toList());
        data.registryId = registryId;
        data.spriteData = spriteData;
        data.renderStyle = renderStyle;
        data.renderType = renderType;
        data.parentEmitter = parentEmitter;
        return data;
    }

    public void setFaceVelocity(boolean face) {
        this.faceVelocity = face;
    }
}