package foundry.veil.api.quasar.data;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import foundry.veil.Veil;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.quasar.data.module.ParticleModuleData;
import foundry.veil.api.quasar.particle.ParticleEmitter;
import foundry.veil.mixin.client.quasar.RegistryDataAccessor;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class QuasarParticles {

    public static final ResourceKey<Registry<ParticleModuleData>> INIT_MODULES = createRegistryKey("quasar/modules/init");
    public static final ResourceKey<Registry<ParticleModuleData>> UPDATE_MODULES = createRegistryKey("quasar/modules/update");
    public static final ResourceKey<Registry<ParticleModuleData>> RENDER_MODULES = createRegistryKey("quasar/modules/render");
    public static final ResourceKey<Registry<QuasarParticleData>> PARTICLE_DATA = createRegistryKey("quasar/modules/particle_data");
    public static final ResourceKey<Registry<ParticleSettings>> PARTICLE_SETTINGS = createRegistryKey("quasar/modules/emitter/particle");
    public static final ResourceKey<Registry<EmitterShapeSettings>> EMITTER_SHAPE_SETTINGS = createRegistryKey("quasar/modules/emitter/shape");
    public static final ResourceKey<Registry<ParticleEmitterData>> EMITTER = createRegistryKey("quasar/emitters");

    private static final SuggestionProvider<?> EMITTER_SUGGESTION_PROVIDER = (unused, builder) -> SharedSuggestionProvider.suggestResource(registryAccess().registryOrThrow(QuasarParticles.EMITTER).keySet(), builder);
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final List<RegistryDataLoader.RegistryData<?>> REGISTRIES = List.of(
            new RegistryDataLoader.RegistryData<>(INIT_MODULES, ParticleModuleData.INIT_DIRECT_CODEC),
            new RegistryDataLoader.RegistryData<>(UPDATE_MODULES, ParticleModuleData.UPDATE_DIRECT_CODEC),
            new RegistryDataLoader.RegistryData<>(RENDER_MODULES, ParticleModuleData.RENDER_DIRECT_CODEC),
            new RegistryDataLoader.RegistryData<>(PARTICLE_DATA, QuasarParticleData.DIRECT_CODEC),
            new RegistryDataLoader.RegistryData<>(PARTICLE_SETTINGS, ParticleSettings.DIRECT_CODEC),
            new RegistryDataLoader.RegistryData<>(EMITTER_SHAPE_SETTINGS, EmitterShapeSettings.DIRECT_CODEC),
            new RegistryDataLoader.RegistryData<>(EMITTER, ParticleEmitterData.DIRECT_CODEC)
    );
    private static RegistryAccess registryAccess = RegistryAccess.EMPTY;

    private QuasarParticles() {
    }

    private static <T> ResourceKey<Registry<T>> createRegistryKey(String name) {
        return ResourceKey.createRegistryKey(Veil.veilPath(name));
    }

    @SuppressWarnings("unchecked")
    public static <T extends SharedSuggestionProvider> SuggestionProvider<T> emitterSuggestionProvider() {
        return (SuggestionProvider<T>) EMITTER_SUGGESTION_PROVIDER;
    }

    public static RegistryAccess registryAccess() {
        return registryAccess;
    }

    @ApiStatus.Internal
    public static class Reloader extends SimplePreparableReloadListener<Reloader.Preparations> {

        @Override
        protected Preparations prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
            Map<ResourceKey<?>, Exception> errors = new HashMap<>();

            List<Pair<WritableRegistry<?>, RegistryDataLoader.Loader>> loaders = QuasarParticles.REGISTRIES.stream().map(data -> ((RegistryDataAccessor) (Object) data).invokeCreate(Lifecycle.stable(), errors)).toList();
            RegistryOps.RegistryInfoLookup lookup = RegistryDataLoader.createContext(RegistryAccess.EMPTY, loaders);
            loaders.forEach(pair -> pair.getSecond().load(resourceManager, lookup));
            loaders.forEach(pair -> {
                Registry<?> registry = pair.getFirst();

                try {
                    registry.freeze();
                } catch (Exception e) {
                    errors.put(registry.key(), e);
                }
            });

            RegistryAccess.Frozen registryAccess = new RegistryAccess.ImmutableRegistryAccess(loaders.stream().map(Pair::getFirst).toList()).freeze();
            return new Preparations(registryAccess, errors);
        }

        @Override
        protected void apply(Preparations preparations, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
            registryAccess = preparations.registryAccess;
            ParticleEmitter.clearErrors();
            printErrors(preparations.errors);
            LOGGER.info("Loaded {} quasar particles", registryAccess.registryOrThrow(EMITTER).size());
            VeilRenderSystem.renderer().getParticleManager().clear();
        }

        @Override
        public String getName() {
            return QuasarParticles.class.getSimpleName();
        }

        public record Preparations(RegistryAccess registryAccess, Map<ResourceKey<?>, Exception> errors) {
        }

        private static void printErrors(Map<ResourceKey<?>, Exception> errors) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            Map<ResourceLocation, Map<ResourceLocation, Exception>> sortedErrors = errors.entrySet().stream().collect(Collectors.groupingBy(entry -> entry.getKey().registry(), Collectors.toMap(entry -> entry.getKey().location(), Map.Entry::getValue)));
            sortedErrors.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(registryError -> {
                printWriter.printf("%n> %d Errors in registry %s:", registryError.getValue().size(), registryError.getKey());
                registryError.getValue().entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(elementError -> {
                    Throwable error = elementError.getValue();
                    while (error.getCause() != null) {
                        error = error.getCause();
                    }
                    printWriter.printf("%n>> Error in element %s: %s", elementError.getKey(), error.getMessage());
                });
            });
            printWriter.flush();
            LOGGER.error("Quasar registry loading errors:{}", stringWriter);
        }
    }
}
