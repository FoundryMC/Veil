package foundry.veil.api.client.render.shader;

import foundry.veil.impl.client.render.shader.injection.ShaderInjectionManager;
import io.github.ocelot.glslprocessor.api.node.GlslTree;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * @deprecated Use {@link ShaderInjectionManager} instead.
 */
@ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
@Deprecated(forRemoval = true)
public class ShaderModificationManager extends SimplePreparableReloadListener<ShaderModificationManager.Preparations> {

    public void applyModifiers(ResourceLocation shaderId, GlslTree tree, int flags) {
    }

    @Override
    protected @NotNull Preparations prepare(@NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller) {
        return new Preparations();
    }

    @Override
    protected void apply(@NotNull Preparations preparations, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller) {
    }

    /**
     * @deprecated Use {@link ShaderInjectionManager} instead.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    @Deprecated(forRemoval = true)
    @ApiStatus.Internal
    public record Preparations() {
    }
}
