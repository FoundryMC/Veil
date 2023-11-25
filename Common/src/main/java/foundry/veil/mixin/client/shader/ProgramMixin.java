package foundry.veil.mixin.client.shader;

import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.shaders.Program;
import foundry.veil.Veil;
import foundry.veil.render.pipeline.VeilRenderSystem;
import foundry.veil.render.shader.VanillaShaderImportProcessor;
import foundry.veil.render.shader.modifier.ShaderModification;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.io.InputStream;

@Mixin(Program.class)
public class ProgramMixin {

    @Unique
    private static ResourceLocation veil$captureId;

    @Inject(method = "compileShaderInternal", at = @At("HEAD"))
    private static void veil$captureId(Program.Type type, String name, InputStream stream, String pack, GlslPreprocessor glslPreprocessor, CallbackInfoReturnable<Integer> cir) {
        ResourceLocation loc = new ResourceLocation(name);
        String s = "shaders/core/" + loc.getPath() + type.getExtension();
        veil$captureId = new ResourceLocation(loc.getNamespace(), s);
    }

    @Inject(method = "compileShaderInternal", at = @At("RETURN"))
    private static void veil$clear(Program.Type type, String name, InputStream stream, String pack, GlslPreprocessor glslPreprocessor, CallbackInfoReturnable<Integer> cir) {
        veil$captureId = null;
    }

    @ModifyVariable(method = "compileShaderInternal", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/preprocessor/GlslPreprocessor;process(Ljava/lang/String;)Ljava/util/List;", shift = At.Shift.BEFORE), ordinal = 2)
    private static String veil$modifyVanillaShader(String value) {
        String source = VeilRenderSystem.renderer().getShaderModificationManager().applyModifiers(veil$captureId, value, ShaderModification.APPLY_VERSION | ShaderModification.ALLOW_OUT);
        try {
            return VanillaShaderImportProcessor.modify(source);
        } catch (Exception e) {
            Veil.LOGGER.error("Failed to modify vanilla source for shader: {}", veil$captureId, e);
        }
        return source;
    }
}
