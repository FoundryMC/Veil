package foundry.veil.impl.compat;

import foundry.veil.Veil;
import net.minecraft.client.renderer.ShaderInstance;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;

@ApiStatus.Internal
public class IrisShaderMap {

    private static final boolean IRIS = Veil.platform().isModLoaded("iris");
    private static Supplier<Set<ShaderInstance>> loadedShadersSupplier = Collections::emptySet;

    public static Set<ShaderInstance> getLoadedShaders() {
        return loadedShadersSupplier.get();
    }

    public static void setLoadedShadersSupplier(Supplier<Set<ShaderInstance>> loadedShadersSupplier) {
        IrisShaderMap.loadedShadersSupplier = loadedShadersSupplier;
    }

    public static boolean isEnabled() {
        return IRIS;
    }
}
