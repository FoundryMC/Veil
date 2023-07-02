package foundry.veil.mixin.client.shader;

import com.mojang.blaze3d.shaders.ProgramManager;
import foundry.veil.render.shader.program.ShaderProgram;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ProgramManager.class)
public class ProgramManagerMixin {

    @Inject(method = "createProgram", at = @At("HEAD"), cancellable = true)
    private static void veil$cancelProgram(CallbackInfoReturnable<Integer> cir) {
        if (ShaderProgram.Wrapper.constructing) {
            cir.setReturnValue(0);
        }
    }
}
