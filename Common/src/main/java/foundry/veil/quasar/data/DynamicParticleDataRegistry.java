package foundry.veil.quasar.data;

import foundry.veil.Veil;
import foundry.veil.quasar.client.particle.data.QuasarParticleData;
import foundry.veil.quasar.data.module.ParticleModuleData;
import foundry.veil.quasar.emitters.modules.emitter.settings.EmitterShapeSettings;
import foundry.veil.quasar.emitters.modules.emitter.settings.ParticleSettings;
import foundry.veil.quasar.emitters.modules.emitter.settings.EmitterSettingsModuleData;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class DynamicParticleDataRegistry {

    public static final ResourceKey<Registry<ParticleModuleData>> INIT_MODULES = createRegistryKey("quasar/modules/init");
    public static final ResourceKey<Registry<ParticleModuleData>> UPDATE_MODULES = createRegistryKey("quasar/modules/update");
    public static final ResourceKey<Registry<ParticleModuleData>> RENDER_MODULES = createRegistryKey("quasar/modules/render");
    public static final ResourceKey<Registry<QuasarParticleData>> PARTICLE_DATA = createRegistryKey("quasar/modules/particle_data");
    public static final ResourceKey<Registry<ParticleSettings>> PARTICLE_SETTINGS = createRegistryKey("quasar/modules/emitter/particle");
    public static final ResourceKey<Registry<EmitterShapeSettings>> EMITTER_SHAPE_SETTINGS = createRegistryKey("quasar/modules/emitter/shape");
    public static final ResourceKey<Registry<EmitterSettingsModuleData>> EMITTER_SETTINGS = createRegistryKey("quasar/modules/emitter/settings");

    private static final List<RegistryDataLoader.RegistryData<?>> REGISTRIES = List.of(
            new RegistryDataLoader.RegistryData<>(INIT_MODULES, ParticleModuleData.INIT_DIRECT_CODEC),
            new RegistryDataLoader.RegistryData<>(UPDATE_MODULES, ParticleModuleData.UPDATE_DIRECT_CODEC),
            new RegistryDataLoader.RegistryData<>(RENDER_MODULES, ParticleModuleData.RENDER_DIRECT_CODEC),
            new RegistryDataLoader.RegistryData<>(PARTICLE_DATA, QuasarParticleData.DIRECT_CODEC),
            new RegistryDataLoader.RegistryData<>(PARTICLE_SETTINGS, ParticleSettings.DIRECT_CODEC),
            new RegistryDataLoader.RegistryData<>(EMITTER_SHAPE_SETTINGS, EmitterShapeSettings.DIRECT_CODEC),
            new RegistryDataLoader.RegistryData<>(EMITTER_SETTINGS, EmitterSettingsModuleData.DIRECT_CODEC)
    );
    private static RegistryAccess registryAccess = RegistryAccess.EMPTY;

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
            return CompletableFuture.supplyAsync(() -> RegistryDataLoader.load(resourceManager, RegistryAccess.EMPTY, REGISTRIES), backgroundExecutor)
                    .thenCompose(preparationBarrier::wait)
                    .thenAcceptAsync(registryAccess -> {
                        DynamicParticleDataRegistry.registryAccess = registryAccess;
                    }, gameExecutor);
        }
    }
}
