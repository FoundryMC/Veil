package foundry.veil.mixin.client.shader;

import com.mojang.blaze3d.shaders.EffectProgram;
import com.mojang.blaze3d.shaders.Program;
import foundry.veil.render.shader.VanillaShaderImportProcessor;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EffectInstance.class)
public class EffectInstanceMixin {

    @Inject(method = "getOrCreate", at = @At("HEAD"))
    private static void veil$setupFallbackProcessor(ResourceManager provider, Program.Type $$1, String $$2, CallbackInfoReturnable<EffectProgram> cir) {
        VanillaShaderImportProcessor.setup(provider);
    }

    @Inject(method = "getOrCreate", at = @At("RETURN"))
    private static void veil$clearFallbackProcessor(ResourceManager $$0, Program.Type $$1, String $$2, CallbackInfoReturnable<EffectProgram> cir) {
        VanillaShaderImportProcessor.free();
    }
}
