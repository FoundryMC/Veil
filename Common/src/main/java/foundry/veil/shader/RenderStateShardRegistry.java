package foundry.veil.shader;

import com.mojang.blaze3d.pipeline.RenderTarget;
import foundry.veil.postprocessing.PostProcessingHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public class RenderStateShardRegistry {
    /**
     * Create and initialize a {@link RenderStateShard.OutputStateShard} with the given name.
     * When initializing your {@link RenderType}, use the method {@link RenderType.CompositeState.CompositeStateBuilder#setOutputState(RenderStateShard.OutputStateShard)} with the output state you created.
     * <p>
     * In order to use this, create a shader that has a target and an auxtarget with the same ID as your {@link RenderTarget}.
     * You can now use this auxtarget as a sampler in your shader.
     * Remember to combine the output of your post-processing shader with the main {@link RenderTarget} in another post-processing shader.
     * You can permanently add a post-processing shader to the screen by adding it to the {@link PostProcessingHandler}.
     * <p>
     *     Example:
     * </p>
     */
    public static final RenderStateShard.OutputStateShard VEIL_CUSTOM = new RenderStateShard.OutputStateShard("veil_custom", () -> {
        RenderTargetRegistry.getRenderTargets().get("veil_custom").bindWrite(false);
    }, () -> {
        RenderTargetRegistry.getRenderTargets().get("veil_custom").bindWrite(false);
    });
}
