package foundry.veil.mixin.client.shader;

import com.mojang.blaze3d.shaders.Program;
import foundry.veil.render.shader.VanillaShaderImportProcessor;
import foundry.veil.render.shader.program.ShaderProgramImpl;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShaderInstance.class)
public class ShaderInstanceMixin {

    @Unique
    private static boolean veil$fallbackProcessor;

    @Inject(method = "getOrCreate", at = @At("HEAD"), cancellable = true)
    private static void veil$cancelDummyProgram(ResourceProvider provider, Program.Type type, String name, CallbackInfoReturnable<Program> cir) {
        if (ShaderProgramImpl.Wrapper.constructing) {
            cir.setReturnValue(null);
        }
    }

    @Inject(method = "getOrCreate", at = @At("HEAD"))
    private static void veil$setupFallbackProcessor(ResourceProvider provider, Program.Type type, String name, CallbackInfoReturnable<Program> cir) {
        veil$fallbackProcessor = VanillaShaderImportProcessor.setupFallback(provider);
    }

    @Inject(method = "getOrCreate", at = @At("RETURN"))
    private static void veil$clearFallbackProcessor(ResourceProvider provider, Program.Type type, String name, CallbackInfoReturnable<Program> cir) {
        if (veil$fallbackProcessor) {
            VanillaShaderImportProcessor.free();
        }
    }
}
