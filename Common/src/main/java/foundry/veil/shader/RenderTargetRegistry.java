package foundry.veil.shader;

import com.mojang.blaze3d.pipeline.RenderTarget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RenderTargetRegistry {
    public static Map<String, RenderTarget> renderTargets = new HashMap<>();
    public static List<String> shouldCopyDepth = new ArrayList<>();

    public static void register(String id, RenderTarget renderTarget) {
        renderTargets.put(id, renderTarget);
    }

    public static void register(String id, RenderTarget renderTarget, boolean shouldCopyDepth) {
        renderTargets.put(id, renderTarget);
        if(shouldCopyDepth) RenderTargetRegistry.shouldCopyDepth.add(id);
    }

    public static Map<String, RenderTarget> getRenderTargets() {
        return renderTargets;
    }
}
