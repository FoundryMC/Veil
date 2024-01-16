package foundry.veil.util;

import foundry.veil.quasar.client.particle.data.QuasarParticleDataListener;
import foundry.veil.quasar.emitters.ParticleEmitterJsonListener;
import foundry.veil.quasar.emitters.modules.emitter.settings.EmitterSettingsJsonListener;
import foundry.veil.quasar.emitters.modules.emitter.settings.ParticleSettingsJsonListener;
import foundry.veil.quasar.emitters.modules.emitter.settings.ShapeSettingsJsonListener;
import foundry.veil.quasar.emitters.modules.particle.init.InitModuleJsonListener;
import foundry.veil.quasar.emitters.modules.particle.render.RenderModuleJsonListener;
import foundry.veil.quasar.emitters.modules.particle.update.UpdateModuleJsonListener;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class VeilJsonListeners {

    public static void registerListeners(Context context) {
        context.register(PackType.CLIENT_RESOURCES, "init_module", new InitModuleJsonListener());
        context.register(PackType.CLIENT_RESOURCES, "update_module", new UpdateModuleJsonListener());
        context.register(PackType.CLIENT_RESOURCES, "render_module", new RenderModuleJsonListener());
        context.register(PackType.CLIENT_RESOURCES, "particle_data", new QuasarParticleDataListener());
        context.register(PackType.CLIENT_RESOURCES, "particle_settings", new ParticleSettingsJsonListener());
        context.register(PackType.CLIENT_RESOURCES, "shape_settings", new ShapeSettingsJsonListener());
        context.register(PackType.CLIENT_RESOURCES, "emitter_settings", new EmitterSettingsJsonListener());
        context.register(PackType.CLIENT_RESOURCES, "emitter", new ParticleEmitterJsonListener());
    }
    @FunctionalInterface
    public interface Context {
        void register(PackType type, String id, SimpleJsonResourceReloadListener listener);
    }
}
