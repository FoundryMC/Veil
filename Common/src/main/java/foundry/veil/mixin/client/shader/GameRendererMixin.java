package foundry.veil.mixin.client.shader;

import com.mojang.blaze3d.vertex.VertexFormat;
import foundry.veil.render.pipeline.VeilRenderSystem;
import foundry.veil.render.pipeline.VeilRenderer;
import foundry.veil.render.shader.VanillaShaderImportProcessor;
import foundry.veil.render.shader.modifier.ReplaceShaderModification;
import foundry.veil.render.shader.modifier.ShaderModification;
import foundry.veil.render.shader.program.ShaderProgram;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.Collection;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Shadow
    @Final
    static Logger LOGGER;

    /**
     * This is needed to replace the shader instance value when shader replacement is used
     *
     * @author Ocelot
     */
    @Redirect(method = "reloadShaders", at = @At(value = "NEW", args = "class=net/minecraft/client/renderer/ShaderInstance"))
    public ShaderInstance veil$replaceShaders(ResourceProvider resourceProvider, String name, VertexFormat vertexFormat) throws IOException {
        ResourceLocation loc = new ResourceLocation(name);
        ResourceLocation id = new ResourceLocation(loc.getNamespace(), "shaders/core/" + loc.getPath());

        VeilRenderer renderer = VeilRenderSystem.renderer();
        Collection<ShaderModification> modifiers = renderer.getShaderModificationManager().getModifiers(id);
        if (modifiers.size() == 1) {
            ShaderModification modification = modifiers.iterator().next();
            if (modification instanceof ReplaceShaderModification replaceModification) {
                ShaderProgram shader = renderer.getShaderManager().getShader(replaceModification.veilShader());
                if (shader == null) {
                    LOGGER.error("Failed to replace vanilla shader '{}' with veil shader: {}", loc, replaceModification.veilShader());
                } else {
                    return shader.toShaderInstance();
                }
            }
        }

        return new ShaderInstance(resourceProvider, name, vertexFormat);
    }

    @Inject(method = "preloadUiShader", at = @At("HEAD"))
    public void veil$preLoadUIShader(ResourceProvider resourceProvider, CallbackInfo ci) {
        VanillaShaderImportProcessor.setup(resourceProvider);
    }

    @Inject(method = "preloadUiShader", at = @At("RETURN"))
    public void veil$postLoadUIShader(ResourceProvider resourceProvider, CallbackInfo ci) {
        VanillaShaderImportProcessor.free();
    }

    @Inject(method = "reloadShaders", at = @At("HEAD"))
    public void veil$preLoadShaders(ResourceProvider resourceProvider, CallbackInfo ci) {
        VanillaShaderImportProcessor.setup(resourceProvider);
    }

    @Inject(method = "reloadShaders", at = @At("RETURN"))
    public void veil$postLoadShaders(ResourceProvider resourceProvider, CallbackInfo ci) {
        VanillaShaderImportProcessor.free();
    }
}
