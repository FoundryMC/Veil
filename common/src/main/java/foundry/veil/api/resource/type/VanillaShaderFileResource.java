package foundry.veil.api.resource.type;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.resource.VeilResourceAction;
import foundry.veil.api.resource.VeilResourceInfo;
import foundry.veil.api.resource.VeilResourceManager;
import foundry.veil.impl.resource.action.TextEditAction;
import foundry.veil.mixin.debug.accessor.DebugGameRendererAccessor;
import imgui.extension.texteditor.TextEditorLanguage;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@ApiStatus.Internal
public record VanillaShaderFileResource(VeilResourceInfo resourceInfo) implements VeilShaderResource<VanillaShaderFileResource> {

    @Override
    public List<VeilResourceAction<VanillaShaderFileResource>> getActions() {
        return List.of(new TextEditAction<>());
    }

    @Override
    public boolean canHotReload() {
        return true;
    }

    @Override
    public void hotReload(VeilResourceManager resourceManager) {
        // I would rather reload only the specific shader, but it's more compatible to reload all shaders
        VeilRenderSystem.renderer().getVanillaShaderCompiler().reload(((DebugGameRendererAccessor) Minecraft.getInstance().gameRenderer).getShaders().values());
    }
}
