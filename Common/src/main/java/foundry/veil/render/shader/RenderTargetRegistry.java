package foundry.veil.render.shader;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RenderTargetRegistry {
    public static Map<String, Pair<Integer, Integer>> renderTargets = new HashMap<>();
    public static Map<String, RenderTarget> renderTargetObjects = new HashMap<>();
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
    public static void register(String id, Pair<Integer, Integer> renderTarget) {
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
    public static void register(String id,  Pair<Integer, Integer> renderTarget, boolean shouldCopyDepth) {
        renderTargets.put(id, renderTarget);
        if(shouldCopyDepth) RenderTargetRegistry.shouldCopyDepth.add(id);
    }

    public static Map<String,  Pair<Integer, Integer>> getRenderTargets() {
        return renderTargets;
    }
    public static void modifyPostChain(PostChain postChain, ResourceLocation $$3) {
        if($$3.getNamespace().equals("veil")){
            RenderTargetRegistry.getRenderTargets().forEach((name, target) -> {
                        if (target == null) return;
                        postChain.addTempTarget(name, target.getFirst(), target.getSecond());
                    }
            );
        }
    }
}
