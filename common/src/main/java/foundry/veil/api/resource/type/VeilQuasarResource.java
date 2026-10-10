package foundry.veil.api.resource.type;

import foundry.veil.api.quasar.data.QuasarParticles;
import foundry.veil.api.resource.VeilResourceAction;
import foundry.veil.api.resource.VeilResourceInfo;
import foundry.veil.api.resource.VeilResourceManager;
import foundry.veil.impl.resource.action.TextEditAction;
import imgui.extension.texteditor.TextEditorLanguage;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.util.profiling.InactiveProfiler;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@ApiStatus.Internal
public record VeilQuasarResource(VeilResourceInfo resourceInfo) implements VeilTextResource<VeilQuasarResource> {

    @Override
    public List<VeilResourceAction<VeilQuasarResource>> getActions() {
        // TODO add quasar edit action
        return List.of(new TextEditAction<>());
    }

    @Override
    public boolean canHotReload() {
        return true;
    }

    @Override
    public void hotReload(VeilResourceManager resourceManager) throws IOException {
        QuasarParticles.Reloader.INSTANCE.reload(CompletableFuture::completedFuture, resourceManager.resources(this.resourceInfo), InactiveProfiler.INSTANCE, InactiveProfiler.INSTANCE, Util.backgroundExecutor(), Minecraft.getInstance());
    }

    @Override
    public int getIconCode() {
        return 0xED0F; // Text file icon
    }

    @Override
    public TextEditorLanguage languageDefinition() {
        return TextEditorLanguage.Json();
    }
}
