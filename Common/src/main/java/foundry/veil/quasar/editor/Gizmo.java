package foundry.veil.quasar.editor;

import foundry.veil.quasar.QuasarClient;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.concurrent.atomic.AtomicBoolean;

public class Gizmo {
    public AABB aabb;
    public float r;
    public float g;
    public float b;
    public float a;
    Vec3 position;
    Vec3 rotation;
    Vec3 scale;
    public Vec3 intersectionPoint = null;

    public Gizmo(AABB aabb, float r, float g, float b, float a, Vec3 position, Vec3 rotation, Vec3 scale) {
        this.aabb = aabb;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
    }

    public boolean isMouseInAABB(){
        // raycast from player to infinity
        Vec3 start = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        Vector3f lookDir = Minecraft.getInstance().gameRenderer.getMainCamera().getLookVector();
        Vec3 look = new Vec3(lookDir.x(), lookDir.y(), lookDir.z()).normalize();
        Vec3 end = start.add(look.scale(1000));
        AtomicBoolean hit = new AtomicBoolean(false);
        // check if any pos on the ray is in the aabb
        for(int i = 0; i < 10000; i++){
            Vec3 pos = start.add(look.scale(i*0.01f));
            aabb.inflate(0.1f).clip(start, pos).ifPresent(vec3 -> {
                intersectionPoint = vec3;
                hit.set(true);
            });
            if(hit.get())
                break;
        }
        return hit.get();
    }

    public static Vec3 getAABBCorner(int index, AABB aabb){
        return switch (index) {
            case 0 -> new Vec3(aabb.minX, aabb.minY, aabb.minZ);
            case 1 -> new Vec3(aabb.minX, aabb.minY, aabb.maxZ);
            case 2 -> new Vec3(aabb.minX, aabb.maxY, aabb.minZ);
            case 3 -> new Vec3(aabb.minX, aabb.maxY, aabb.maxZ);
            case 4 -> new Vec3(aabb.maxX, aabb.minY, aabb.minZ);
            case 5 -> new Vec3(aabb.maxX, aabb.minY, aabb.maxZ);
            case 6 -> new Vec3(aabb.maxX, aabb.maxY, aabb.minZ);
            case 7 -> new Vec3(aabb.maxX, aabb.maxY, aabb.maxZ);
            default -> Vec3.ZERO;
        };
    }
    public void render(PoseStack stack, VertexConsumer consumer, float partialTicks, double mouseX, double mouseY){
        stack.pushPose();
        stack.translate(-position.x, -position.y, -position.z);
        boolean isSelected = QuasarClient.currentlySelectedGizmo == this;
        if(!isSelected){
            LevelRenderer.renderLineBox(stack, consumer, aabb, r, g, b, a);
        } else {
            LevelRenderer.renderLineBox(stack, consumer, aabb, 1, 1, 1, 1);
        }
        stack.popPose();
    }
}
