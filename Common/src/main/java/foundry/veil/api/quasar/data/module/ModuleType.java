package foundry.veil.api.quasar.data.module;

import com.mojang.serialization.Codec;
import foundry.veil.api.quasar.data.ParticleModuleTypeRegistry;
import foundry.veil.api.quasar.data.module.collision.CollisionSubEmitterData;
import foundry.veil.api.quasar.data.module.collision.DieOnCollisionModuleData;
import foundry.veil.api.quasar.data.module.force.*;
import foundry.veil.api.quasar.data.module.init.*;
import foundry.veil.api.quasar.data.module.render.ColorParticleModuleData;
import foundry.veil.api.quasar.data.module.render.TrailParticleModuleData;
import foundry.veil.api.quasar.data.module.update.TickSizeParticleModuleData;
import foundry.veil.api.quasar.data.module.update.TickSubEmitterModuleData;
import foundry.veil.api.quasar.emitters.module.init.InitRandomRotationModuleData;
import org.jetbrains.annotations.ApiStatus;

@FunctionalInterface
public interface ModuleType<T extends ParticleModuleData> {

    // INIT
    ModuleType<InitialVelocityModuleData> INITIAL_VELOCITY = registerInitModule("initial_velocity", InitialVelocityModuleData.CODEC);
    ModuleType<ColorParticleModuleData> INIT_COLOR = registerInitModule("init_color", ColorParticleModuleData.CODEC);
    ModuleType<InitSubEmitterModuleData> INIT_SUB_EMITTER = registerInitModule("init_sub_emitter", InitSubEmitterModuleData.CODEC);
    ModuleType<InitSizeParticleModuleData> INIT_SIZE = registerInitModule("init_size", InitSizeParticleModuleData.CODEC);
    //    ModuleType<InitRandomColorParticleModule> INIT_RANDOM_COLOR = registerInitModule("init_random_color", InitRandomColorParticleModule.CODEC);
    ModuleType<InitRandomRotationModuleData> INIT_RANDOM_ROTATION = registerInitModule("init_random_rotation", InitRandomRotationModuleData.CODEC);
    ModuleType<LightModuleData> LIGHT = registerInitModule("light", LightModuleData.CODEC);
    ModuleType<BlockParticleModuleData> BLOCK_PARTICLE = registerInitModule("block", BlockParticleModuleData.CODEC);


    // RENDER
    ModuleType<TrailParticleModuleData> TRAIL = registerRenderModule("trail", TrailParticleModuleData.CODEC);
    ModuleType<ColorParticleModuleData> COLOR = registerRenderModule("color", ColorParticleModuleData.CODEC);
//    ModuleType<ColorOverTimeParticleModule> COLOR_OVER_LIFETIME = registerRenderModule("color_over_lifetime", ColorOverTimeParticleModule.CODEC);
//    ModuleType<ColorOverVelocityParticleModule> COLOR_OVER_VELOCITY = registerRenderModule("color_over_velocity", ColorOverVelocityParticleModule.CODEC);

    // UPDATE

    ModuleType<TickSizeParticleModuleData> TICK_SIZE = registerUpdateModule("tick_size", TickSizeParticleModuleData.CODEC);
    ModuleType<TickSubEmitterModuleData> TICK_SUB_EMITTER = registerUpdateModule("tick_sub_emitter", TickSubEmitterModuleData.CODEC);

    // UPDATE - COLLISION
    ModuleType<DieOnCollisionModuleData> DIE_ON_COLLISION = registerUpdateModule("die_on_collision", DieOnCollisionModuleData.CODEC);
    ModuleType<CollisionSubEmitterData> SUB_EMITTER_COLLISION = registerUpdateModule("sub_emitter_collision", CollisionSubEmitterData.CODEC);
//    ModuleType<BounceParticleModule> BOUNCE = registerUpdateModule("bounce", BounceParticleModule.CODEC);


    // UPDATE - FORCES
    ModuleType<GravityForceData> GRAVITY = registerUpdateModule("gravity", GravityForceData.CODEC);
    ModuleType<VortexForceData> VORTEX = registerUpdateModule("vortex", VortexForceData.CODEC);
    ModuleType<PointAttractorForceData> POINT_ATTRACTOR = registerUpdateModule("point_attractor", PointAttractorForceData.CODEC);
    ModuleType<VectorFieldForceData> VECTOR_FIELD = registerUpdateModule("vector_field", VectorFieldForceData.CODEC);
    ModuleType<DragForceData> DRAG = registerUpdateModule("drag", DragForceData.CODEC);
    ModuleType<WindForceData> WIND = registerUpdateModule("wind", WindForceData.CODEC);
    ModuleType<PointForceData> POINT = registerUpdateModule("point_force", PointForceData.CODEC);

    /**
     * @return The codec for this module type data
     */
    Codec<T> codec();

    @ApiStatus.Internal
    static void bootstrap() {
    }

    @ApiStatus.Internal
    static <T extends ParticleModuleData> ModuleType<T> registerUpdateModule(String name, Codec<T> codec) {
        ModuleType<T> type = () -> codec;
        ParticleModuleTypeRegistry.registerUpdate(name, type);
        return type;
    }

    @ApiStatus.Internal
    static <T extends ParticleModuleData> ModuleType<T> registerRenderModule(String name, Codec<T> codec) {
        ModuleType<T> type = () -> codec;
        ParticleModuleTypeRegistry.registerRender(name, type);
        return type;
    }

    @ApiStatus.Internal
    static <T extends ParticleModuleData> ModuleType<T> registerInitModule(String name, Codec<T> codec) {
        ModuleType<T> type = () -> codec;
        ParticleModuleTypeRegistry.registerInit(name, type);
        return type;
    }
}
