package foundry.veil.editor;

import foundry.veil.render.deferred.VeilDeferredRenderer;
import foundry.veil.render.framebuffer.AdvancedFbo;
import foundry.veil.render.framebuffer.AdvancedFboTextureAttachment;
import foundry.veil.render.framebuffer.FramebufferManager;
import foundry.veil.render.framebuffer.VeilFramebuffers;
import foundry.veil.render.pipeline.VeilRenderSystem;
import foundry.veil.render.pipeline.VeilRenderer;
import foundry.veil.render.shader.definition.ShaderPreDefinitions;
import imgui.ImGui;
import imgui.type.ImBoolean;
import org.jetbrains.annotations.Nullable;

public class DeferredEditor extends SingleWindowEditor {

    private final ImBoolean enableDeferredPipeline = new ImBoolean(true);
    private final ImBoolean enableEntityLight = new ImBoolean(true);

    @Override
    public String getDisplayName() {
        return "Deferred Renderer";
    }

    @Override
    protected void renderComponents() {
        VeilRenderer renderer = VeilRenderSystem.renderer();
        ShaderPreDefinitions definitions = renderer.getShaderDefinitions();
        VeilDeferredRenderer deferredRenderer = renderer.getDeferredRenderer();

        if (ImGui.checkbox("Enable Pipeline", this.enableDeferredPipeline)) {
            if (this.enableDeferredPipeline.get()) {
                deferredRenderer.enable();
            } else {
                deferredRenderer.disable();
            }
        }

        ImGui.sameLine();
        if (ImGui.checkbox("Enable Vanilla Entity Lights", this.enableEntityLight)) {
            if (this.enableEntityLight.get()) {
                definitions.remove(VeilDeferredRenderer.DISABLE_VANILLA_ENTITY_LIGHT_KEY);
            } else {
                definitions.define(VeilDeferredRenderer.DISABLE_VANILLA_ENTITY_LIGHT_KEY);
            }
        }

        ImGui.text("Framebuffers");
        if (ImGui.beginTabBar("Framebuffers")) {
            FramebufferManager framebufferManager = renderer.getFramebufferManager();
            AdvancedFbo deferredBuffer = framebufferManager.getFramebuffer(VeilFramebuffers.DEFERRED);
            AdvancedFbo deferredFinalBuffer = framebufferManager.getFramebuffer(VeilFramebuffers.DEFERRED_FINAL);
            AdvancedFbo transparentBuffer = framebufferManager.getFramebuffer(VeilFramebuffers.TRANSPARENT);
            AdvancedFbo transparentFinalBuffer = framebufferManager.getFramebuffer(VeilFramebuffers.TRANSPARENT_FINAL);
            AdvancedFbo lightBuffer = framebufferManager.getFramebuffer(VeilFramebuffers.LIGHT);

            drawBuffers("Opaque", deferredBuffer);
            drawBuffers("Opaque Final", deferredFinalBuffer);
            drawBuffers("Transparent", transparentBuffer);
            drawBuffers("Transparent Final", transparentFinalBuffer);
            drawBuffers("Light", lightBuffer);

            ImGui.endTabBar();
        }
    }

    private static void drawBuffers(String name, @Nullable AdvancedFbo buffer) {
        ImGui.beginDisabled(buffer == null);
        if (ImGui.beginTabItem(name)) {
            if (buffer != null) {
                int columns = (int) Math.ceil(Math.sqrt(buffer.getColorAttachments() + (buffer.isDepthTextureAttachment() ? 1 : 0)));
                float width = ImGui.getContentRegionAvailX() / columns;
                float height = width * buffer.getHeight() / buffer.getWidth();
                int i;
                for (i = 0; i < buffer.getColorAttachments(); i++) {
                    if (!buffer.isColorTextureAttachment(i)) {
                        continue;
                    }

                    if (i % columns != 0) {
                        ImGui.sameLine();
                    }
                    ImGui.beginGroup();
                    AdvancedFboTextureAttachment attachment = buffer.getColorTextureAttachment(i);
                    ImGui.text(attachment.getName() != null ? attachment.getName() : "Attachment " + i);
                    ImGui.image(attachment.getId(), width, height, 0, 1, 1, 0, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F);
                    ImGui.endGroup();
                }

                if (buffer.isDepthTextureAttachment()) {
                    if (i % columns != 0) {
                        ImGui.sameLine();
                    }
                    ImGui.beginGroup();
                    AdvancedFboTextureAttachment attachment = buffer.getDepthTextureAttachment();
                    ImGui.text(attachment.getName() != null ? attachment.getName() : "Depth");
                    ImGui.image(attachment.getId(), width, height, 0, 1, 1, 0, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F);
                    ImGui.endGroup();
                }
            }
            ImGui.endTabItem();
        }
        ImGui.endDisabled();
    }

    @Override
    public boolean isEnabled() {
        return VeilRenderSystem.renderer().getDeferredRenderer().isEnabled();
    }
}
