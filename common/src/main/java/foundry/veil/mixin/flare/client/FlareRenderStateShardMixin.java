package foundry.veil.mixin.flare.client;

import foundry.veil.ext.RenderStateShardExtension;
import net.minecraft.client.renderer.RenderStateShard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(RenderStateShard.class)
public class FlareRenderStateShardMixin implements RenderStateShardExtension {
    @Unique
    private final List<Runnable> veil$setup = new ArrayList<>();
    @Unique
    private final List<Runnable> veil$clear = new ArrayList<>();

    @Inject(method = "setupRenderState", at = @At(value = "TAIL"))
    private void applySetup(CallbackInfo ci) {

    }

    @Inject(method = "clearRenderState", at = @At(value = "HEAD"))
    private void applyClear(CallbackInfo ci) {
        for (Runnable post : veil$clear) {
            post.run();
        }
        this.veil$setup.clear();
        this.veil$clear.clear();
    }

    @Override
    public void veil$addSetup(Runnable preDraw) {
        this.veil$setup.add(preDraw);
    }

    @Override
    public void veil$addClear(Runnable postDraw) {
        this.veil$clear.add(postDraw);
    }

    @Override
    public List<Runnable> veil$getSetup() {
        return this.veil$setup;
    }

    @Override
    public List<Runnable> veil$getClear() {
        return this.veil$clear;
    }
}
