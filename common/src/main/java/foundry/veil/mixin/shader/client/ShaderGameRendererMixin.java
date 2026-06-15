package foundry.veil.mixin.shader.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import foundry.veil.Veil;
import foundry.veil.api.client.render.VeilRenderBridge;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.VeilRenderer;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import foundry.veil.impl.client.render.shader.injection.ShaderInjectionManager;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Consumer;

@Mixin(GameRenderer.class)
public class ShaderGameRendererMixin {

    @Inject(method = "reloadShaders", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;shutdownShaders()V"))
    public void replaceShaders(CallbackInfo ci, @Local(ordinal = 1) List<Pair<ShaderInstance, Consumer<ShaderInstance>>> loadedShaders) {
        if (Veil.platform().hasErrors()) {
            return;
        }

        VeilRenderer renderer = VeilRenderSystem.renderer();
        ShaderInjectionManager injectionManager = renderer.getShaderInjectionManager();

        for (Pair<ShaderInstance, Consumer<ShaderInstance>> pair : loadedShaders) {
            String name = pair.getFirst().getName();
            ResourceLocation target = ResourceLocation.tryParse(name);
            if (target == null) {
                Veil.LOGGER.warn("Couldn't parse shader name '{}' as resource location", name);
                continue;
            }

            ResourceLocation replacementId = injectionManager.getReplacement(target);
            if (replacementId == null) {
                Veil.LOGGER.debug("No replacement found for {}", name);
                continue;
            }

            ShaderProgram shader = renderer.getShaderManager().getShader(replacementId);
            if (shader == null) {
                Veil.LOGGER.error("Failed to replace vanilla shader '{}': replacement '{}' not found", name, replacementId);
                continue;
            }

            ShaderInstance oldInstance = pair.getFirst();
            ShaderInstance newInstance = VeilRenderBridge.toShaderInstance(shader);
            pair.getSecond().accept(newInstance);
            Veil.LOGGER.info("Replaced vanilla shader '{}' with '{}'", name, replacementId);
            oldInstance.close();
        }
    }
}
