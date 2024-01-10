package foundry.veil.mixin.client.deferred;

import com.mojang.blaze3d.vertex.VertexConsumer;
import foundry.veil.render.deferred.DeferredVertexConsumer;
import foundry.veil.render.pipeline.VeilRenderSystem;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ModelBlockRenderer.class)
public class ModelBlockRendererMixin {

    @ModifyVariable(method = "putQuadData", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    public VertexConsumer modifyConsumer(VertexConsumer value) {
        return VeilRenderSystem.renderer().getDeferredRenderer().isEnabled() ? new DeferredVertexConsumer(value) : value;
    }
}
