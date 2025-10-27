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
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static foundry.veil.Veil.LOGGER;

@ApiStatus.Internal
public class FlareManager {

    public static final ResourceKey<Registry<FlareEffectTemplate>> EFFECT_TEMPLATES = createRegistryKey("flare/templates");
    public static final ResourceKey<Registry<FlareModule>> EFFECT_MODULES = createRegistryKey("flare/modules");

    private static final List<RegistryDataLoader.RegistryData<?>> REGISTRIES = List.of(
            new RegistryDataLoader.RegistryData<>(EFFECT_TEMPLATES, FlareEffectTemplate.CODEC, false),
            new RegistryDataLoader.RegistryData<>(EFFECT_MODULES, FlareModule.CODEC, false)
    );
    private static RegistryAccess registryAccess = RegistryAccess.EMPTY;

    private FlareManager() {
    }

    public static void bootstrap() {
    }

    private static <T> ResourceKey<Registry<T>> createRegistryKey(String name) {
        return ResourceKey.createRegistryKey(Veil.veilPath(name));
    }

    public static RegistryAccess registryAccess() {
        return registryAccess;
    }

    public static class Reloader implements PreparableReloadListener {

        public static final Reloader INSTANCE = new Reloader();

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
                    }, gameExecutor);
        }

        @Override
        public @NotNull String getName() {
            return QuasarParticles.class.getSimpleName();
        }
    }
}
