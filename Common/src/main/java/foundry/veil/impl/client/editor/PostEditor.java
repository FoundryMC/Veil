package foundry.veil.impl.client.editor;

import foundry.veil.api.client.editor.SingleWindowEditor;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.post.PostProcessingManager;
import imgui.ImGui;
import imgui.flag.ImGuiDataType;
import imgui.type.ImInt;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashSet;
import java.util.Set;

@ApiStatus.Internal
public class PostEditor extends SingleWindowEditor {

    private final Set<ResourceLocation> removedPipelines;

    public PostEditor() {
        this.removedPipelines = new HashSet<>(1);
    }

    @Override
    public void renderComponents() {
        this.removedPipelines.clear();
        PostProcessingManager postProcessing = VeilRenderSystem.renderer().getPostProcessingManager();
        float lineHeight = ImGui.getTextLineHeightWithSpacing();

        ImGui.text("Active Pipelines:");
        if (ImGui.beginListBox("##shaders", -Float.MIN_VALUE, 0)) {
            for (PostProcessingManager.ProfileEntry entry : postProcessing.getActivePipelines()) {
                ResourceLocation id = entry.getPipeline();
                ImInt editPriority = new ImInt(entry.getPriority());

                ImGui.pushID(id.toString());
                ImGui.text(id.toString());

                ImGui.sameLine(ImGui.getContentRegionAvailX() / 2);
                ImGui.setNextItemWidth(-lineHeight - ImGui.getStyle().getCellPaddingX() * 2);
                if (ImGui.dragScalar("##priority", ImGuiDataType.S32, editPriority, 1)) {
                    entry.setPriority(editPriority.get());
                }

                ImGui.sameLine();
                if (ImGui.button("X", lineHeight, lineHeight)) {
                    this.removedPipelines.add(id);
                }

                ImGui.popID();
            }

            ImGui.endListBox();
        }

        ImGui.text("Add Pipeline:");
        if (ImGui.beginListBox("##available_pipelines", -Float.MIN_VALUE, 0)) {
            for (ResourceLocation entry : postProcessing.getPipelines()) {
                if (postProcessing.isActive(entry)) {
                    continue;
                }
                if (ImGui.selectable(entry.toString(), false)) {
                    postProcessing.add(entry);
                }
            }
            ImGui.endListBox();
        }

        for (ResourceLocation id : this.removedPipelines) {
            postProcessing.remove(id);
        }
    }

    @Override
    public String getDisplayName() {
        return "Post Shaders";
    }
}
