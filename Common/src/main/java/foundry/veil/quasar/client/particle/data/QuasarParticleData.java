package foundry.veil.quasar.client.particle.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.quasar.client.particle.QuasarVanillaParticle;
import foundry.veil.quasar.data.QuasarParticles;
import foundry.veil.quasar.data.module.ParticleModuleData;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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
public record QuasarParticleData(boolean shouldCollide,
                                 boolean faceVelocity,
                                 float velocityStretchFactor,
                                 List<Holder<ParticleModuleData>> initModules,
                                 List<Holder<ParticleModuleData>> updateModules,
                                 List<Holder<ParticleModuleData>> collisionModules,
                                 List<Holder<ParticleModuleData>> forceModules,
                                 List<Holder<ParticleModuleData>> renderModules,
                                 @Nullable SpriteData spriteData,
                                 QuasarVanillaParticle.RenderStyle renderStyle) {

    public static final Codec<QuasarParticleData> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("should_collide", true).forGetter(QuasarParticleData::shouldCollide),
            Codec.BOOL.optionalFieldOf("face_velocity", false).forGetter(QuasarParticleData::faceVelocity),
            Codec.FLOAT.optionalFieldOf("velocity_stretch_factor", 0.0F).forGetter(QuasarParticleData::velocityStretchFactor),
            ParticleModuleData.INIT_CODEC.listOf().optionalFieldOf("init_modules", Collections.emptyList()).forGetter(QuasarParticleData::initModules),
            ParticleModuleData.UPDATE_CODEC.listOf().optionalFieldOf("update_modules", Collections.emptyList()).forGetter(QuasarParticleData::updateModules),
            ParticleModuleData.UPDATE_CODEC.listOf().optionalFieldOf("collision_modules", Collections.emptyList()).forGetter(QuasarParticleData::collisionModules),
            ParticleModuleData.UPDATE_CODEC.listOf().optionalFieldOf("forces", Collections.emptyList()).forGetter(QuasarParticleData::forceModules),
            ParticleModuleData.RENDER_CODEC.listOf().optionalFieldOf("render_modules", Collections.emptyList()).forGetter(QuasarParticleData::renderModules),
            SpriteData.CODEC.optionalFieldOf("sprite_data").forGetter(data -> Optional.ofNullable(data.spriteData())),
            Codec.STRING.fieldOf("render_style")
                    .xmap(QuasarVanillaParticle.RenderStyle::valueOf, QuasarVanillaParticle.RenderStyle::name)
                    .orElse(QuasarVanillaParticle.RenderStyle.BILLBOARD)
                    .forGetter(QuasarParticleData::renderStyle)
    ).apply(instance, (shouldCollide, faceVelocity, velocityStretchFactor, initModules, updateModules, collisionModules, forceModules, renderModules, spriteData, renderStyle) -> new QuasarParticleData(shouldCollide, faceVelocity, velocityStretchFactor, initModules, updateModules, collisionModules, forceModules, renderModules, spriteData.orElse(null), renderStyle)));
    public static final Codec<Holder<QuasarParticleData>> CODEC = RegistryFileCodec.create(QuasarParticles.PARTICLE_DATA, DIRECT_CODEC);

    /**
     * @return A stream containing all modules in the particle.
     */
    public Stream<Holder<ParticleModuleData>> allModules() {
        Stream.Builder<Holder<ParticleModuleData>> builder = Stream.builder();
        for (Holder<ParticleModuleData> initModule : this.initModules) {
            builder.add(initModule);
        }
        for (Holder<ParticleModuleData> initModule : this.updateModules) {
            builder.add(initModule);
        }
        for (Holder<ParticleModuleData> initModule : this.collisionModules) {
            builder.add(initModule);
        }
        for (Holder<ParticleModuleData> initModule : this.forceModules) {
            builder.add(initModule);
        }
        for (Holder<ParticleModuleData> initModule : this.renderModules) {
            builder.add(initModule);
        }
        return builder.build();
    }

    public ResourceLocation getRegistryId() {
        return QuasarParticles.registryAccess().registryOrThrow(QuasarParticles.PARTICLE_DATA).getKey(this);
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
}