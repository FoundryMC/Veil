package foundry.veil.editor;

import foundry.veil.render.pipeline.VeilRenderSystem;
import foundry.veil.render.post.PostProcessingManager;
import imgui.ImGui;
import imgui.flag.ImGuiDataType;
import imgui.flag.ImGuiSelectableFlags;
import imgui.type.ImInt;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

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
        if (ImGui.beginListBox("##shaders", -Float.MIN_VALUE, 8 * lineHeight)) {
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
        if (ImGui.beginListBox("##available_pipelines", -Float.MIN_VALUE, 8 * lineHeight)) {
            for (ResourceLocation entry : postProcessing.getPipelines()) {
                if (postProcessing.isActive(entry)) {
                    continue;
                }
                if (ImGui.selectable(entry.toString(), false, ImGuiSelectableFlags.AllowDoubleClick)) {
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
