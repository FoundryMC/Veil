package foundry.veil.editor;

import foundry.veil.render.deferred.LightRenderer;
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
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

public class DeferredEditor extends SingleWindowEditor {

    private final ImBoolean enableDeferredPipeline = new ImBoolean();
    private final ImBoolean enableVanillaLight = new ImBoolean();
    private final ImBoolean enableEntityLight = new ImBoolean();
    private final ImBoolean bakeTransparentLight = new ImBoolean();

    @Override
    public String getDisplayName() {
        return "Deferred Renderer";
    }

    @Override
    protected void renderComponents() {
        VeilRenderer renderer = VeilRenderSystem.renderer();
        ShaderPreDefinitions definitions = renderer.getShaderDefinitions();
        VeilDeferredRenderer deferredRenderer = renderer.getDeferredRenderer();
        LightRenderer lightRenderer = deferredRenderer.getLightRenderer();

        this.enableDeferredPipeline.set(deferredRenderer.getRendererState() != VeilDeferredRenderer.RendererState.DISABLED);
        if (ImGui.checkbox("Enable Pipeline", this.enableDeferredPipeline)) {
            if (this.enableDeferredPipeline.get()) {
                deferredRenderer.enable();
            } else {
                deferredRenderer.disable();
            }
            Minecraft.getInstance().levelRenderer.allChanged();
        }

        ImGui.sameLine();
        this.enableVanillaLight.set(lightRenderer.isVanillaLightEnabled());
        if (ImGui.checkbox("Enable Vanilla Light", this.enableVanillaLight)) {
            if (this.enableVanillaLight.get()) {
                lightRenderer.enableVanillaLight();
            } else {
                lightRenderer.disableVanillaLight();
            }
        }

        ImGui.sameLine();
        this.enableEntityLight.set(definitions.getDefinition(VeilDeferredRenderer.DISABLE_VANILLA_ENTITY_LIGHT_KEY) == null);
        if (ImGui.checkbox("Enable Vanilla Entity Lights", this.enableEntityLight)) {
            if (this.enableEntityLight.get()) {
                definitions.remove(VeilDeferredRenderer.DISABLE_VANILLA_ENTITY_LIGHT_KEY);
            } else {
                definitions.define(VeilDeferredRenderer.DISABLE_VANILLA_ENTITY_LIGHT_KEY);
            }
        }

        ImGui.sameLine();
        this.bakeTransparentLight.set(definitions.getDefinition(VeilDeferredRenderer.USE_BAKED_TRANSPARENT_LIGHTMAPS_KEY) != null);
        if (ImGui.checkbox("Bake Transparency Lightmaps", this.bakeTransparentLight)) {
            if (this.bakeTransparentLight.get()) {
                definitions.define(VeilDeferredRenderer.USE_BAKED_TRANSPARENT_LIGHTMAPS_KEY);
            } else {
                definitions.remove(VeilDeferredRenderer.USE_BAKED_TRANSPARENT_LIGHTMAPS_KEY);
            }
        }

        ImGui.text("Framebuffers");
        if (ImGui.beginTabBar("Framebuffers")) {
            FramebufferManager framebufferManager = renderer.getFramebufferManager();
            AdvancedFbo deferredBuffer = framebufferManager.getFramebuffer(VeilFramebuffers.DEFERRED);
            AdvancedFbo deferredFinalBuffer = framebufferManager.getFramebuffer(VeilFramebuffers.DEFERRED_FINAL);
            AdvancedFbo transparentBuffer = framebufferManager.getFramebuffer(VeilFramebuffers.TRANSPARENT);
            AdvancedFbo transparentFinalBuffer = framebufferManager.getFramebuffer(VeilFramebuffers.TRANSPARENT_FINAL);

            drawBuffers("Opaque", deferredBuffer);
            drawBuffers("Opaque Final", deferredFinalBuffer);
            drawBuffers("Transparent", transparentBuffer);
            drawBuffers("Transparent Final", transparentFinalBuffer);

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
