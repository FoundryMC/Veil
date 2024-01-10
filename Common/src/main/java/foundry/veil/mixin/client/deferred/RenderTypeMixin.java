package foundry.veil.mixin.client.deferred;

import foundry.veil.Veil;
import foundry.veil.ext.CompositeStateExtension;
import foundry.veil.render.pipeline.VeilRenderSystem;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiFunction;
import java.util.function.Function;

@Mixin(RenderType.class)
public class RenderTypeMixin {

    @Shadow
    @Final
    private static RenderType SOLID;
    @Shadow
    @Final
    private static RenderType CUTOUT_MIPPED;
    @Shadow
    @Final
    private static RenderType CUTOUT;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void injectBlocks(CallbackInfo ci) {
        RenderStateShard solidDeferred = new RenderStateShard("deferred", () -> VeilRenderSystem.renderer().getDeferredRenderer().setup(), () -> VeilRenderSystem.renderer().getDeferredRenderer().clear()) {
        };
        veil$addShards(SOLID, solidDeferred);
        veil$addShards(CUTOUT_MIPPED, solidDeferred);
        veil$addShards(CUTOUT, solidDeferred);
    }

    @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;memoize(Ljava/util/function/Function;)Ljava/util/function/Function;", ordinal = 0))
    private static Function<ResourceLocation, RenderType> injectArmorCutoutNoCull(Function<ResourceLocation, RenderType> function) {
        return veil$addShards(function);
    }

    @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;memoize(Ljava/util/function/Function;)Ljava/util/function/Function;", ordinal = 1))
    private static Function<ResourceLocation, RenderType> injectEntitySolid(Function<ResourceLocation, RenderType> function) {
        return veil$addShards(function);
    }

    @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;memoize(Ljava/util/function/Function;)Ljava/util/function/Function;", ordinal = 2))
    private static Function<ResourceLocation, RenderType> injectEntityCutout(Function<ResourceLocation, RenderType> function) {
        return veil$addShards(function);
    }

    @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;memoize(Ljava/util/function/BiFunction;)Ljava/util/function/BiFunction;", ordinal = 0))
    private static BiFunction<ResourceLocation, Boolean, RenderType> injectEntityCutoutNoCull(BiFunction<ResourceLocation, Boolean, RenderType> function) {
        return veil$addShards(function);
    }

    @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;memoize(Ljava/util/function/BiFunction;)Ljava/util/function/BiFunction;", ordinal = 1))
    private static BiFunction<ResourceLocation, Boolean, RenderType> injectEntityCutoutNoCullZOffset(BiFunction<ResourceLocation, Boolean, RenderType> function) {
        return veil$addShards(function);
    }

    @Unique
    private static Function<ResourceLocation, RenderType> veil$addShards(Function<ResourceLocation, RenderType> function) {
        return function.andThen(renderType -> {
            veil$addShards(renderType, new RenderStateShard("deferred", () -> VeilRenderSystem.renderer().getDeferredRenderer().setup(), () -> VeilRenderSystem.renderer().getDeferredRenderer().clear()) {
            });
            return renderType;
        });
    }

    @Unique
    private static BiFunction<ResourceLocation, Boolean, RenderType> veil$addShards(BiFunction<ResourceLocation, Boolean, RenderType> function) {
        return function.andThen((renderType) -> {
            veil$addShards(renderType, new RenderStateShard("deferred", () -> VeilRenderSystem.renderer().getDeferredRenderer().setup(), () -> VeilRenderSystem.renderer().getDeferredRenderer().clear()) {
            });
            return renderType;
        });
    }

    @Unique
    private static void veil$addShards(RenderType renderType, RenderStateShard shard) {
        if (!(renderType instanceof RenderType.CompositeRenderType compositeRenderType)) {
            Veil.LOGGER.error("Failed to add shards to render type: {}", renderType);
            return;
        }

        ((CompositeStateExtension) (Object) compositeRenderType.state()).veil$addShard(shard);
    }
}
