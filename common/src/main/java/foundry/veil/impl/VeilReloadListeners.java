package foundry.veil.impl;

import foundry.veil.Veil;
import foundry.veil.VeilClient;
import foundry.veil.api.quasar.data.QuasarParticles;
import foundry.veil.impl.flare.FlareManager;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class VeilReloadListeners {
    public static void registerListeners(Context context) {
        context.register(PackType.CLIENT_RESOURCES, "quasar", QuasarParticles.Reloader.INSTANCE);
        context.register(PackType.CLIENT_RESOURCES, "flare", FlareManager.Reloader.INSTANCE);
        if (Veil.IMGUIMC) {
            context.register(PackType.CLIENT_RESOURCES, "resources", VeilClient.resourceManager().createReloadListener());
        }
    }

    @FunctionalInterface
    public interface Context {

        void register(PackType type, String id, PreparableReloadListener listener);
    }
}
