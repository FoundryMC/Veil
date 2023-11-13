package foundry.veil.quasar.editor;

import foundry.veil.quasar.QuasarClient;
import foundry.veil.quasar.emitters.anchors.AnchorPoint;
import foundry.veil.quasar.util.ModelPartExtension;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.math.Axis;
import imgui.ImGui;
import imgui.ImGuiIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;


public class ImGuiEditorScreen extends Screen {
    public ImGuiEditorScreen() {
        super(Component.empty());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private float zoom = 0f;
    private float dragRotationX = 0f;
    private float dragRotationY = 0f;
    private float dragPanX = 0f;
    private float dragPanY = 0f;

    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        PoseStack ps = guiGraphics.pose();
        if (QuasarClient.editorScreen.currentlySelectedEntity != null) {
            Entity entity = QuasarClient.editorScreen.currentlySelectedEntity;
            float entityBBSize = (float) entity.getBoundingBox().getSize();
            float zoomScaled = (float) Math.max(-0.75, zoom - 0.25f * entityBBSize);
            float scaleTranslation = -1 - zoomScaled;
            ps.pushPose();
            ps.scale(0.15f, 0.15f, 0.15f);
            ps.translate(0, scaleTranslation, -2);
            ps.scale(1 + zoomScaled, 1 + zoomScaled, 1 + zoomScaled);
            ps.translate(-dragPanX, dragPanY, 0);
            ps.translate(0, -scaleTranslation, 0);
            ps.mulPose(Axis.XP.rotationDegrees((float) Math.toDegrees(dragRotationY)));
            ps.mulPose(Axis.YP.rotationDegrees((float) Math.toDegrees(dragRotationX)));
            ps.translate(0, scaleTranslation, 0);

            RenderSystem.backupProjectionMatrix();
            RenderSystem.setProjectionMatrix(new Matrix4f().setPerspective(90.0f, (float) this.width / this.height, 0.3f, 3000.0f), VertexSorting.DISTANCE_TO_ORIGIN);
            MultiBufferSource.BufferSource boxSource = Minecraft.getInstance().renderBuffers().bufferSource();
            ps.pushPose();
            ps.scale(5000f, 5000f, 5000f);
            ps.translate(0, -1, 0);
            float yRot = 0;
            Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity).render(entity, 0, pPartialTick, ps, boxSource, LightTexture.FULL_BRIGHT);
            if (entity instanceof LivingEntity le) {
                LivingEntityRenderer<?, ?> renderer = (LivingEntityRenderer<?, ?>) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(le);
                Class<LivingEntityRenderer<?, ?>> clazz = (Class<LivingEntityRenderer<?, ?>>) renderer.getClass();
                EntityModel<LivingEntity> model = (EntityModel<LivingEntity>) clazz.cast(renderer).getModel();
                Map<String, ModelPart> parts = mapRenderers(model);
                // this is really ugly pls ignore
                if (QuasarClient.editorScreen.currentlySelectedEntityModelPartName == null)
                    QuasarClient.editorScreen.currentlySelectedEntityModelPartName = parts.keySet().stream().findFirst().get();
                if (QuasarClient.editorScreen.currentlySelectedEntityModelParts == null)
                    QuasarClient.editorScreen.currentlySelectedEntityModelParts = new ArrayList<>(parts.keySet());
                if (QuasarClient.editorScreen.currentlySelectedEntityModelPart == null)
                    QuasarClient.editorScreen.currentlySelectedEntityModelPart = parts.values().stream().findFirst().get();
                if (QuasarClient.editorScreen.modelParts == null)
                    QuasarClient.editorScreen.modelParts = parts;
                QuasarClient.editorScreen.root = parts.containsKey("root") ? parts.get("root") : parts.values().stream().findFirst().get();
                Vector3f v =new Vector3f(0,0,0);
                v.add(0.0f, entity.getBbHeight() / 2f, 0f);
                AnchorPoint.TEST_POINT.origin = v;// its unused
                AnchorPoint.TEST_POINT.modelParts = getModelPartTree(QuasarClient.editorScreen.currentlySelectedEntityModelPartName, QuasarClient.editorScreen.root);
                yRot = QuasarClient.editorScreen.root.yRot;
            }
            ps.popPose();

            ps.pushPose();
            ps.scale(5000f, 5000f, 5000f);
            ps.translate(0, -1, 0);
            ps.mulPose(Axis.YP.rotationDegrees(entity.getYRot()));
            AnchorPoint.TEST_POINT.render(ps, boxSource, 1 + zoomScaled);
            boxSource.endBatch();
            ps.popPose();
            RenderSystem.restoreProjectionMatrix();

            ps.popPose();
        }
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    public static Map<String, ModelPart> mapRenderers(Model model) {
        Map<String, ModelPart> renderers = new HashMap<>();
        Class<?> i = model.getClass();
        while (i != null && i != Object.class) {
            for (Field field : i.getDeclaredFields()) {
                if (!field.isSynthetic()) {
                    if (ModelPart.class.isAssignableFrom(field.getType())) {
                        try {
                            field.setAccessible(true);
                            ModelPart part = (ModelPart) field.get(model);
                            renderers.put(((ModelPartExtension)(Object)part).getName(), part);
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
            i = i.getSuperclass();
        }
        return renderers;
    }

    public Map<String, ModelPart> getModelParts(LivingEntity entity, float pPartialTicks) {
        LivingEntityRenderer<?, ?> renderer = (LivingEntityRenderer<?, ?>) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
        Class<LivingEntityRenderer<?, ?>> clazz = (Class<LivingEntityRenderer<?, ?>>) renderer.getClass();
        EntityModel<LivingEntity> model = (EntityModel<LivingEntity>) clazz.cast(renderer).getModel();
        if (model instanceof HierarchicalModel<?>) {
            Map<String, ModelPart> parts = recursivelyGetModelParts(((HierarchicalModel<?>) model).root());
            return parts;
        }
        Class<EntityModel<?>> modelClass = (Class<EntityModel<?>>) model.getClass();
        Map<String, ModelPart> parts = Arrays.stream(modelClass.getFields()).filter(field -> field.getType().equals(ModelPart.class)).collect(Collectors.toMap(Field::getName, field -> {
            try {
                field.setAccessible(true);
                return (ModelPart) field.get(model);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        }));
        return parts;
    }

    public Map<String, ModelPart> recursivelyGetModelParts(ModelPart root) {
        Map<String, ModelPart> parts = new HashMap<>();
        parts.put("root", root);
        root.children.forEach((name, child) -> {
            parts.putAll(recursivelyGetModelParts("root." + name, child));
        });
        return parts;
    }

    public Map<String, ModelPart> recursivelyGetModelParts(String id, ModelPart root) {
        Map<String, ModelPart> parts = new HashMap<>();
        parts.put(id, root);
        root.children.forEach((name, child) -> {
            parts.putAll(recursivelyGetModelParts(id + "." + name, child));
        });
        return parts;
    }

    public static List<ModelPart> getModelPartTree(String path, ModelPart root) {
        List<ModelPart> parts = new ArrayList<>();
        parts.add(root);
        if(root.children.isEmpty()) return parts;
        path = path.substring(path.indexOf('.') + 1);
        String[] split = path.split("\\.");
        // remove "root" from split
        if (split[0].equals("root")) {
            split = Arrays.copyOfRange(split, 1, split.length);
        }
        ModelPart part = root;
        for (String s : split) {
            if(part.hasChild(s)){
                part = part.getChild(s);
                parts.add(part);
            }
        }
        return parts;
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        ImGuiIO io = ImGui.getIO();
        double screenThirdWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 4d;
        if (pMouseX > screenThirdWidth && pMouseX < screenThirdWidth * 3 && !io.getWantCaptureMouse()) {
            if (pButton == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                dragRotationX += pDragX / 50f;
                dragRotationY += pDragY / 50f;
            }
            if (pButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                dragPanX += -pDragX / 100f;
                dragPanY += -pDragY / 100f;
            }
        }
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        ImGuiIO io = ImGui.getIO();
        // calculate the x range of the middle third of the screen
        double screenThirdWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 4d;
        // if mouse is in middle third of screen, scroll
        if (pMouseX > screenThirdWidth && pMouseX < screenThirdWidth * 3 && !io.getWantCaptureMouse()) {
            if (zoom < 0) zoom = (float) (zoom + pDelta / 10f);
            else zoom = (float) (zoom + pDelta / 10f);
            if (zoom > 100) zoom = 100;
            zoom = Math.max(zoom, -0.5f);
        }
        return super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (pKeyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }
}
