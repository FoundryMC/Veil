package foundry.veil.api.client.render.shader;

import foundry.veil.impl.client.render.shader.injection.util.ShaderInjection;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Map;

/**
 * @deprecated Use {@link ShaderInjectionManager} instead.
 */
@Deprecated(forRemoval = true)
public class ShaderModificationManager extends ShaderInjectionManager {

    /**
     * @deprecated Use {@link ShaderInjectionManager} instead.
     */
    @Deprecated(forRemoval = true)
    @ApiStatus.Internal
    public record Preparations(Map<ResourceLocation, List<ShaderInjection>> shaders,
                               Map<ShaderInjection, ResourceLocation> names) {
    }
}
