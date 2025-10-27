package foundry.veil.api.resource.editor;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import foundry.veil.api.client.imgui.VeilImGuiUtil;
import foundry.veil.api.client.render.MatrixStack;
import foundry.veil.api.flare.EffectHost;
import foundry.veil.api.flare.data.effect.FlareEffectTemplate;
import foundry.veil.api.resource.VeilEditorEnvironment;
import foundry.veil.api.resource.VeilResourceInfo;
import foundry.veil.api.resource.VeilResourceManager;
import foundry.veil.api.resource.type.FlareResource;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import it.unimi.dsi.fastutil.objects.Object2FloatArrayMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

import java.io.Reader;
import java.lang.Math;

import static foundry.veil.Veil.LOGGER;

/**
 * Viewer for effect templates. Adapted from {@link BlockModelInspector}
 *
 * @author GuyApooye
 */
public class EffectInspector implements ResourceFileEditor<FlareResource>, EffectHost {

    private static final Component TITLE = Component.translatable("inspector.veil.effect.title");
    private static final PoseStack POSE_STACK = new PoseStack();

    private final ImBoolean open;
    private final VeilResourceManager resourceManager;
    private final FlareResource resource;
    private final ImVec2 mouseDragDelta;
    private final Object2FloatMap<String> values;
    private String selectedValue;
    private double offsetXRot;
    private double offsetYRot;
    private float cameraDistance;

    private @Nullable FlareEffectTemplate template;

    public EffectInspector(VeilEditorEnvironment environment, FlareResource resource) {
        this.open = new ImBoolean(true);
        this.resourceManager = environment.getResourceManager();
        this.resource = resource;
        this.mouseDragDelta = new ImVec2();
        this.values = new Object2FloatArrayMap<>();
        this.offsetXRot = Math.toRadians(45.0);
        this.offsetYRot = Math.toRadians(30.0);
        this.cameraDistance = 10.0f;
        this.loadFromDisk();
    }

    @Override
    public void render() {
        if (this.resource == null || !this.open.get()) {
            return;
        }

        VeilResourceInfo resourceInfo = this.resource.resourceInfo();
        ImGui.setNextWindowSizeConstraints(256.0F, 256.0F, Float.MAX_VALUE, Float.MAX_VALUE);
        ImGui.setNextWindowSize(256.0F, 256.0F, ImGuiCond.Once);
        if (ImGui.begin(TITLE.getString() + "###shell_editor_" + resourceInfo.fileName(), this.open, ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoSavedSettings)) {
            VeilImGuiUtil.resourceLocation(resourceInfo.location());

            if (this.selectedValue == null && !this.values.isEmpty()) {
                this.selectedValue = this.values.object2FloatEntrySet().iterator().next().getKey();
            }
            if (this.selectedValue != null) {
                float[] value = new float[]{this.getValue(this.selectedValue)};
                if (ImGui.beginCombo("##values", this.selectedValue + " = " + value[0])) {
                    for (String name : this.values.keySet()) {
                        if (ImGui.selectable(name)) {
                            this.selectedValue = name;
                        }
                    }
                    ImGui.endCombo();
                }
                if (ImGui.dragFloat("##value", value, 0.05f)) {
                    this.values.put(this.selectedValue, value[0]);
                }
            }

            int desiredWidth = ((int) ImGui.getContentRegionAvailX() - 2) * 2;
            int desiredHeight = ((int) ImGui.getContentRegionAvailY() - 2) * 2;

            if (desiredWidth <= 0 || desiredHeight <= 0) {
                ImGui.end();
                return;
            }

            int texture = VeilImGuiUtil.renderArea(desiredWidth, desiredHeight, fbo -> {

                Quaterniond cameraOrientation = new Quaterniond().rotateX(this.offsetXRot).rotateY(this.offsetYRot);
                Vector3d cameraPos = cameraOrientation.transformInverse(new Vector3d(0.0, 0.0, this.cameraDistance)).add(0.0, 0.0, 0.0);

                Matrix4f viewMatrix = new Matrix4f().rotate(new Quaternionf(cameraOrientation)).translate((float) -cameraPos.x, (float) -cameraPos.y, (float) -cameraPos.z);

                float aspect = (float) desiredWidth / (float) desiredHeight;
                Matrix4f projMat = new Matrix4f().perspective((float) Math.toRadians(40.0), aspect, 0.3f, 1000.0f);
                Matrix4f modelView = new Matrix4f().mul(viewMatrix);

                if (this.template == null) {
                    return;
                }
                Matrix4fStack stack = RenderSystem.getModelViewStack();

                stack.pushMatrix();
                stack.set(modelView);
                RenderSystem.applyModelViewMatrix();
                RenderSystem.backupProjectionMatrix();
                RenderSystem.setProjectionMatrix(projMat, VertexSorting.ORTHOGRAPHIC_Z);

                this.template.render(this, (MatrixStack) POSE_STACK, 0.0f, null);

                stack.popMatrix();
                RenderSystem.restoreProjectionMatrix();
                RenderSystem.applyModelViewMatrix();
            });

            if (ImGui.beginChild("3D View", desiredWidth / 2.0F + 2, desiredHeight / 2.0F + 2, false, ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoMove)) {
                if (ImGui.isWindowHovered()) {
                    this.applyCameraChanges();
                }
                ImGui.image(texture, desiredWidth / 2.0F, desiredHeight / 2.0F, 0, 1, 1, 0, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.1F);
            }
            ImGui.endChild();
        }
        ImGui.end();
    }

    private void applyCameraChanges() {
        if (ImGui.isMouseDragging(ImGuiMouseButton.Left)) {
            ImGui.getMouseDragDelta(this.mouseDragDelta);
            ImGui.resetMouseDragDelta();
            this.offsetXRot += this.mouseDragDelta.y * 0.01f;
            this.offsetYRot += this.mouseDragDelta.x * 0.01f;
        }

        this.cameraDistance -= ImGui.getIO().getMouseWheel();
    }

    @Override
    public boolean isClosed() {
        return !this.open.get();
    }

    @Override
    public FlareResource getResource() {
        return this.resource;
    }

    @Override
    public void close() {
        EffectHost.super.close();
    }

    @Override
    public void loadFromDisk() {
        try (Reader reader = this.resource.resourceInfo().openAsReader(this.resourceManager)) {
            JsonElement element = JsonParser.parseReader(reader);
            DataResult<FlareEffectTemplate> result = FlareEffectTemplate.CODEC.parse(JsonOps.INSTANCE, element);

            if (result.error().isPresent()) {
                throw new JsonSyntaxException(result.error().get().message());
            }

            this.template = result.getOrThrow();

        } catch (Exception e) {
            LOGGER.error("Failed to load template", e);
        }
    }

    @Override
    public float getValue(String name) {
        return this.values.computeIfAbsent(name, n -> 0.0f);
    }

    @Override
    public String getName() {
        return "inspector";
    }

    @Override
    public void update(float partialTick) {
    }
}
