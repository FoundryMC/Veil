package foundry.veil.api.quasar.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import foundry.veil.Veil;
import foundry.veil.api.quasar.data.module.ModuleType;
import foundry.veil.api.quasar.data.module.ParticleModuleData;
import foundry.veil.api.quasar.data.module.collision.CollisionSubEmitterData;
import foundry.veil.api.quasar.data.module.collision.DieOnCollisionModuleData;
import foundry.veil.api.quasar.data.module.force.*;
import foundry.veil.api.quasar.data.module.init.*;
import foundry.veil.api.quasar.data.module.render.ColorParticleModuleData;
import foundry.veil.api.quasar.data.module.render.TrailParticleModuleData;
import foundry.veil.api.quasar.data.module.update.TickSizeParticleModuleData;
import foundry.veil.api.quasar.data.module.update.TickSubEmitterModuleData;
import foundry.veil.api.quasar.emitters.module.init.InitRandomRotationModuleData;
import foundry.veil.api.quasar.emitters.module.update.VectorField;
import foundry.veil.api.util.CodecUtil;
import foundry.veil.api.util.FastNoiseLite;
import foundry.veil.impl.quasar.ColorGradient;
import foundry.veil.platform.registry.RegistrationProvider;
import gg.moonflower.molangcompiler.api.MolangExpression;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.function.Supplier;

public class ParticleModuleTypeRegistry {

    public static final ResourceKey<Registry<ModuleType<?>>> REGISTRY_KEY = ResourceKey.createRegistryKey(Veil.veilPath("quasar/module_type"));

    private static final RegistrationProvider<ModuleType<?>> PROVIDER = RegistrationProvider.get(
            REGISTRY_KEY,
            Veil.MODID);

    /**
     * @deprecated Use {@link #REGISTRY} instead
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    @Deprecated
    private static final RegistrationProvider<ModuleType<?>> INIT_MODULES_PROVIDER = PROVIDER;
    /**
     * @deprecated Use {@link #REGISTRY} instead
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    @Deprecated
    private static final RegistrationProvider<ModuleType<?>> UPDATE_MODULES_PROVIDER = PROVIDER;
    /**
     * @deprecated Use {@link #REGISTRY} instead
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    @Deprecated
    private static final RegistrationProvider<ModuleType<?>> RENDER_MODULES_PROVIDER = PROVIDER;

    public static final Registry<ModuleType<? extends ParticleModuleData>> REGISTRY = PROVIDER.asVanillaRegistry();

    /**
     * @deprecated Use {@link #REGISTRY} instead
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    @Deprecated
    public static final Registry<ModuleType<? extends ParticleModuleData>> INIT_MODULES_REGISTRY = REGISTRY;
    /**
     * @deprecated Use {@link #REGISTRY} instead
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    @Deprecated
    public static final Registry<ModuleType<? extends ParticleModuleData>> UPDATE_MODULES_REGISTRY = REGISTRY;
    /**
     * @deprecated Use {@link #REGISTRY} instead
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    @Deprecated
    public static final Registry<ModuleType<? extends ParticleModuleData>> RENDER_MODULES_REGISTRY = REGISTRY;

    public static final Codec<ModuleType<?>> CODEC = CodecUtil.registryOrLegacyCodec(REGISTRY);

    /**
     * @deprecated Use {@link #CODEC} instead
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    @Deprecated
    public static final Codec<ModuleType<?>> INIT_MODULE_CODEC = CODEC;
    /**
     * @deprecated Use {@link #CODEC} instead
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    @Deprecated
    public static final Codec<ModuleType<?>> UPDATE_MODULE_CODEC = CODEC;
    /**
     * @deprecated Use {@link #CODEC} instead
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    @Deprecated
    public static final Codec<ModuleType<?>> RENDER_MODULE_CODEC = CODEC;

    // INIT
    public static final ModuleType<InitialVelocityModuleData> INITIAL_VELOCITY = registerModule("initial_velocity", InitialVelocityModuleData.CODEC, () -> new InitialVelocityModuleData(new Vector3d(0, 1, 0), false, 1.0F));
    public static final ModuleType<InitSubEmitterModuleData> INIT_SUB_EMITTER = registerModule("init_sub_emitter", InitSubEmitterModuleData.CODEC, () -> new InitSubEmitterModuleData(ResourceLocation.withDefaultNamespace("")));
    /**
     * @deprecated Use {@link ParticleSettings#randomSize()} instead
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    @Deprecated
    public static final ModuleType<InitSizeParticleModuleData> INIT_SIZE = registerDeprecatedModule(
            "init_size",
            InitSizeParticleModuleData.CODEC,
            () -> new InitSizeParticleModuleData(MolangExpression.of(1.0F)),
            "Use \"random_size\" instead",
            "5.0.0");
    //    ModuleType<InitRandomColorParticleModule> INIT_RANDOM_COLOR = registerInitModule("init_random_color", InitRandomColorParticleModule.CODEC);
    /**
     * @deprecated Use {@link ParticleSettings#randomInitialRotation()} instead
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    @Deprecated
    public static final ModuleType<InitRandomRotationModuleData> INIT_RANDOM_ROTATION = registerDeprecatedModule(
            "init_random_rotation",
            InitRandomRotationModuleData.CODEC,
            () -> new InitRandomRotationModuleData(new Vector3f(-180, -180, -180), new Vector3f(180, 180, 180)),
            "Use \"random_initial_rotation\" instead",
            "5.0.0");
    public static final ModuleType<LightModuleData> LIGHT = registerModule("light", LightModuleData.CODEC, () -> new LightModuleData(new ColorGradient(1, 1, 1, 1), MolangExpression.of(1.0F), MolangExpression.of(1.0F)));
    public static final ModuleType<LightmapParticleModuleData> LIGHTMAP = registerModule("lightmap", LightmapParticleModuleData.CODEC, () -> new LightmapParticleModuleData(15728880));
    public static final ModuleType<BlockParticleModuleData> BLOCK_PARTICLE = registerModule("block", BlockParticleModuleData.CODEC, () -> new BlockParticleModuleData(false));

    // RENDER
    public static final ModuleType<TrailParticleModuleData> TRAIL = registerModule("trail", TrailParticleModuleData.CODEC, () -> new TrailParticleModuleData(new ArrayList<>()));
    public static final ModuleType<ColorParticleModuleData> COLOR = registerModule("color", ColorParticleModuleData.CODEC, () -> new ColorParticleModuleData(new ColorGradient(1, 1, 1, 1), MolangExpression.ZERO));
    //    ModuleType<ColorOverTimeParticleModule> COLOR_OVER_LIFETIME = registerRenderModule("color_over_lifetime", ColorOverTimeParticleModule.CODEC);
    //    ModuleType<ColorOverVelocityParticleModule> COLOR_OVER_VELOCITY = registerRenderModule("color_over_velocity", ColorOverVelocityParticleModule.CODEC);

    // UPDATE
    public static final ModuleType<TickSizeParticleModuleData> TICK_SIZE = registerModule("size", TickSizeParticleModuleData.CODEC, () -> new TickSizeParticleModuleData(MolangExpression.of(1)));
    public static final ModuleType<TickSubEmitterModuleData> TICK_SUB_EMITTER = registerModule("tick_sub_emitter", TickSubEmitterModuleData.CODEC, () -> new TickSubEmitterModuleData(ResourceLocation.withDefaultNamespace(""), 5));
    // UPDATE - COLLISION
    public static final ModuleType<DieOnCollisionModuleData> DIE_ON_COLLISION = registerModule("die_on_collision", DieOnCollisionModuleData.CODEC, DieOnCollisionModuleData::new);
    public static final ModuleType<CollisionSubEmitterData> SUB_EMITTER_COLLISION = registerModule("sub_emitter_collision", CollisionSubEmitterData.CODEC, () -> new CollisionSubEmitterData(ResourceLocation.withDefaultNamespace("")));
    //    ModuleType<BounceParticleModule> BOUNCE = registerUpdateModule("bounce", BounceParticleModule.CODEC);
    // UPDATE - FORCES
    public static final ModuleType<GravityForceData> GRAVITY = registerModule("gravity", GravityForceData.CODEC, () -> new GravityForceData(1F));
    public static final ModuleType<VortexForceData> VORTEX = registerModule("vortex", VortexForceData.CODEC, () -> new VortexForceData(new Vector3d(0, 1, 0), new Vector3d(0), true, 10, 1.0f));
    public static final ModuleType<PointAttractorForceData> POINT_ATTRACTOR = registerModule("point_attractor", PointAttractorForceData.CODEC, () -> new PointAttractorForceData(new Vector3d(0), true, 20, 1.0f, false, false));
    public static final ModuleType<VectorFieldForceData> VECTOR_FIELD = registerModule("vector_field", VectorFieldForceData.CODEC, () -> new VectorFieldForceData(new VectorField(new FastNoiseLite(), 1.0F), 1.0F));
    public static final ModuleType<DragForceData> DRAG = registerModule("drag", DragForceData.CODEC, () -> new DragForceData(0.5f));
    public static final ModuleType<WindForceData> WIND = registerModule("wind", WindForceData.CODEC, () -> new WindForceData(new Vector3d(1, 0, 0), 0.5f, 1.0f));
    public static final ModuleType<PointForceData> POINT = registerModule("point_force", PointForceData.CODEC, () -> new PointForceData(new Vector3d(0), true, 20, 1.0f));

    @ApiStatus.Internal
    public static void bootstrap() {
    }

    private static <T extends ParticleModuleData> ModuleType<T> registerModule(String name, MapCodec<T> codec, Supplier<T> defaultValue) {
        ModuleType<T> type = new ModuleType<>() {
            @Override
            public MapCodec<T> codec() {
                return codec;
            }

            @Override
            public Supplier<T> defaultValue() {
                return defaultValue;
            }
        };
        PROVIDER.register(name, () -> type);
        return type;
    }

    @Deprecated
    private static <T extends ParticleModuleData> ModuleType<T> registerDeprecatedModule(
            String name,
            MapCodec<T> codec,
            Supplier<T> defaultValue,
            String reason,
            String removeVersion) {
        ModuleType.DeprecationStatus status = new ModuleType.DeprecationStatus(reason, removeVersion);
        ModuleType<T> type = new ModuleType<>() {
            @Override
            public MapCodec<T> codec() {
                return codec;
            }

            @Override
            public Supplier<T> defaultValue() {
                return defaultValue;
            }

            @Override
            public DeprecationStatus deprecationStatus() {
                return status;
            }
        };
        PROVIDER.register(name, () -> type);
        return type;
    }
}