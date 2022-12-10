package foundry.veil.shader;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RenderTargetRegistry {
    public static Map<String, RenderTarget> renderTargets = new HashMap<>();
    public static List<String> shouldCopyDepth = new ArrayList<>();

    /**
     * Add a {@link RenderTarget} to the registry.
     * @param id The name of the render target.
     * @param renderTarget The render target.
     *                     <p>
     * This will initialize the render target and optionally copy the depth buffer.
     * Tip: If you want a quick RenderTarget to use, use {@link TextureTarget}
     *                     <p>
     * How to use: See {@link RenderStateShardRegistry}
     */
    public static void register(String id, RenderTarget renderTarget) {
        renderTargets.put(id, renderTarget);
    }
    /**
     * Add a {@link RenderTarget} to the registry.
     * @param id The name of the render target.
     * @param renderTarget The render target.
     * @param shouldCopyDepth Whether the depth buffer should be copied to this render target.
     *                        <p>
     * This will initialize the render target and optionally copy the depth buffer.
     * Tip: If you want a quick RenderTarget to use, use {@link TextureTarget}
     *                        <p>
     * How to use: See {@link RenderStateShardRegistry}
     */
    public static void register(String id, RenderTarget renderTarget, boolean shouldCopyDepth) {
        renderTargets.put(id, renderTarget);
        if(shouldCopyDepth) RenderTargetRegistry.shouldCopyDepth.add(id);
    }

    public static Map<String, RenderTarget> getRenderTargets() {
        return renderTargets;
    }
}
