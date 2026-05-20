package foundry.veil.impl.client.editor;

import foundry.veil.api.client.color.Colorc;
import foundry.veil.api.client.editor.EditorAttributeProvider;
import foundry.veil.api.client.editor.SingleWindowInspector;
import foundry.veil.api.client.imgui.VeilImGuiUtil;
import foundry.veil.api.client.registry.LightTypeRegistry;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.LightData;
import foundry.veil.api.client.render.light.renderer.LightRenderHandle;
import foundry.veil.api.client.render.light.renderer.LightRenderer;
import imgui.ImGui;
import imgui.flag.ImGuiHoveredFlags;
import imgui.type.ImBoolean;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.DebugEntityNameGenerator;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class LightInspector extends SingleWindowInspector {

    public static final Component TITLE = Component.translatable("inspector.veil.light.title");

    private static final Component ADD = Component.translatable("inspector.veil.light.button.add");
    private static final Component REMOVE = Component.translatable("inspector.veil.light.button.remove");
    private static final Component REMOVE_ALL = Component.translatable("inspector.veil.light.button.remove_all");
    private static final Component REMOVE_ALL_DESC = Component.translatable("inspector.veil.light.button.remove_all.desc");
    private static final Component SET_POSITION = Component.translatable("inspector.veil.light.button.set_position");
    private static final Component ATTRIBUTES = Component.translatable("inspector.veil.light.attributes");
    private static final Component ENABLE_AO = Component.translatable("inspector.veil.light.toggle.ao");

    private final List<ResourceKey<LightTypeRegistry.LightType<?>>> lightTypes = new ArrayList<>();
    private ResourceKey<LightTypeRegistry.LightType<?>> selectedTab;

    private final ImBoolean enableAmbientOcclusion = new ImBoolean();

    @Override
    public Component getDisplayName() {
        return TITLE;
    }

    @Override
    public @Nullable Component getGroup() {
        return RENDERER_GROUP;
    }

    @Override
    public boolean isEnabled() {
        return Minecraft.getInstance().level != null && LightTypeRegistry.REGISTRY.size() > 0;
    }

    @Override
    protected void renderComponents() {
        LightRenderer lightRenderer = VeilRenderSystem.renderer().getLightRenderer();

        if (this.selectedTab == null || !LightTypeRegistry.REGISTRY.containsKey(this.selectedTab)) {
            this.selectedTab = this.lightTypes.getFirst();
        }

        LightTypeRegistry.LightType<?> lightType = LightTypeRegistry.REGISTRY.get(this.selectedTab);
        ImGui.beginDisabled(lightType == null || lightType.debugLightFactory() == null);
        if (ImGui.button(ADD.getString()) && lightType != null && lightType.debugLightFactory() != null) {
            LightTypeRegistry.DebugLightFactory factory = lightType.debugLightFactory();
            Minecraft client = Minecraft.getInstance();
            Camera mainCamera = client.gameRenderer.getMainCamera();
            lightRenderer.addLight(factory.createDebugLight(client.level, mainCamera));
        }
        ImGui.endDisabled();
        if (ImGui.isItemHovered(ImGuiHoveredFlags.None)) {
            VeilImGuiUtil.setTooltip(Component.translatable("inspector.veil.light.button.add.desc", this.selectedTab.location().toString()));
        }

        ImGui.sameLine();
        ImGui.beginDisabled(lightType == null);
        if (ImGui.button(REMOVE.getString()) && lightType != null) {
            for (LightRenderHandle<?> handle : List.copyOf(lightRenderer.getLights(lightType))) {
                handle.free();
            }
        }
        ImGui.endDisabled();
        if (ImGui.isItemHovered(ImGuiHoveredFlags.None)) {
            VeilImGuiUtil.setTooltip(Component.translatable("inspector.veil.light.button.remove.desc", this.selectedTab.location().toString()));
        }

        ImGui.sameLine();
        if (ImGui.button(REMOVE_ALL.getString())) {
            lightRenderer.free();
        }
        if (ImGui.isItemHovered(ImGuiHoveredFlags.None)) {
            VeilImGuiUtil.setTooltip(REMOVE_ALL_DESC);
        }

        ImGui.beginTabBar("##lights");
        for (ResourceKey<LightTypeRegistry.LightType<?>> key : this.lightTypes) {
            ResourceLocation id = key.location();
            if (ImGui.beginTabItem(id.toString())) {
                this.selectedTab = key;
                int i = 0;
                List<? extends LightRenderHandle<?>> lightData = List.copyOf(lightRenderer.getLights(LightTypeRegistry.REGISTRY.get(key)));
                for (LightRenderHandle<?> handle : lightData) {
                    ImGui.pushID("light" + i);
                    renderLightComponents(handle);
                    ImGui.popID();
                    i++;
                }
                ImGui.endTabItem();
            }
        }
        ImGui.endTabBar();
    }

    @Override
    public void onShow() {
        super.onShow();
        this.lightTypes.clear();
        this.lightTypes.addAll(LightTypeRegistry.REGISTRY.registryKeySet().stream().sorted(Comparator.comparing(ResourceKey::location)).toList());
    }

    private static void renderLightComponents(LightRenderHandle<?> handle) {
        LightData data = handle.getLightData();

        ImBoolean visible = new ImBoolean(true);
        ImGui.pushID(data.hashCode());
        if (ImGui.collapsingHeader(DebugEntityNameGenerator.getEntityName(new UUID(data.hashCode(), 0L)), visible)) {
            renderLightAttributeComponents(data);
            handle.markDirty();
        }
        ImGui.popID();
        if (!visible.get()) {
            handle.free();
        }
        ImGui.separator();
    }

    private static void renderLightAttributeComponents(LightData lightData) {
        Colorc lightColor = lightData.getColor();

        float[] editBrightness = new float[]{lightData.getBrightness()};
        float[] editLightColor = new float[]{lightColor.red(), lightColor.green(), lightColor.blue()};

        ImGui.indent();
        if (ImGui.dragScalar("brightness", editBrightness, 0.02F)) {
            lightData.setBrightness(editBrightness[0]);
        }
        if (ImGui.colorEdit3("color", editLightColor)) {
            lightData.setColor(editLightColor[0], editLightColor[1], editLightColor[2]);
        }

        if (ImGui.button(SET_POSITION.getString())) {
            lightData.setTo(Minecraft.getInstance().gameRenderer.getMainCamera());
        }

        ImGui.newLine();
        VeilImGuiUtil.component(ATTRIBUTES);

        if (lightData instanceof EditorAttributeProvider editorAttributeProvider) {
            editorAttributeProvider.renderImGuiAttributes();
        }
        ImGui.unindent();
    }
}
