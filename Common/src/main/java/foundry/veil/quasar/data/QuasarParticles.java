package foundry.veil.quasar.data;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import foundry.veil.Veil;
import foundry.veil.mixin.client.quasar.RegistryDataAccessor;
import foundry.veil.mixin.client.quasar.RegistryDataLoaderAccessor;
import foundry.veil.quasar.client.particle.data.QuasarParticleData;
import foundry.veil.quasar.data.module.ParticleModuleData;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

public final class QuasarParticles {

    public static final ResourceKey<Registry<ParticleModuleData>> INIT_MODULES = createRegistryKey("quasar/modules/init");
    public static final ResourceKey<Registry<ParticleModuleData>> UPDATE_MODULES = createRegistryKey("quasar/modules/update");
    public static final ResourceKey<Registry<ParticleModuleData>> RENDER_MODULES = createRegistryKey("quasar/modules/render");
    public static final ResourceKey<Registry<QuasarParticleData>> PARTICLE_DATA = createRegistryKey("quasar/modules/particle_data");
    public static final ResourceKey<Registry<ParticleSettings>> PARTICLE_SETTINGS = createRegistryKey("quasar/modules/emitter/particle");
    public static final ResourceKey<Registry<EmitterShapeSettings>> EMITTER_SHAPE_SETTINGS = createRegistryKey("quasar/modules/emitter/shape");
    public static final ResourceKey<Registry<EmitterSettings>> EMITTER_SETTINGS = createRegistryKey("quasar/modules/emitter/settings");
    public static final ResourceKey<Registry<ParticleEmitterData>> EMITTER = createRegistryKey("quasar/emitters");

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final List<RegistryDataLoader.RegistryData<?>> REGISTRIES = List.of(
            new RegistryDataLoader.RegistryData<>(INIT_MODULES, ParticleModuleData.INIT_DIRECT_CODEC),
            new RegistryDataLoader.RegistryData<>(UPDATE_MODULES, ParticleModuleData.UPDATE_DIRECT_CODEC),
            new RegistryDataLoader.RegistryData<>(RENDER_MODULES, ParticleModuleData.RENDER_DIRECT_CODEC),
            new RegistryDataLoader.RegistryData<>(PARTICLE_DATA, QuasarParticleData.DIRECT_CODEC),
            new RegistryDataLoader.RegistryData<>(PARTICLE_SETTINGS, ParticleSettings.DIRECT_CODEC),
            new RegistryDataLoader.RegistryData<>(EMITTER_SHAPE_SETTINGS, EmitterShapeSettings.DIRECT_CODEC),
            new RegistryDataLoader.RegistryData<>(EMITTER_SETTINGS, EmitterSettings.DIRECT_CODEC),
            new RegistryDataLoader.RegistryData<>(EMITTER, ParticleEmitterData.DIRECT_CODEC)
    );
    private static RegistryAccess registryAccess = RegistryAccess.EMPTY;

    private QuasarParticles() {
    }

    private static <T> ResourceKey<Registry<T>> createRegistryKey(String name) {
        return ResourceKey.createRegistryKey(Veil.veilPath(name));
    }

    public static RegistryAccess registryAccess() {
        return registryAccess;
    }

    @ApiStatus.Internal
    public static class Reloader implements PreparableReloadListener {

        @Override
        public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller prepareProfiler, ProfilerFiller applyProfiler, Executor backgroundExecutor, Executor gameExecutor) {
            return CompletableFuture.supplyAsync(() -> {
                        Map<ResourceKey<?>, Exception> errors = new HashMap<>();

                        List<Pair<WritableRegistry<?>, RegistryDataLoader.Loader>> loaders = QuasarParticles.REGISTRIES.stream().map(data -> ((RegistryDataAccessor) (Object) data).invokeCreate(Lifecycle.stable(), errors)).toList();
                        RegistryOps.RegistryInfoLookup lookup = RegistryDataLoaderAccessor.invokeCreateContext(RegistryAccess.EMPTY, loaders);
                        loaders.forEach(pair -> pair.getSecond().load(resourceManager, lookup));
                        loaders.forEach(pair -> {
                            Registry<?> registry = pair.getFirst();

                            try {
                                registry.freeze();
                            } catch (Exception e) {
                                errors.put(registry.key(), e);
                            }
                        });

                        printErrors(errors);
                        return new RegistryAccess.ImmutableRegistryAccess(loaders.stream().map(Pair::getFirst).toList()).freeze();
                    }, backgroundExecutor)
                    .thenCompose(preparationBarrier::wait)
                    .thenAcceptAsync(registryAccess -> QuasarParticles.registryAccess = registryAccess, gameExecutor);
        }

        private static void printErrors(Map<ResourceKey<?>, Exception> errors) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            Map<ResourceLocation, Map<ResourceLocation, Exception>> sortedErrors = errors.entrySet().stream().collect(Collectors.groupingBy(entry -> entry.getKey().registry(), Collectors.toMap(entry -> entry.getKey().location(), Map.Entry::getValue)));
            sortedErrors.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(registryError -> {
                printWriter.printf("> %d Errors in registry %s:%n", registryError.getValue().size(), registryError.getKey());
                registryError.getValue().entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(elementError -> {
                    Throwable error = elementError.getValue();
                    while (error.getCause() != null) {
                        error = error.getCause();
                    }
                    printWriter.printf(">> Error in element %s: %s%n", elementError.getKey(), error.getMessage());
                });
            });
            printWriter.flush();
            LOGGER.error("Registry loading errors:\n{}", stringWriter);
        }
    }
}
