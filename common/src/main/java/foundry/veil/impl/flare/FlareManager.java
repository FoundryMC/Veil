package foundry.veil.impl.flare;

import foundry.veil.api.quasar.data.*;
import foundry.veil.api.resource.VeilDynamicRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import foundry.veil.Veil;
import foundry.veil.api.flare.data.effect.FlareEffectTemplate;
import foundry.veil.api.flare.data.effect.FlareModule;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static foundry.veil.Veil.LOGGER;

public class FlareManager {

    public static final ResourceKey<Registry<FlareEffectTemplate>> EFFECT_TEMPLATES = createRegistryKey("flare/templates");
    public static final ResourceKey<Registry<FlareModule>> EFFECT_MODULES = createRegistryKey("flare/modules");

    private static final List<RegistryDataLoader.RegistryData<?>> REGISTRIES = List.of(
            new RegistryDataLoader.RegistryData<>(EFFECT_TEMPLATES, FlareEffectTemplate.CODEC, false),
            new RegistryDataLoader.RegistryData<>(EFFECT_MODULES, FlareModule.CODEC, false)
    );
    private static RegistryAccess registryAccess = RegistryAccess.EMPTY;
    private static boolean registriesDirty = false;


    private final ResourcesCache cache = new ResourcesCache();

    public FlareEffectTemplate getTemplate(ResourceLocation resourceLocation) {
        return cache.getCachedTemplate(resourceLocation);
    }

    public FlareModule getModule(ResourceLocation resourceLocation) {
        return cache.getCachedModule(resourceLocation);
    }

    public FlareManager() {
    }

    public static void bootstrap() {
    }

    private static <T> ResourceKey<Registry<T>> createRegistryKey(String name) {
        return ResourceKey.createRegistryKey(Veil.veilPath(name));
    }

    private static class ResourcesCache {

        public Map<ResourceLocation, FlareEffectTemplate> templateCache = new HashMap<>();
        public Map<ResourceLocation, FlareModule> moduleCache = new HashMap<>();

        public FlareEffectTemplate getCachedTemplate(ResourceLocation resourceLocation) {
            FlareEffectTemplate template;
            if (!registriesDirty) {
                clearRegistries();
                template = templateFromRegistry(resourceLocation);
            } else if ((template = templateCache.get(resourceLocation)) == null) {
                template = templateFromRegistry(resourceLocation);
            }

            return template;
        }

        public FlareModule getCachedModule(ResourceLocation resourceLocation) {
            FlareModule module;
            if (!registriesDirty) {
                clearRegistries();
                module = moduleFromRegistry(resourceLocation);
            } else if ((module = moduleCache.get(resourceLocation)) == null) {
                module = moduleFromRegistry(resourceLocation);
            }

            return module;
        }

        private FlareEffectTemplate templateFromRegistry(ResourceLocation resourceLocation) {
            return registryAccess.registry(EFFECT_TEMPLATES).map(registry -> registry.get(resourceLocation)).orElse(null);
        }

        private FlareModule moduleFromRegistry(ResourceLocation resourceLocation) {
            return registryAccess.registry(EFFECT_MODULES).map(registry -> registry.get(resourceLocation)).orElse(null);
        }

        private void clearRegistries() {
            templateCache.clear();
            moduleCache.clear();
            registriesDirty = false;
        }
    }

    public static class Reloader implements PreparableReloadListener {

        public static Reloader INSTANCE = new Reloader();

        private Reloader() {

        }

        @Override
        public @NotNull CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller preparationsProfiler, @NotNull ProfilerFiller reloadProfiler, @NotNull Executor backgroundExecutor, @NotNull Executor gameExecutor) {
            return VeilDynamicRegistry.loadRegistries(resourceManager, REGISTRIES, backgroundExecutor)
                    .thenCompose(preparationBarrier::wait)
                    .thenAcceptAsync(data -> {
                        registryAccess = data.registryAccess();
                        data.errors().values().forEach(Exception::printStackTrace);
                        String msg = VeilDynamicRegistry.printErrors(data.errors());
                        if (msg != null) {
                            LOGGER.error("Flare registry loading errors:{}", msg);
                        }
                        LOGGER.info("Loaded {} templates", registryAccess.registryOrThrow(EFFECT_TEMPLATES).size());
                        LOGGER.info("Loaded {} modules", registryAccess.registryOrThrow(EFFECT_MODULES).size());
                        registriesDirty = true;
                    }, gameExecutor);
        }

        @Override
        public @NotNull String getName() {
            return QuasarParticles.class.getSimpleName();
        }
    }
}
