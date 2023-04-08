package foundry.veil.mixin.client;

import foundry.veil.material.BlockMaterialHolder;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ItemBlockRenderTypes.class)
public class ItemBlockRenderTypesMixin {

    @Inject(method = "getChunkRenderType", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private static void getChunkRenderType(BlockState $$0, CallbackInfoReturnable<RenderType> cir) {
        Block block = $$0.getBlock();
        if (BlockMaterialHolder.hasMaterial(block)) {
            cir.setReturnValue(BlockMaterialHolder.getMaterial(block).constructRenderType());
        }
    }
}
