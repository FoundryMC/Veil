package foundry.veil.util;

import foundry.veil.api.quasar.data.QuasarParticles;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class VeilJsonListeners {

    public static void registerListeners(Context context) {
        context.register(PackType.CLIENT_RESOURCES, "quasar", new QuasarParticles.Reloader());
    }

    @FunctionalInterface
    public interface Context {

        void register(PackType type, String id, PreparableReloadListener listener);
    }
}
