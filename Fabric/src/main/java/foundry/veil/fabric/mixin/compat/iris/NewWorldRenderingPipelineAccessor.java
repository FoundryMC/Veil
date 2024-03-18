package foundry.veil.fabric.mixin.compat.iris;

import net.coderbot.iris.pipeline.newshader.NewWorldRenderingPipeline;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(NewWorldRenderingPipeline.class)
public interface NewWorldRenderingPipelineAccessor {

    @Accessor(remap = false)
    Set<ShaderInstance> getLoadedShaders();
}
