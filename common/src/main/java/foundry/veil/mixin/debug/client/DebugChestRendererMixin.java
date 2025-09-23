package foundry.veil.mixin.debug.client;

import com.mojang.blaze3d.vertex.PoseStack;
import foundry.veil.Veil;
import foundry.veil.api.client.render.MatrixStack;
import foundry.veil.api.flare.EffectHost;
import foundry.veil.api.flare.FlareEffectManager;
import foundry.veil.api.flare.data.effect.FlareModule;
import foundry.veil.api.flare.data.effect.FlareSubModule;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChestRenderer.class)
public class DebugChestRendererMixin implements EffectHost {
    @Unique
    private static final ResourceLocation veil$DEBUG = Veil.veilPath("debug");

    @Inject(method = "render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V", at = @At("TAIL"))
    private <T extends BlockEntity & LidBlockEntity> void flare(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, CallbackInfo ci) {
        FlareModule module = FlareEffectManager.getInstance().getModule(veil$DEBUG);
        if (module == null) return;
        FlareSubModule subModule = module.getSubModule("plume");
        if (subModule != null) subModule.render(this, (MatrixStack) poseStack, null);
    }

    @Override
    public float getValue(String name) {
        return 0.5f;
    }

    @Override
    public String getName() {
        return toString();
    }

    @Override
    public void update(float partialTick) {

    }
}
