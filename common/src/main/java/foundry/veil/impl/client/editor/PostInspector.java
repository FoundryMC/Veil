package foundry.veil.impl.client.editor;

import foundry.veil.Veil;
import foundry.veil.api.client.editor.SingleWindowInspector;
import foundry.veil.api.client.imgui.VeilImGuiUtil;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.post.PostProcessingManager;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiDragDropFlags;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

@ApiStatus.Internal
public class PostInspector extends SingleWindowInspector {

    public static final Component TITLE = Component.translatable("inspector.veil.post.title");

    private static final Component INACTIVE = Component.translatable("inspector.veil.post.inactive");
    private static final Component ACTIVE = Component.translatable("inspector.veil.post.active");

    private final Set<ResourceLocation> removedPipelines;

    public PostInspector() {
        this.removedPipelines = new HashSet<>(1);
    }

    private static boolean isInternal(ResourceLocation id) {
        return Veil.MODID.equals(id.getNamespace()) && id.getPath().startsWith("core/");
    }

    @Override
    public void render() {
        ImGui.setNextWindowSize(600, 0);
        super.render();
    }

    @Override
    public void renderComponents() {
        this.removedPipelines.clear();
        PostProcessingManager postProcessingManager = VeilRenderSystem.renderer().getPostProcessingManager();

        float availableWidth = ImGui.getContentRegionAvailX();

        ImGui.setNextItemWidth(availableWidth / 2);
        ImGui.beginGroup();
        VeilImGuiUtil.component(INACTIVE);
        if (ImGui.beginListBox("##available_pipelines", availableWidth / 2, 0)) {
            for (ResourceLocation entry : postProcessingManager.getPipelines()) {
                if (postProcessingManager.isActive(entry) || isInternal(entry)) {
                    continue;
                }

                VeilImGuiUtil.resourceLocation(entry);

                if (ImGui.beginDragDropSource(ImGuiDragDropFlags.SourceAllowNullID)) {
                    ImGui.setDragDropPayload("INACTIVE_POST_PIPELINE", entry, ImGuiCond.Once);
                    VeilImGuiUtil.resourceLocation(entry);

                    ImGui.endDragDropSource();
                }
            }

            ImGui.endListBox();
        }

        if (ImGui.beginDragDropTarget()) {
            ResourceLocation payload = ImGui.acceptDragDropPayload("ACTIVE_POST_PIPELINE");
            if (payload != null) {
                this.removedPipelines.add(payload);
            }

            ImGui.endDragDropTarget();
        }

        ImGui.endGroup();

        ImGui.sameLine();

        ImGui.setNextItemWidth(availableWidth / 2);
        ImGui.beginGroup();
        VeilImGuiUtil.component(ACTIVE);

        if (ImGui.beginListBox("##shaders", availableWidth / 2, 0)) {
            List<PostProcessingManager.ProfileEntry> pipelines = postProcessingManager.getActivePipelines();
            ResourceLocation[] names = new ResourceLocation[pipelines.size()];

            int i = 0;
            ListIterator<PostProcessingManager.ProfileEntry> iterator = pipelines.listIterator(pipelines.size());
            while (iterator.hasPrevious()) {
                names[i++] = iterator.previous().getPipeline();
            }

            for (int j = 0; j < names.length; j++) {
                ResourceLocation id = names[j];

                ImGui.pushID(id.toString());
                VeilImGuiUtil.resourceLocation(id);

                if (ImGui.beginDragDropSource(ImGuiDragDropFlags.SourceAllowNullID)) {
                    ImGui.setDragDropPayload("ACTIVE_POST_PIPELINE", id, ImGuiCond.Once);
                    VeilImGuiUtil.resourceLocation(id);
                    ImGui.endDragDropSource();
                }

                if (ImGui.beginDragDropTarget()) {
                    ResourceLocation payload = ImGui.acceptDragDropPayload("ACTIVE_POST_PIPELINE");
                    if (payload != null) {
                        int oldIndex;
                        for (oldIndex = 0; oldIndex < names.length; oldIndex++) {
                            if (names[oldIndex].equals(payload)) {
                                break;
                            }
                        }

                        for (int k = 0; k < names.length; k++) {
                            if (k == j) { // If setting to the the current index
                                postProcessingManager.add(1001 + k, payload);
                            } else if (k == oldIndex) { // If setting to the old index
                                postProcessingManager.add(1001 + k, id);
                            } else {
                                postProcessingManager.add(1001 + k, names[k]);
                            }
                        }
                    }

                    ImGui.endDragDropTarget();
                }

                ImGui.popID();
            }

            ImGui.endListBox();
        }

        if (ImGui.beginDragDropTarget()) {
            ResourceLocation payload = ImGui.acceptDragDropPayload("INACTIVE_POST_PIPELINE");
            if (payload != null) {
                postProcessingManager.add(1000, payload);
            }

            ImGui.endDragDropTarget();
        }
        ImGui.endGroup();

        for (ResourceLocation id : this.removedPipelines) {
            postProcessingManager.remove(id);
        }
    }

    @Override
    public Component getDisplayName() {
        return TITLE;
    }

    @Override
    public Component getGroup() {
        return RENDERER_GROUP;
    }
}
