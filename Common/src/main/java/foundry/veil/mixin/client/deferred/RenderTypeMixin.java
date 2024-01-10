package foundry.veil.mixin.client.deferred;

import com.mojang.blaze3d.vertex.VertexFormat;
import foundry.veil.Veil;
import foundry.veil.ext.CompositeStateExtension;
import foundry.veil.render.pipeline.VeilRenderSystem;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderType.class)
public class RenderTypeMixin {

    @Inject(method = "create(Ljava/lang/String;Lcom/mojang/blaze3d/vertex/VertexFormat;Lcom/mojang/blaze3d/vertex/VertexFormat$Mode;IZZLnet/minecraft/client/renderer/RenderType$CompositeState;)Lnet/minecraft/client/renderer/RenderType$CompositeRenderType;", at = @At("HEAD"))
    private static void injectDeferred(String $$0, VertexFormat $$1, VertexFormat.Mode $$2, int $$3, boolean $$4, boolean $$5, RenderType.CompositeState state, CallbackInfoReturnable<RenderType.CompositeRenderType> cir) {
        ((CompositeStateExtension) (Object) state).veil$addShard(new RenderStateShard(Veil.MODID + ":deferred", () -> VeilRenderSystem.renderer().getDeferredRenderer().setup(), () -> VeilRenderSystem.renderer().getDeferredRenderer().clear()) {
        });
    }
}
