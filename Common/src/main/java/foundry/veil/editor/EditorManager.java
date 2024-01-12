package foundry.veil.editor;

import imgui.ImGui;
import imgui.type.ImBoolean;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;
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

        // debug editors
        this.add(new ExampleEditor());
        this.add(new PostEditor());
        this.add(new ShaderEditor());
        this.add(new TextureEditor());
        this.add(new OpenCLEditor());
        this.add(new DeviceInfoViewer());
        this.add(new DeferredEditor());

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

                    if (!editor.isEnabled()) {
                        enabled.set(false);
                    }
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
        return enabled;
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
        PreparableReloadListener[] listeners = this.editors.keySet().stream().filter(editor -> editor instanceof PreparableReloadListener).toArray(PreparableReloadListener[]::new);
        if (listeners.length == 0) {
            return preparationBarrier.wait(null);
        }
        if (listeners.length == 1) {
            return listeners[0].reload(preparationBarrier, resourceManager, prepareProfiler, applyProfiler, backgroundExecutor, gameExecutor);
        }

        // FIXME This might not work properly
        CompletableFuture<Unit> allComplete = new CompletableFuture<>();
        Set<PreparableReloadListener> preparingListeners = new HashSet<>(List.of(listeners));

        List<CompletableFuture<?>> futures = new ArrayList<>(listeners.length);
        for (PreparableReloadListener listener : listeners) {
            PreparationBarrier barrier = new PreparationBarrier() {
                @Override
                public <T> CompletableFuture<T> wait(T value) {
                    preparingListeners.remove(listener);
                    if (preparingListeners.isEmpty()) {
                        preparationBarrier.wait(null).thenRun(() -> allComplete.complete(Unit.INSTANCE));
                    }
                    return allComplete.thenApply(unused -> value);
                }
            };
            futures.add(listener.reload(barrier, resourceManager, prepareProfiler, applyProfiler, backgroundExecutor, gameExecutor));
        }

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }
}
