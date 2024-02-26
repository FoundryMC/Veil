package foundry.veil.impl.client.editor;

import foundry.veil.api.client.editor.EditorAttributeProvider;
import foundry.veil.api.client.editor.SingleWindowEditor;
import foundry.veil.api.client.registry.LightTypeRegistry;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.deferred.light.Light;
import foundry.veil.api.client.render.deferred.light.renderer.LightRenderer;
import imgui.ImGui;
import imgui.flag.ImGuiDataType;
import imgui.flag.ImGuiHoveredFlags;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3fc;

import java.util.List;
import java.util.Map;

public class LightEditor extends SingleWindowEditor {

    private ResourceKey<LightTypeRegistry.LightType<?>> selectedTab;

    @Override
    public String getDisplayName() {
        return "Light Editor";
    }

    @Override
    public boolean isEnabled() {
        return VeilRenderSystem.renderer().getDeferredRenderer().isEnabled() && Minecraft.getInstance().level != null && LightTypeRegistry.REGISTRY.size() > 0;
    }

    @Override
    protected void renderComponents() {
        LightRenderer lightRenderer = VeilRenderSystem.renderer().getDeferredRenderer().getLightRenderer();

        if (this.selectedTab == null || !LightTypeRegistry.REGISTRY.containsKey(this.selectedTab)) {
            this.selectedTab = LightTypeRegistry.REGISTRY.registryKeySet().iterator().next();
        }

        LightTypeRegistry.LightType<?> lightType = LightTypeRegistry.REGISTRY.get(this.selectedTab);
        ImGui.beginDisabled(lightType == null || lightType.debugLightFactory() == null);
        if (ImGui.button("Add Light") && lightType != null && lightType.debugLightFactory() != null) {
            LightTypeRegistry.DebugLightFactory factory = lightType.debugLightFactory();
            Minecraft client = Minecraft.getInstance();
            Camera mainCamera = client.gameRenderer.getMainCamera();
            lightRenderer.addLight(factory.createDebugLight(client.level, mainCamera));
        }
        ImGui.endDisabled();
        if (ImGui.isItemHovered(ImGuiHoveredFlags.None)) {
            ImGui.setTooltip("Add a new " + this.selectedTab.location() + " light to the world");
        }

        ImGui.sameLine();
        ImGui.beginDisabled(lightType == null);
        if (ImGui.button("Remove Lights") && lightType != null) {
            for (Light light : lightRenderer.getLights(lightType)) {
                lightRenderer.removeLight(light);
            }
        }
        ImGui.endDisabled();
        if (ImGui.isItemHovered(ImGuiHoveredFlags.None)) {
            ImGui.setTooltip("Removes all " + this.selectedTab.location() + " lights");
        }

        ImGui.sameLine();
        if (ImGui.button("Remove All Lights")) {
            lightRenderer.free();
        }
        if (ImGui.isItemHovered(ImGuiHoveredFlags.None)) {
            ImGui.setTooltip("Removes all light types");
        }

        ImGui.beginTabBar("lights");
        for (Map.Entry<ResourceKey<LightTypeRegistry.LightType<?>>, LightTypeRegistry.LightType<?>> entry : LightTypeRegistry.REGISTRY.entrySet()) {
            ResourceLocation id = entry.getKey().location();
            if (ImGui.beginTabItem(id.toString())) {
                this.selectedTab = entry.getKey();
                List<Light> lights = lightRenderer.getLights(entry.getValue());
                for (int i = 0; i < lights.size(); i++) {
                    ImGui.pushID("light" + i);
                    renderLightComponents(lights.get(i));
                    ImGui.popID();
                }
                ImGui.endTabItem();
            }
        }
        ImGui.endTabBar();
    }

    private static void renderLightComponents(Light light) {
        ImBoolean visible = new ImBoolean(true);
        ImGui.pushID(light.hashCode());
        if (ImGui.collapsingHeader("0x%X".formatted(light.hashCode()), visible)) {
            renderLightAttributeComponents(light);
        }
        ImGui.popID();
        if (!visible.get()) {
            VeilRenderSystem.renderer().getDeferredRenderer().getLightRenderer().removeLight(light);
        }
        ImGui.separator();
    }

    private static void renderLightAttributeComponents(Light light) {
        Vector3fc lightColor = light.getColor();

        ImFloat editBrightness = new ImFloat(light.getBrightness());
        float[] editLightColor = new float[]{lightColor.x(), lightColor.y(), lightColor.z()};

        ImGui.indent();
        if (ImGui.dragScalar("brightness", ImGuiDataType.Float, editBrightness, 0.02F)) {
            light.setBrightness(editBrightness.get());
        }
        if (ImGui.colorEdit3("color", editLightColor)) {
            light.setColor(editLightColor[0], editLightColor[1], editLightColor[2]);
        }
        
        if (ImGui.button("Set Position/Rotation to View")) {
            light.setTo(Minecraft.getInstance().gameRenderer.getMainCamera());
        }

        ImGui.newLine();
        ImGui.text("Attributes:");

        if (light instanceof EditorAttributeProvider editorAttributeProvider) {
            editorAttributeProvider.renderImGuiAttributes();
        }
        ImGui.unindent();
    }
}
