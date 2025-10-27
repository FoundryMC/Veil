package foundry.veil.api.resource.type;

import foundry.veil.api.resource.VeilResourceAction;
import foundry.veil.api.resource.VeilResourceInfo;
import foundry.veil.api.resource.VeilResourceManager;
import foundry.veil.impl.flare.FlareManager;
import foundry.veil.impl.resource.action.TemplateInspectAction;
import foundry.veil.impl.resource.action.TextEditAction;
import imgui.extension.texteditor.TextEditorLanguageDefinition;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.util.profiling.InactiveProfiler;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public record FlareResource(VeilResourceInfo resourceInfo) implements VeilTextResource<FlareResource> {

    @Override
    public List<VeilResourceAction<FlareResource>> getActions() {
        return this.resourceInfo.location().getPath().startsWith("flare/templates") ?
                List.of(new TextEditAction<>(), new TemplateInspectAction<>()) :
                List.of(new TextEditAction<>());
    }

    @Override
    public boolean canHotReload() {
        return true;
    }

    @Override
    public void hotReload(VeilResourceManager resourceManager) throws IOException {
        FlareManager.Reloader.INSTANCE.reload(CompletableFuture::completedFuture, resourceManager.resources(this.resourceInfo), InactiveProfiler.INSTANCE, InactiveProfiler.INSTANCE, Util.backgroundExecutor(), Minecraft.getInstance());
    }

    @Override
    public int getIconCode() {
        return 0xEA1C;
    }

    @Override
    public @Nullable TextEditorLanguageDefinition languageDefinition() {
        return null;
    }
}
