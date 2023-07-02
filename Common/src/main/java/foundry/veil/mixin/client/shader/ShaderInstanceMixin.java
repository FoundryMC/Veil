package foundry.veil.mixin.client.shader;

import com.mojang.blaze3d.shaders.Program;
import foundry.veil.render.shader.program.ShaderProgram;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShaderInstance.class)
public class ShaderInstanceMixin {

    @Inject(method = "getOrCreate", at = @At("HEAD"), cancellable = true)
    private static void veil$cancelDummyProgram(ResourceProvider provider, Program.Type type, String name, CallbackInfoReturnable<Program> cir) {
        if (ShaderProgram.Wrapper.constructing) {
            cir.setReturnValue(null);
        }
    }
}
