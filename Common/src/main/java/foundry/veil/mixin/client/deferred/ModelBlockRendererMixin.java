package foundry.veil.mixin.client.deferred;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import foundry.veil.render.deferred.DeferredVertexConsumer;
import foundry.veil.render.deferred.VeilDeferredRenderer;
import foundry.veil.render.pipeline.VeilRenderSystem;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelBlockRenderer.class)
public class ModelBlockRendererMixin {

    @Unique
    private static final ThreadLocal<Boolean> veil$DEFERRED = ThreadLocal.withInitial(() -> false);

    @Inject(method = "tesselateBlock", at = @At("HEAD"))
    public void captureState(BlockAndTintGetter $$0, BakedModel $$1, BlockState blockState, BlockPos $$3, PoseStack $$4, VertexConsumer $$5, boolean $$6, RandomSource $$7, long $$8, int $$9, CallbackInfo ci) {
        VeilDeferredRenderer deferredRenderer = VeilRenderSystem.renderer().getDeferredRenderer();
        veil$DEFERRED.set(deferredRenderer.isEnabled() && deferredRenderer.getRendererState() != VeilDeferredRenderer.RendererState.DISABLED);
    }

    @Inject(method = "tesselateBlock", at = @At("RETURN"))
    public void clearState(BlockAndTintGetter $$0, BakedModel $$1, BlockState blockState, BlockPos $$3, PoseStack $$4, VertexConsumer $$5, boolean $$6, RandomSource $$7, long $$8, int $$9, CallbackInfo ci) {
        veil$DEFERRED.set(false);
    }

    @ModifyVariable(method = "tesselateBlock", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    public VertexConsumer modifyConsumer(VertexConsumer value) {
        return veil$DEFERRED.get() ? new DeferredVertexConsumer(value) : value;
    }
}
