package foundry.veil.quasar.emitters.modules;

import foundry.veil.quasar.emitters.modules.particle.init.*;
import foundry.veil.quasar.emitters.modules.particle.init.forces.InitialVelocityForce;
import foundry.veil.quasar.emitters.modules.particle.init.LightModule;
import foundry.veil.quasar.emitters.modules.particle.render.RenderModuleRegistry;
import foundry.veil.quasar.emitters.modules.particle.render.TrailParticleModule;
import foundry.veil.quasar.emitters.modules.particle.render.color.ColorParticleModule;
import foundry.veil.quasar.emitters.modules.particle.render.color.ColorOverTimeParticleModule;
import foundry.veil.quasar.emitters.modules.particle.render.color.ColorOverVelocityParticleModule;
import foundry.veil.quasar.emitters.modules.particle.update.BounceParticleModule;
import foundry.veil.quasar.emitters.modules.particle.update.TickSubEmitter;
import foundry.veil.quasar.emitters.modules.particle.update.UpdateModuleRegistry;
import foundry.veil.quasar.emitters.modules.particle.update.collsion.DieOnCollisionParticleModule;
import foundry.veil.quasar.emitters.modules.particle.update.collsion.SubEmitterCollisionParticleModule;
import foundry.veil.quasar.emitters.modules.particle.update.forces.*;
import com.mojang.serialization.Codec;

public interface ModuleType<T extends ParticleModule> {

    // INIT
    ModuleType<InitialVelocityForce> INITIAL_VELOCITY = registerInitModule("initial_velocity", InitialVelocityForce.CODEC);
    ModuleType<InitColorParticleModule> INIT_COLOR = registerInitModule("init_color", InitColorParticleModule.CODEC);
    ModuleType<InitSubEmitter> INIT_SUB_EMITTER = registerInitModule("init_sub_emitter", InitSubEmitter.CODEC);
    ModuleType<InitRandomColorParticleModule> INIT_RANDOM_COLOR = registerInitModule("init_random_color", InitRandomColorParticleModule.CODEC);
    ModuleType<InitRandomRotationParticleModule> INIT_RANDOM_ROTATION = registerInitModule("init_random_rotation", InitRandomRotationParticleModule.CODEC);
    ModuleType<LightModule> LIGHT_MODULE = registerInitModule("light", LightModule.CODEC);


    // RENDER
    ModuleType<TrailParticleModule> TRAIL = registerRenderModule("trail", TrailParticleModule.CODEC);
    ModuleType<ColorParticleModule> COLOR = registerRenderModule("color", ColorParticleModule.CODEC);
    ModuleType<ColorOverTimeParticleModule> COLOR_OVER_LIFETIME = registerRenderModule("color_over_lifetime", ColorOverTimeParticleModule.CODEC);
    ModuleType<ColorOverVelocityParticleModule> COLOR_OVER_VELOCITY = registerRenderModule("color_over_velocity", ColorOverVelocityParticleModule.CODEC);

    // UPDATE

    ModuleType<TickSubEmitter> TICK_SUB_EMITTER = registerUpdateModule("tick_sub_emitter", TickSubEmitter.CODEC);

    // UPDATE - COLLISION
    ModuleType<DieOnCollisionParticleModule> DIE_ON_COLLISION = registerUpdateModule("die_on_collision", DieOnCollisionParticleModule.CODEC);
    ModuleType<SubEmitterCollisionParticleModule> SUB_EMITTER_COLLISION = registerUpdateModule("sub_emitter_collision", SubEmitterCollisionParticleModule.CODEC);
    ModuleType<BounceParticleModule> BOUNCE = registerUpdateModule("bounce", BounceParticleModule.CODEC);


    // UPDATE - FORCES
    ModuleType<GravityForce> GRAVITY = registerUpdateModule("gravity", GravityForce.CODEC);
    ModuleType<VortexForce> VORTEX = registerUpdateModule("vortex", VortexForce.CODEC);
    ModuleType<PointAttractorForce> POINT_ATTRACTOR = registerUpdateModule("point_attractor", PointAttractorForce.CODEC);
    ModuleType<VectorFieldForce> VECTOR_FIELD = registerUpdateModule("vector_field", VectorFieldForce.CODEC);
    ModuleType<DragForce> DRAG = registerUpdateModule("drag", DragForce.CODEC);
    ModuleType<WindForce> WIND = registerUpdateModule("wind", WindForce.CODEC);
    ModuleType<PointForce> POINT = registerUpdateModule("point_force", PointForce.CODEC);
    Codec<T> getCodec();

    static void bootstrap() {
        UpdateModuleRegistry.bootstrap();
        RenderModuleRegistry.bootstrap();
        InitModuleRegistry.bootstrap();
    }

    static <T extends ParticleModule> ModuleType<T> registerUpdateModule(String name, Codec<T> codec) {
        ModuleType<T> type = () -> codec;
        UpdateModuleRegistry.register(name, type);
        return type;
    }

    static <T extends ParticleModule> ModuleType<T> registerRenderModule(String name, Codec<T> codec) {
        ModuleType<T> type = () -> codec;
        RenderModuleRegistry.register(name, type);
        return type;
    }

    static <T extends ParticleModule> ModuleType<T> registerInitModule(String name, Codec<T> codec) {
        ModuleType<T> type = () -> codec;
        InitModuleRegistry.register(name, type);
        return type;
    }
}
