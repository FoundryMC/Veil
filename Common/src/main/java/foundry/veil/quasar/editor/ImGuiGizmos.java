package foundry.veil.quasar.editor;

import foundry.veil.quasar.QuasarClient;
import foundry.veil.quasar.emitters.ParticleEmitter;
import foundry.veil.quasar.emitters.modules.emitter.settings.shapes.AbstractEmitterShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class ImGuiGizmos {
    public static void renderGizmos(PoseStack stack, Camera camera, ImGuiEditorOverlay editorScreen, float partialTicks) {
        if(editorScreen != null && editorScreen.renderGizmos) {
            stack.pushPose();
            Vec3 pos = camera.getPosition();
            stack.translate(-pos.x, -pos.y, -pos.z);
            if(editorScreen.currentlySelectedEmitterInstance != null){
                Vec3 position = editorScreen.currentlySelectedEmitterInstance.getEmitterModule().getPosition();
                stack.translate(position.x, position.y, position.z);
                renderAxisGizmos(stack, editorScreen.currentlySelectedEmitterInstance, partialTicks);
                renderEmitterShape(stack, editorScreen.currentlySelectedEmitterInstance);
            }
            stack.popPose();
        }
    }


    // this is ugly as fuck but i have no energy to fix it can stay for now
    public static void renderAxisGizmos(PoseStack stack, ParticleEmitter emitter, float partialTicks){
        stack.pushPose();
        stack.translate(0, 0, 0);
        VertexConsumer consumer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.lines());
        Vec3 pos = emitter.getEmitterModule().getPosition();
        setupGizmos(stack, consumer, pos, emitter, partialTicks);
        stack.popPose();
    }

    private static void setupGizmos(PoseStack stack, VertexConsumer consumer, Vec3 pos, ParticleEmitter emitter, float partialTicks) {
        if(QuasarClient.editorScreen.localGizmos){
            stack.mulPose(Axis.YP.rotationDegrees((float) emitter.getEmitterSettingsModule().getEmissionShapeSettings().getRotation().y()));
            stack.mulPose(Axis.XP.rotationDegrees((float) emitter.getEmitterSettingsModule().getEmissionShapeSettings().getRotation().x()));
            stack.mulPose(Axis.ZP.rotationDegrees((float) emitter.getEmitterSettingsModule().getEmissionShapeSettings().getRotation().z()));
        }
        QuasarClient.yGizmo.aabb = new AABB(pos.x - 0.01f, pos.y, pos.z - 0.01f, pos.x + 0.01f, pos.y + 0.5f * emitter.getEmitterSettingsModule().getEmissionShapeSettings().getDimensions().length(), pos.z + 0.01f);
        QuasarClient.zGizmo.aabb = new AABB(pos.x - 0.01f, pos.y - 0.01f, pos.z, pos.x + 0.01f, pos.y + 0.01f, pos.z + 0.5f * emitter.getEmitterSettingsModule().getEmissionShapeSettings().getDimensions().length());
        QuasarClient.xGizmo.aabb = new AABB(pos.x, pos.y - 0.01f, pos.z - 0.01f, pos.x + 0.5f * emitter.getEmitterSettingsModule().getEmissionShapeSettings().getDimensions().length(), pos.y + 0.01f, pos.z + 0.01f);
        QuasarClient.xGizmo.position = pos;
        QuasarClient.yGizmo.position = pos;
        QuasarClient.zGizmo.position = pos;
        QuasarClient.xGizmo.rotation = new Vec3(-90, 0, -90);
        QuasarClient.yGizmo.rotation = new Vec3(0, 0, 0);
        QuasarClient.zGizmo.rotation = new Vec3(-90, 180, 0);
        QuasarClient.xGizmo.scale = new Vec3(0.1f, 0.1f, 0.1f);
        QuasarClient.yGizmo.scale = new Vec3(0.1f, 0.1f, 0.1f);
        QuasarClient.zGizmo.scale = new Vec3(0.1f, 0.1f, 0.1f);

        QuasarClient.xGizmo.render(stack, consumer, partialTicks, QuasarClient.mouseX, QuasarClient.mouseY);
        QuasarClient.yGizmo.render(stack, consumer, partialTicks, QuasarClient.mouseX, QuasarClient.mouseY);
        QuasarClient.zGizmo.render(stack, consumer, partialTicks, QuasarClient.mouseX, QuasarClient.mouseY);
    }

    public static void renderEmitterShape(PoseStack stack, ParticleEmitter emitter) {
        stack.pushPose();
        stack.translate(0, 0, 0);
        AbstractEmitterShape shape = emitter.getEmitterSettingsModule().getEmissionShapeSettings().getShape();
        Vec3 dimensions = emitter.getEmitterSettingsModule().getEmissionShapeSettings().getDimensions();
        Vec3 rotation = emitter.getEmitterSettingsModule().getEmissionShapeSettings().getRotation();
        RenderSystem.disableCull();
        stack.mulPose(Axis.YP.rotationDegrees((float) rotation.y));
        stack.mulPose(Axis.XP.rotationDegrees((float) rotation.x));
        stack.mulPose(Axis.ZP.rotationDegrees((float) rotation.z));
        VertexConsumer consumer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.lines());
        Matrix4f pose = stack.last().pose();
        shape.renderShape(stack, consumer, dimensions, rotation);
        RenderSystem.enableCull();
        stack.popPose();
    }


}
