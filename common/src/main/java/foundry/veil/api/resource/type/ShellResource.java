package foundry.veil.api.resource.type;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.resource.VeilResourceAction;
import foundry.veil.api.resource.VeilResourceInfo;
import foundry.veil.api.resource.VeilResourceManager;
import foundry.veil.impl.resource.action.ShellInspectAction;
import foundry.veil.impl.resource.action.TextEditAction;
import imgui.extension.texteditor.TextEditorLanguage;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.util.profiling.InactiveProfiler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public record ShellResource(VeilResourceInfo resourceInfo) implements VeilTextResource<ShellResource> {

    @Override
    public List<VeilResourceAction<ShellResource>> getActions() {
        return List.of(new TextEditAction<>(), new ShellInspectAction<>());
    }

    @Override
    public boolean canHotReload() {
        return true;
    }

    @Override
    public void hotReload(VeilResourceManager resourceManager) throws IOException {
        VeilRenderSystem.renderer().getEffectManager().getShellManager().reload(CompletableFuture::completedFuture, resourceManager.resources(this.resourceInfo), InactiveProfiler.INSTANCE, InactiveProfiler.INSTANCE, Util.backgroundExecutor(), Minecraft.getInstance());
    }

    @Override
    public int getIconCode() {
        return 0xEDDF;
    }

    @Override
    public TextEditorLanguage languageDefinition() {
        return TextEditorLanguage.Json();
    }
}
