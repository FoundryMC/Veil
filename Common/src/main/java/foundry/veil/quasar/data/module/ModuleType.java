package foundry.veil.quasar.data.module;

import com.mojang.serialization.Codec;
import foundry.veil.quasar.data.ParticleModuleTypeRegistry;
import foundry.veil.quasar.data.module.init.LightModuleData;
import foundry.veil.quasar.emitters.modules.particle.init.*;

@FunctionalInterface
public interface ModuleType<T extends ParticleModuleData> {

    // INIT
//    ModuleType<InitialVelocityForce> INITIAL_VELOCITY = registerInitModule("initial_velocity", InitialVelocityForce.CODEC);
//    ModuleType<InitColorParticleModule> INIT_COLOR = registerInitModule("init_color", InitColorParticleModule.CODEC);
//    ModuleType<InitSubEmitter> INIT_SUB_EMITTER = registerInitModule("init_sub_emitter", InitSubEmitter.CODEC);
//    ModuleType<InitRandomColorParticleModule> INIT_RANDOM_COLOR = registerInitModule("init_random_color", InitRandomColorParticleModule.CODEC);
//    ModuleType<InitRandomRotationParticleModule> INIT_RANDOM_ROTATION = registerInitModule("init_random_rotation", InitRandomRotationParticleModule.CODEC);
    ModuleType<LightModuleData> LIGHT = registerInitModule("light", LightModuleData.CODEC);
//    ModuleType<BlockParticleModule> BLOCK_PARTICLE = registerInitModule("block", BlockParticleModule.CODEC);


    // RENDER
//    ModuleType<TrailParticleModule> TRAIL = registerRenderModule("trail", TrailParticleModule.CODEC);
//    ModuleType<ColorParticleModule> COLOR = registerRenderModule("color", ColorParticleModule.CODEC);
//    ModuleType<ColorOverTimeParticleModule> COLOR_OVER_LIFETIME = registerRenderModule("color_over_lifetime", ColorOverTimeParticleModule.CODEC);
//    ModuleType<ColorOverVelocityParticleModule> COLOR_OVER_VELOCITY = registerRenderModule("color_over_velocity", ColorOverVelocityParticleModule.CODEC);

    // UPDATE

//    ModuleType<TickSubEmitter> TICK_SUB_EMITTER = registerUpdateModule("tick_sub_emitter", TickSubEmitter.CODEC);

    // UPDATE - COLLISION
//    ModuleType<DieOnCollisionParticleModule> DIE_ON_COLLISION = registerUpdateModule("die_on_collision", DieOnCollisionParticleModule.CODEC);
//    ModuleType<SubEmitterCollisionParticleModule> SUB_EMITTER_COLLISION = registerUpdateModule("sub_emitter_collision", SubEmitterCollisionParticleModule.CODEC);
//    ModuleType<BounceParticleModule> BOUNCE = registerUpdateModule("bounce", BounceParticleModule.CODEC);


    // UPDATE - FORCES
//    ModuleType<GravityForce> GRAVITY = registerUpdateModule("gravity", GravityForce.CODEC);
//    ModuleType<VortexForce> VORTEX = registerUpdateModule("vortex", VortexForce.CODEC);
//    ModuleType<PointAttractorForce> POINT_ATTRACTOR = registerUpdateModule("point_attractor", PointAttractorForce.CODEC);
//    ModuleType<VectorFieldForce> VECTOR_FIELD = registerUpdateModule("vector_field", VectorFieldForce.CODEC);
//    ModuleType<DragForce> DRAG = registerUpdateModule("drag", DragForce.CODEC);
//    ModuleType<WindForce> WIND = registerUpdateModule("wind", WindForce.CODEC);
//    ModuleType<PointForce> POINT = registerUpdateModule("point_force", PointForce.CODEC);

    /**
     * @return The codec for this module type data
     */
    Codec<T> codec();

    static <T extends ParticleModuleData> ModuleType<T> registerUpdateModule(String name, Codec<T> codec) {
        ModuleType<T> type = () -> codec;
        ParticleModuleTypeRegistry.registerUpdate(name, type);
        return type;
    }

    static <T extends ParticleModuleData> ModuleType<T> registerRenderModule(String name, Codec<T> codec) {
        ModuleType<T> type = () -> codec;
        ParticleModuleTypeRegistry.registerRender(name, type);
        return type;
    }

    static <T extends ParticleModuleData> ModuleType<T> registerInitModule(String name, Codec<T> codec) {
        ModuleType<T> type = () -> codec;
        ParticleModuleTypeRegistry.registerInit(name, type);
        return type;
    }
}
