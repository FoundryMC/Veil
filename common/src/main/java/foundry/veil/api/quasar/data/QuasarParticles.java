package foundry.veil.api.quasar.data;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import foundry.veil.Veil;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.quasar.data.module.ModuleType;
import foundry.veil.api.quasar.data.module.ParticleModuleData;
import foundry.veil.api.quasar.particle.ParticleEmitter;
import foundry.veil.api.resource.VeilDynamicRegistry;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class QuasarParticles {

    public static final ResourceKey<Registry<ParticleModuleData>> MODULES = createRegistryKey("quasar/modules/module");
    public static final ResourceKey<Registry<QuasarParticleData>> PARTICLE_DATA = createRegistryKey("quasar/modules/particle_data");
    public static final ResourceKey<Registry<ParticleSettings>> PARTICLE_SETTINGS = createRegistryKey("quasar/modules/emitter/particle");
    public static final ResourceKey<Registry<EmitterShapeSettings>> EMITTER_SHAPE_SETTINGS = createRegistryKey("quasar/modules/emitter/shape");
    public static final ResourceKey<Registry<ParticleEmitterData>> EMITTER = createRegistryKey("quasar/emitters");

    private static final SuggestionProvider<?> EMITTER_SUGGESTION_PROVIDER = (unused, builder) -> registryAccess().registry(EMITTER).map(registry -> SharedSuggestionProvider.suggestResource(registry.keySet(), builder)).orElseGet(Suggestions::empty);
    private static final List<RegistryDataLoader.RegistryData<?>> REGISTRIES = List.of(
            new RegistryDataLoader.RegistryData<>(MODULES, ParticleModuleData.DIRECT_CODEC, false),
            new RegistryDataLoader.RegistryData<>(PARTICLE_DATA, QuasarParticleData.DIRECT_CODEC, false),
            new RegistryDataLoader.RegistryData<>(PARTICLE_SETTINGS, ParticleSettings.DIRECT_CODEC, false),
            new RegistryDataLoader.RegistryData<>(EMITTER_SHAPE_SETTINGS, EmitterShapeSettings.DIRECT_CODEC, false),
            new RegistryDataLoader.RegistryData<>(EMITTER, ParticleEmitterData.DIRECT_CODEC, false)
    );
    private static RegistryAccess registryAccess = RegistryAccess.EMPTY;

    private QuasarParticles() {
    }

    @ApiStatus.Internal
    public static void bootstrap() {
        // This is here to make sure the class is loaded before particles are reloaded, otherwise KubeJS crashes
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
    public static class Reloader implements PreparableReloadListener {

        @Override
        public @NotNull CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller preparationsProfiler, @NotNull ProfilerFiller reloadProfiler, @NotNull Executor backgroundExecutor, @NotNull Executor gameExecutor) {
            return VeilDynamicRegistry.loadRegistries(resourceManager, REGISTRIES, backgroundExecutor)
                    .thenCompose(preparationBarrier::wait)
                    .thenAcceptAsync(data -> {
                        registryAccess = data.registryAccess();
                        ParticleEmitter.clearErrors();

                        String msg = VeilDynamicRegistry.printErrors(data.errors());
                        if (msg != null) {
                            Veil.LOGGER.error("Quasar registry loading errors:{}", msg);
                        }

                        Registry<ParticleModuleData> modulesRegistry = registryAccess.registryOrThrow(MODULES);
                        for (Map.Entry<ResourceKey<ParticleModuleData>, ParticleModuleData> entry : modulesRegistry.entrySet()) {
                            ParticleModuleData moduleData = entry.getValue();
                            ModuleType.DeprecationStatus status = moduleData.getType().deprecationStatus();
                            if (status != null) {
                                Veil.LOGGER.error("{} is deprecated and will be removed in {}", entry.getKey(), status.removeVersion());
                                String reason = status.reason();
                                if (reason != null) {
                                    Veil.LOGGER.error("Reason: {}", reason);
                                }
                            }
                        }

                        Veil.LOGGER.info("Loaded {} quasar particles", registryAccess.registryOrThrow(EMITTER).size());
                        VeilRenderSystem.renderer().getParticleManager().clear();
                    }, gameExecutor);
        }

        @Override
        public @NotNull String getName() {
            return QuasarParticles.class.getSimpleName();
        }
    }
}
