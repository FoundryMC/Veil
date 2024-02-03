package foundry.veil.util;

import foundry.veil.quasar.data.QuasarParticles;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class VeilJsonListeners {

    public static void registerListeners(Context context) {
        context.register(PackType.CLIENT_RESOURCES, "quasar", new QuasarParticles.Reloader());
//        context.register(PackType.CLIENT_RESOURCES, "init_module", new InitModuleJsonListener());
//        context.register(PackType.CLIENT_RESOURCES, "update_module", new UpdateModuleJsonListener());
//        context.register(PackType.CLIENT_RESOURCES, "render_module", new RenderModuleJsonListener());
//        context.register(PackType.CLIENT_RESOURCES, "particle_data", new QuasarParticleDataListener());
//        context.register(PackType.CLIENT_RESOURCES, "particle_settings", new ParticleSettingsJsonListener());
//        context.register(PackType.CLIENT_RESOURCES, "shape_settings", new ShapeSettingsJsonListener());
//        context.register(PackType.CLIENT_RESOURCES, "emitter_settings", new EmitterSettingsJsonListener());
//        context.register(PackType.CLIENT_RESOURCES, "emitter", new ParticleEmitterJsonListener());
    }

    @FunctionalInterface
    public interface Context {

        void register(PackType type, String id, PreparableReloadListener listener);
    }
}
