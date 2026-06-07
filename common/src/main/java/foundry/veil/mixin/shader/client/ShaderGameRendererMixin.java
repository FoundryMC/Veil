package foundry.veil.mixin.shader.client;

import foundry.veil.Veil;
import foundry.veil.api.client.render.VeilRenderBridge;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.VeilRenderer;
import foundry.veil.api.client.render.shader.ShaderInjectionManager;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.Map;

@Mixin(GameRenderer.class)
public class ShaderGameRendererMixin {

    @Shadow
    @Final
    private Map<String, ShaderInstance> shaders;

    @Inject(method = "reloadShaders", at = @At("RETURN"))
    public void replaceShaders(ResourceProvider resourceProvider, CallbackInfo ci) {
        if (Veil.platform().hasErrors()) {
            return;
        }

        VeilRenderer renderer = VeilRenderSystem.renderer();
        ShaderInjectionManager injectionManager = renderer.getShaderModificationManager();

        for (Map.Entry<String, ShaderInstance> entry : this.shaders.entrySet()) {
            String name = entry.getKey();
            ResourceLocation target = ResourceLocation.tryParse("minecraft:shaders/core/" + name + ".fsh");
            if (target == null) {
                Veil.LOGGER.warn("Couldn't parse shader name '{}' as resource location", name);
                continue;
            }

            ResourceLocation replacementId = injectionManager.getReplacement(target);
            if (replacementId == null) {
                Veil.LOGGER.debug("No replacement found for {}", target);
                continue;
            }

            ShaderProgram shader = renderer.getShaderManager().getShader(replacementId);
            if (shader != null) {
                ShaderInstance oldInstance = entry.getValue();
                ShaderInstance newInstance = VeilRenderBridge.toShaderInstance(shader);
                entry.setValue(newInstance);
                this.setShaderField(oldInstance, newInstance);
                Veil.LOGGER.info("Replaced vanilla shader '{}' with '{}'", name, replacementId);
            } else {
                Veil.LOGGER.error("Failed to replace vanilla shader '{}': replacement '{}' not found", name, replacementId);
            }
        }
    }

    private void setShaderField(ShaderInstance current, ShaderInstance instance) {
        try {
            for (Field field : GameRenderer.class.getDeclaredFields()) {
                if (ShaderInstance.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    if (field.get(this) == current) {
                        field.set(this, instance);
                        Veil.LOGGER.debug("Updated field {} with {}", field.getName(), instance.getName());
                        return;
                    }
                }
            }
            Veil.LOGGER.warn("No GameRenderer field found for shader '{}' (identity match failed)", current.getName());
        } catch (IllegalAccessException e) {
            Veil.LOGGER.error("Failed to set shader field for '{}'", instance.getName(), e);
        }
    }
}
