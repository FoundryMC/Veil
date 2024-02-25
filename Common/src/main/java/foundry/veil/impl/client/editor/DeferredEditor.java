package foundry.veil.impl.client.editor;

import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.api.client.editor.SingleWindowEditor;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.VeilRenderer;
import foundry.veil.api.client.render.deferred.VeilDeferredRenderer;
import foundry.veil.api.client.render.deferred.light.renderer.LightRenderer;
import foundry.veil.api.client.render.deferred.light.PointLight;
import foundry.veil.api.client.render.framebuffer.*;
import foundry.veil.api.client.render.shader.definition.ShaderPreDefinitions;
import foundry.veil.api.client.util.TextureDownloader;
import imgui.ImGui;
import imgui.type.ImBoolean;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import static org.lwjgl.opengl.GL11C.*;

@ApiStatus.Internal
public class DeferredEditor extends SingleWindowEditor {

    private final ImBoolean enableDeferredPipeline = new ImBoolean();
    private final ImBoolean enableAmbientOcclusion = new ImBoolean();
    private final ImBoolean enableVanillaLight = new ImBoolean();
    private final ImBoolean enableEntityLight = new ImBoolean();
    private final ImBoolean bakeTransparentLight = new ImBoolean();
    private AdvancedFbo downloadBuffer;

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
        }

        ImGui.sameLine();
        this.enableAmbientOcclusion.set(lightRenderer.isAmbientOcclusionEnabled());
        if (ImGui.checkbox("Enable Ambient Occlusion", this.enableAmbientOcclusion)) {
            if (this.enableAmbientOcclusion.get()) {
                lightRenderer.enableAmbientOcclusion();
            } else {
                lightRenderer.disableAmbientOcclusion();
            }
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

        LocalPlayer player = Minecraft.getInstance().player;
        ImGui.sameLine();
        ImGui.beginDisabled(player == null);
        if (ImGui.button("Add Test Light")) {
            if (player != null) {
                Vec3 pos = player.getEyePosition();
                lightRenderer.addLight(new PointLight().setPosition(pos.x, pos.y, pos.z).setColor(new Vector3f(1.0F, 1.0F, 1.0F)).setBrightness(5.0F).setRadius(15));
            }
        }
        ImGui.endDisabled();

        ImGui.text("Framebuffers");
        if (ImGui.beginTabBar("Framebuffers")) {
            FramebufferManager framebufferManager = renderer.getFramebufferManager();
            AdvancedFbo opaqueBuffer = framebufferManager.getFramebuffer(VeilFramebuffers.OPAQUE);
            AdvancedFbo opaqueLightBuffer = framebufferManager.getFramebuffer(VeilFramebuffers.OPAQUE_LIGHT);
            AdvancedFbo opaqueFinalBuffer = framebufferManager.getFramebuffer(VeilFramebuffers.OPAQUE_FINAL);
            AdvancedFbo transparentBuffer = framebufferManager.getFramebuffer(VeilFramebuffers.TRANSPARENT);
            AdvancedFbo transparentLightBuffer = framebufferManager.getFramebuffer(VeilFramebuffers.TRANSPARENT_LIGHT);
            AdvancedFbo transparentFinalBuffer = framebufferManager.getFramebuffer(VeilFramebuffers.TRANSPARENT_FINAL);

            this.drawBuffers("Opaque", opaqueBuffer);
            this.drawBuffers("Opaque Light", opaqueLightBuffer);
            this.drawBuffers("Opaque Final", opaqueFinalBuffer);
            this.drawBuffers("Transparent", transparentBuffer);
            this.drawBuffers("Transparent Light", transparentLightBuffer);
            this.drawBuffers("Transparent Final", transparentFinalBuffer);

            ImGui.endTabBar();
        }
    }

    @Override
    public void renderLast() {
        super.renderLast();

        if (this.downloadBuffer != null) {
            try {
                Minecraft client = Minecraft.getInstance();
                Path outputFolder = Paths.get(client.gameDirectory.toURI()).resolve("debug-out").resolve("deferred");
                if (!Files.exists(outputFolder)) {
                    Files.createDirectories(outputFolder);
                } else {
                    Files.walkFileTree(outputFolder, new SimpleFileVisitor<>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            Files.delete(file);
                            return FileVisitResult.CONTINUE;
                        }
                    });
                }

                List<CompletableFuture<?>> result = new LinkedList<>();
                for (int i = 0; i < this.downloadBuffer.getColorAttachments(); i++) {
                    if (this.downloadBuffer.isColorTextureAttachment(i)) {
                        AdvancedFboTextureAttachment attachment = this.downloadBuffer.getColorTextureAttachment(i);
                        String name = attachment.getName() != null ? attachment.getName() : "Attachment " + i;
                        result.add(TextureDownloader.save(name, outputFolder, attachment.getId(), true));
                    }
                }

                CompletableFuture.allOf(result.toArray(new CompletableFuture[0])).thenRunAsync(() -> Util.getPlatform().openFile(outputFolder.toFile()), client);
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.downloadBuffer = null;
        }
    }

    private void drawBuffers(String name, @Nullable AdvancedFbo buffer) {
        ImGui.beginDisabled(buffer == null);
        if (ImGui.beginTabItem(name)) {
            if (buffer != null) {
                if (ImGui.button("Download")) {
                    this.downloadBuffer = buffer;
                }

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
                    ImGui.text(this.getAttachmentName(i, attachment));
                    ImGui.image(attachment.getId(), width, height, 0, 1, 1, 0, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F);
                    ImGui.endGroup();
                }

                if (buffer.isDepthTextureAttachment()) {
                    if (i % columns != 0) {
                        ImGui.sameLine();
                    }
                    ImGui.beginGroup();
                    AdvancedFboTextureAttachment attachment = buffer.getDepthTextureAttachment();
                    ImGui.text(this.getAttachmentName(-1, attachment));
                    ImGui.image(attachment.getId(), width, height, 0, 1, 1, 0, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F);
                    ImGui.endGroup();
                }
            }
            ImGui.endTabItem();
        }
        ImGui.endDisabled();
    }

    private String getAttachmentName(int index, AdvancedFboTextureAttachment attachment) {
        RenderSystem.bindTexture(attachment.getId());
        StringBuilder attachmentName = new StringBuilder(attachment.getName() != null ? attachment.getName() : index == -1 ? "Depth" : ("Attachment " + index));

        int internalFormat = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_INTERNAL_FORMAT);
        for (FramebufferAttachmentDefinition.Format format : FramebufferAttachmentDefinition.Format.values()) {
            if (internalFormat == format.getInternalId()) {
                attachmentName.append(" (").append(format.name()).append(")");
                return attachmentName.toString();
            }
        }

        attachmentName.append(" (0x").append(Integer.toHexString(internalFormat).toUpperCase(Locale.ROOT)).append(")");
        return attachmentName.toString();
    }

    @Override
    public boolean isEnabled() {
        return VeilRenderSystem.renderer().getDeferredRenderer().isEnabled();
    }
}
