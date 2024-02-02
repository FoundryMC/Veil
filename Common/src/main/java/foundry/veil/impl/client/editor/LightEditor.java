package foundry.veil.impl.client.editor;

import foundry.veil.api.client.editor.SingleWindowEditor;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.VeilRenderer;
import foundry.veil.api.client.render.deferred.light.*;
import imgui.ImGui;
import imgui.ImVec4;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiDataType;
import imgui.flag.ImGuiHoveredFlags;
import imgui.flag.ImGuiTableColumnFlags;
import imgui.type.ImBoolean;
import imgui.type.ImDouble;
import imgui.type.ImFloat;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.*;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

public class LightEditor extends SingleWindowEditor {

    private final Light.Type[] lightTypes;

    public LightEditor() {
        this.lightTypes = Light.Type.values();
    }

    @Override
    public String getDisplayName() {
        return "Light Editor";
    }

    @Override
    public boolean isEnabled() {
        return VeilRenderSystem.renderer().getDeferredRenderer().isEnabled();
    }

    @Override
    protected void renderComponents() {
        LightRenderer lightRenderer = VeilRenderSystem.renderer().getDeferredRenderer().getLightRenderer();
        float lineHeight = ImGui.getTextLineHeightWithSpacing();

        List<Light> lights = new ArrayList<>();
        for (Light.Type lightType : this.lightTypes) {
            lights.addAll(lightRenderer.getLights(lightType));
        }

        if (ImGui.beginTable("##lights", 2)) {
            ImGui.tableSetupColumn("     Lights");
            ImGui.tableSetupColumn("Visible", ImGuiTableColumnFlags.WidthFixed, lineHeight * 3);
            ImGui.tableHeadersRow();

            ImGui.indent();
            for (int i = 0; i < lights.size(); i++) {
                Light light = lights.get(i);
                boolean visible = light.isVisible(VeilRenderer.getCullingFrustum());

                if (!visible) {
                    ImVec4 textColor = ImGui.getStyle().getColor(ImGuiCol.Text);
                    ImGui.pushStyleColor(ImGuiCol.Text, textColor.x, textColor.y, textColor.z, textColor.w * 0.5F);
                }

                ImGui.tableNextRow();
                ImGui.tableSetColumnIndex(0);

                renderLightComponents(light, i);

                ImGui.tableSetColumnIndex(1);
                ImBoolean isVisible = new ImBoolean(visible);
                ImGui.checkbox("##visible" + i, isVisible);

                if (!visible) {
                    ImGui.popStyleColor();
                }
            }

            ImGui.endTable();
        }

        ImGui.sameLine(lineHeight);
        if (ImGui.button("+", lineHeight, lineHeight)) {
            ImGui.openPopup("add_light_popup");
        }
        if (ImGui.isItemHovered(ImGuiHoveredFlags.None)) {
            ImGui.setTooltip("Add a new light to the world");
        }
        if (ImGui.beginPopup("add_light_popup")) {
            ImGui.text("Choose Light Type:");
            for (Light.Type lightType : this.lightTypes) {
                if (ImGui.selectable(lightType.name())) {
                    Light light = switch (lightType) {
                        case POINT -> new PointLight().setRadius(15.0F);
                        case AREA -> new AreaLight().setDistance(15.0F).setOrientation(new Quaternionf().lookAlong(Minecraft.getInstance().gameRenderer.getMainCamera().getLookVector().mul(-1), Minecraft.getInstance().gameRenderer.getMainCamera().getUpVector()));
                        case DIRECTIONAL -> new DirectionalLight().setDirection(0, -1, 0);
                    };

                    if (light instanceof PositionedLight positionedLight) {
                        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
                        positionedLight.setPosition(cameraPos.x(), cameraPos.y(), cameraPos.z());
                    }
                    lightRenderer.addLight(light.setColor(new Vector3f(1.0F, 1.0F, 1.0F)).setBrightness(1.0F));
                }
            }
            ImGui.endPopup();
        }
    }

    private static void renderLightComponents(Light light, int index) {
        LightRenderer lightRenderer = VeilRenderSystem.renderer().getDeferredRenderer().getLightRenderer();
        ImBoolean notDeleted = new ImBoolean(true);
        if (ImGui.collapsingHeader(light.getType().name() + "#" + light.hashCode(), notDeleted)) {
            renderLightAttributeComponents(light, index);
        }
        if (!notDeleted.get()) {
            lightRenderer.removeLight(light);
        }
        ImGui.separator();
    }

    // TODO: move these into somewhere less hard-coded.
    // probably do this when we switch from an enum?
    private static void renderLightAttributeComponents(Light light, int index) {
        Vector3fc lightColor = light.getColor();

        ImFloat editBrightness = new ImFloat(light.getBrightness());
        float[] editLightColor = new float[]{lightColor.x(), lightColor.y(), lightColor.z()};

        ImGui.indent();
        if (ImGui.dragScalar("##brightness" + index, ImGuiDataType.Float, editBrightness, 0.02F)) {
            light.setBrightness(editBrightness.get());
        }
        ImGui.sameLine(0, ImGui.getStyle().getItemInnerSpacingX());
        ImGui.text("brightness");

        if (ImGui.colorPicker3("##color" + index, editLightColor)) {
            light.setColor(editLightColor[0], editLightColor[1], editLightColor[2]);
        }
        ImGui.sameLine(0, ImGui.getStyle().getItemInnerSpacingX());
        ImGui.text("color");

        ImGui.newLine();

        switch (light.getType()) {
            case POINT -> renderPointLightAttributeComponents((PointLight) light, index);
            case AREA -> renderAreaLightAttributeComponents((AreaLight) light, index);
            case DIRECTIONAL -> renderDirectionalLightAttributeComponents((DirectionalLight) light, index);
        }
        ImGui.unindent();
    }

    private static void renderPointLightAttributeComponents(PointLight light, int index) {
        Vector3d position = light.getPosition();

        ImDouble editX = new ImDouble(position.x());
        ImDouble editY = new ImDouble(position.y());
        ImDouble editZ = new ImDouble(position.z());

        ImFloat editRadius = new ImFloat(light.getRadius());
        ImFloat editFalloff = new ImFloat(light.getFalloff());

        float totalWidth = ImGui.calcItemWidth();
        ImGui.pushItemWidth(totalWidth / 3.0F - (ImGui.getStyle().getItemInnerSpacingX() * 0.58F));
        if (ImGui.dragScalar("##x" + index, ImGuiDataType.Double, editX, 0.02F)) {
            light.setPosition(editX.get(), position.y(), position.z());
        }
        ImGui.sameLine(0, ImGui.getStyle().getItemInnerSpacingX());
        if (ImGui.dragScalar("##y" + index, ImGuiDataType.Double, editY, 0.02F)) {
            light.setPosition(position.x(), editY.get(), position.z());
        }
        ImGui.sameLine(0, ImGui.getStyle().getItemInnerSpacingX());
        if (ImGui.dragScalar("##z" + index, ImGuiDataType.Double, editZ, 0.02F)) {
            light.setPosition(position.x(), position.y(), editZ.get());
        }

        ImGui.popItemWidth();
        ImGui.sameLine(0, ImGui.getStyle().getItemInnerSpacingX());
        ImGui.text("position");

        if (ImGui.dragScalar("##radius" + index, ImGuiDataType.Float, editRadius, 0.02F, 0.0F)) {
            light.setRadius(editRadius.get());
        }
        ImGui.sameLine(0, ImGui.getStyle().getItemInnerSpacingX());
        ImGui.text("radius");

        if (ImGui.dragScalar("##falloff" + index, ImGuiDataType.Float, editFalloff, 0.01F, 0.0F)) {
            light.setFalloff(editFalloff.get());
        }
        ImGui.sameLine(0, ImGui.getStyle().getItemInnerSpacingX());
        ImGui.text("falloff");
    }

    private static void renderAreaLightAttributeComponents(AreaLight light, int index) {
        Vector2f size = light.getSize();
        Vector3d position = light.getPosition();
        Vector3f orientationAngles = light.getOrientation().getEulerAnglesXYZ(new Vector3f());

        float[] editSize = new float[]{size.x(), size.y()};

        ImDouble editX = new ImDouble(position.x());
        ImDouble editY = new ImDouble(position.y());
        ImDouble editZ = new ImDouble(position.z());

        ImFloat editXRot = new ImFloat(orientationAngles.x() * Mth.RAD_TO_DEG);
        ImFloat editYRot = new ImFloat(orientationAngles.y() * Mth.RAD_TO_DEG);
        ImFloat editZRot = new ImFloat(orientationAngles.z() * Mth.RAD_TO_DEG);

        ImFloat editAngle = new ImFloat(light.getAngle() * Mth.RAD_TO_DEG);
        ImFloat editDistance = new ImFloat(light.getDistance());
        ImFloat editFalloff = new ImFloat(light.getFalloff());

        if (ImGui.dragFloat2("##size" + index, editSize, 0.02F, 0.0001F)) {
            light.setSize(editSize[0], editSize[1]);
        }
        ImGui.sameLine(0, ImGui.getStyle().getItemInnerSpacingX());
        ImGui.text("size");

        float totalWidth = ImGui.calcItemWidth();
        ImGui.pushItemWidth(totalWidth / 3.0F - (ImGui.getStyle().getItemInnerSpacingX() * 0.58F));
        if (ImGui.dragScalar("##x" + index, ImGuiDataType.Double, editX, 0.02F)) {
            light.setPosition(editX.get(), position.y(), position.z());
        }
        ImGui.sameLine(0, ImGui.getStyle().getItemInnerSpacingX());
        if (ImGui.dragScalar("##y" + index, ImGuiDataType.Double, editY, 0.02F)) {
            light.setPosition(position.x(), editY.get(), position.z());
        }
        ImGui.sameLine(0, ImGui.getStyle().getItemInnerSpacingX());
        if (ImGui.dragScalar("##z" + index, ImGuiDataType.Double, editZ, 0.02F)) {
            light.setPosition(position.x(), position.y(), editZ.get());
        }

        ImGui.popItemWidth();
        ImGui.sameLine(0, ImGui.getStyle().getItemInnerSpacingX());
        ImGui.text("position");


        ImGui.pushItemWidth(totalWidth / 3.0F - (ImGui.getStyle().getItemInnerSpacingX() * 0.58F));
        if (ImGui.dragScalar("##xrot" + index, ImGuiDataType.Float, editXRot, 0.2F)) {
            light.setOrientation(new Quaternionf().rotationXYZ(editXRot.get() * Mth.DEG_TO_RAD, orientationAngles.y(), orientationAngles.z()));
        }
        ImGui.sameLine(0, ImGui.getStyle().getItemInnerSpacingX());
        if (ImGui.dragScalar("##yrot" + index, ImGuiDataType.Float, editYRot, 0.2F)) {
            light.setOrientation(new Quaternionf().rotationXYZ(orientationAngles.x(), editYRot.get() * Mth.DEG_TO_RAD, orientationAngles.z()));
        }
        ImGui.sameLine(0, ImGui.getStyle().getItemInnerSpacingX());
        if (ImGui.dragScalar("##zrot" + index, ImGuiDataType.Float, editZRot, 0.2F)) {
            light.setOrientation(new Quaternionf().rotationXYZ(orientationAngles.x(), orientationAngles.y(), editZRot.get() * Mth.DEG_TO_RAD));
        }

        ImGui.popItemWidth();
        ImGui.sameLine(0, ImGui.getStyle().getItemInnerSpacingX());
        ImGui.text("orientation");


        if (ImGui.dragScalar("##angle" + index, ImGuiDataType.Float, editAngle, 0.02F, 0.0F, 180.0F)) {
            light.setAngle(editAngle.get() * Mth.DEG_TO_RAD);
        }
        ImGui.sameLine(0, ImGui.getStyle().getItemInnerSpacingX());
        ImGui.text("angle");

        if (ImGui.dragScalar("##distance" + index, ImGuiDataType.Float, editDistance, 0.02F, 0.0F)) {
            light.setDistance(editDistance.get());
        }
        ImGui.sameLine(0, ImGui.getStyle().getItemInnerSpacingX());
        ImGui.text("distance");

        if (ImGui.dragScalar("##falloff" + index, ImGuiDataType.Float, editFalloff, 0.01F, 0.0F)) {
            light.setFalloff(editFalloff.get());
        }
        ImGui.sameLine(0, ImGui.getStyle().getItemInnerSpacingX());
        ImGui.text("falloff");
    }

    private static void renderDirectionalLightAttributeComponents(DirectionalLight light, int index) {
        Vector3fc direction = light.getDirection();

        float[] editDirection = new float[]{direction.x(), direction.y(), direction.z()};

        if (ImGui.dragFloat3("##direction" + index, editDirection, 0.005F)) {
            Vector3f vector = new Vector3f(editDirection).normalize();
            if (!Float.isNaN(vector.x()) && !Float.isNaN(vector.y()) && !Float.isNaN(vector.z())) {
                light.setDirection(editDirection[0], editDirection[1], editDirection[2]);
            }
        }
        ImGui.sameLine(0, ImGui.getStyle().getItemInnerSpacingX());
        ImGui.text("direction");
    }
}
