package foundry.veil.mixin.client.stage;

import com.mojang.blaze3d.vertex.VertexFormat;
import foundry.veil.api.client.render.RenderTypeStageRegistry;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderType.class)
public class RenderTypeMixin {

    @Inject(method = "create(Ljava/lang/String;Lcom/mojang/blaze3d/vertex/VertexFormat;Lcom/mojang/blaze3d/vertex/VertexFormat$Mode;IZZLnet/minecraft/client/renderer/RenderType$CompositeState;)Lnet/minecraft/client/renderer/RenderType$CompositeRenderType;", at = @At("RETURN"))
    private static void injectDeferred(String $$0, VertexFormat $$1, VertexFormat.Mode $$2, int $$3, boolean $$4, boolean $$5, RenderType.CompositeState $$6, CallbackInfoReturnable<RenderType.CompositeRenderType> cir) {
        RenderTypeStageRegistry.inject(cir.getReturnValue());
    }
}
