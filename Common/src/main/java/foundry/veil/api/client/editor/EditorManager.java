package foundry.veil.api.client.editor;

import foundry.veil.api.util.CompositeReloadListener;
import imgui.ImGui;
import imgui.type.ImBoolean;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * <p>Manages all editors for Veil. Editors are ImGui powered panels that can be dynamically registered and unregistered with {@link #add(Editor)}.</p>
 *
 * @author Ocelot
 */
public class EditorManager implements PreparableReloadListener {

    private final Map<Editor, ImBoolean> editors;
    private boolean enabled;

    @ApiStatus.Internal
    public EditorManager(ReloadableResourceManager resourceManager) {
        this.editors = new TreeMap<>(Comparator.comparing(Editor::getDisplayName));
        resourceManager.registerReloadListener(this);
    }

    @ApiStatus.Internal
    public void render() {
        if (!this.enabled) {
            return;
        }

        if (ImGui.beginMainMenuBar()) {
            if (ImGui.beginMenu("Editor")) {
                for (Map.Entry<Editor, ImBoolean> entry : this.editors.entrySet()) {
                    Editor editor = entry.getKey();
                    ImBoolean enabled = entry.getValue();

                    ImGui.beginDisabled(!editor.isEnabled());
                    if (ImGui.menuItem(editor.getDisplayName(), null, enabled.get())) {
                        if (!enabled.get()) {
                            this.show(editor);
                        } else {
                            this.hide(editor);
                        }
                    }
                    ImGui.endDisabled();
                }
                ImGui.endMenu();
            }

            for (Map.Entry<Editor, ImBoolean> entry : this.editors.entrySet()) {
                Editor editor = entry.getKey();
                if (entry.getValue().get() && editor.isMenuBarEnabled()) {
                    ImGui.separator();
                    ImGui.textColored(0xFFAAAAAA, editor.getDisplayName());
                    editor.renderMenuBar();
                }
            }

            ImGui.endMainMenuBar();
        }

        for (Map.Entry<Editor, ImBoolean> entry : this.editors.entrySet()) {
            Editor editor = entry.getKey();
            ImBoolean enabled = entry.getValue();

            if (!editor.isEnabled()) {
                enabled.set(false);
            }
            if (!enabled.get()) {
                continue;
            }

            editor.render();
        }
    }

    @ApiStatus.Internal
    public void renderLast() {
        if (!this.enabled) {
            return;
        }

        for (Map.Entry<Editor, ImBoolean> entry : this.editors.entrySet()) {
            Editor editor = entry.getKey();
            ImBoolean enabled = entry.getValue();
            if (enabled.get()) {
                editor.renderLast();
            }
        }
    }

    public void show(Editor editor) {
        ImBoolean enabled = this.editors.get(editor);
        if (enabled != null && !enabled.get()) {
            editor.onShow();
            enabled.set(true);
        }
    }

    public void hide(Editor editor) {
        ImBoolean enabled = this.editors.get(editor);
        if (enabled != null && enabled.get()) {
            editor.onHide();
            enabled.set(false);
        }
    }

    public boolean isVisible(Editor editor) {
        ImBoolean visible = this.editors.get(editor);
        return visible != null && visible.get();
    }

    public synchronized void add(Editor editor) {
        this.editors.computeIfAbsent(editor, unused -> new ImBoolean());
    }

    public synchronized void remove(Editor editor) {
        this.hide(editor);
        this.editors.remove(editor);
    }

    /**
     * Toggles visibility of the ImGui overlay.
     */
    public void toggle() {
        this.enabled = !this.enabled;
    }

    /**
     * @return Whether the overlay is active
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Sets whether the overlay should be active.
     *
     * @param enabled Whether to enable the ImGui overlay
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public @NotNull CompletableFuture<Void> reload(@NotNull PreparationBarrier preparationBarrier, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller prepareProfiler, @NotNull ProfilerFiller applyProfiler, @NotNull Executor backgroundExecutor, @NotNull Executor gameExecutor) {
        PreparableReloadListener listener = CompositeReloadListener.of(this.editors.keySet().stream().filter(editor -> editor instanceof PreparableReloadListener).toArray(PreparableReloadListener[]::new));
        return listener.reload(preparationBarrier, resourceManager, prepareProfiler, applyProfiler, backgroundExecutor, gameExecutor);
    }
}
