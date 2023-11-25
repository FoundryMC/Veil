package foundry.veil.model.graveyard.update;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import foundry.veil.model.graveyard.render.mesh.ModelMesh;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InterpolatedBone {
    public float x, y, z, pX, pY, pZ;
    public Quaternionf rotation, pRotation;
    protected Quaternionf currentRotation;
    public float xSize, ySize, zSize, pXSize, pYSize, pZSize;

    public float initialX, initialY, initialZ;
    public Quaternionf initialRotation;
    public float initialXSize, initialYSize, initialZSize;

    @Nullable
    public InterpolatedBone parent;
    public List<InterpolatedBone> children;

    public final String identifier;
    public boolean shouldRender = true;

    // list of all parents, starting from the root and going down
    public List<InterpolatedBone> parentChain;

    public InterpolatedBone(String identifier) {
        this.identifier = identifier;

        this.rotation = new Quaternionf();
        this.pRotation = new Quaternionf();
        this.currentRotation = new Quaternionf();
        this.initialRotation = new Quaternionf();

        this.xSize = 1.0F;
        this.ySize = 1.0F;
        this.zSize = 1.0F;
        this.pXSize = 1.0F;
        this.pYSize = 1.0F;
        this.pZSize = 1.0F;
        this.initialXSize = 1.0F;
        this.initialYSize = 1.0F;
        this.initialZSize = 1.0F;

        this.children = new ArrayList<>();
        this.parentChain = new ArrayList<>();
    }

    public void setInitialTransform(float x, float y, float z, Quaternionf rotation) {
        this.initialX = x;
        this.initialY = y;
        this.initialZ = z;
        this.x = this.initialX;
        this.y = this.initialY;
        this.z = this.initialZ;
        this.pX = this.initialX;
        this.pY = this.initialY;
        this.pZ = this.initialZ;
        this.initialRotation.set(rotation);
        this.rotation.set(this.initialRotation);
        this.pRotation.set(this.initialRotation);
        this.currentRotation.set(this.initialRotation);
    }

    public void reset() {
        this.x = this.initialX;
        this.y = this.initialY;
        this.z = this.initialZ;
        this.rotation.set(this.initialRotation);
        this.xSize = this.initialXSize;
        this.ySize = this.initialYSize;
        this.zSize = this.initialZSize;
    }

    protected void updatePreviousPosition() {
        this.pX = this.x;
        this.pY = this.y;
        this.pZ = this.z;
        this.pRotation.set(this.rotation);
        this.pXSize = this.xSize;
        this.pYSize = this.ySize;
        this.pZSize = this.zSize;
    }

    public void setGlobalSpaceRotation(Quaternionf globalSpaceRotation) {
        Quaternionf parentRotation = new Quaternionf();

        //add together the rotations of all parents.
        for (InterpolatedBone bone : this.parentChain) {
            parentRotation.mul(bone.rotation);
        }

        //subtract that from the global space rotation

        parentRotation.difference(globalSpaceRotation, this.rotation);
    }

    protected void tick(float deltaTime) {}

    public void transform(PoseStack pPoseStack, float partialTick) {
        pPoseStack.translate(Mth.lerp(partialTick, pX, x), Mth.lerp(partialTick, pY, y), Mth.lerp(partialTick, pZ, z));
        this.currentRotation = pRotation.slerp(rotation, partialTick, currentRotation);
        this.currentRotation.normalize();
        pPoseStack.mulPose(this.currentRotation);
        pPoseStack.scale(Mth.lerp(partialTick, pXSize, xSize), Mth.lerp(partialTick, pYSize, ySize), Mth.lerp(partialTick, pZSize, zSize));
    }

    public void render(Map<String, ModelMesh> meshes, float partialTick, PoseStack pPoseStack, VertexConsumer pVertexConsumer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha, boolean drawChildren) {
        if (!shouldRender) return;
        ModelMesh mesh = meshes.getOrDefault(this.identifier, ModelMesh.EMPTY);

        pPoseStack.pushPose();

        this.transform(pPoseStack, partialTick);
        mesh.render(this, pPoseStack, pVertexConsumer,pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);

        if (drawChildren) {
            for (InterpolatedBone child : this.children) {
                child.render(meshes, partialTick, pPoseStack, pVertexConsumer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha, true);
            }
        }


        pPoseStack.popPose();
    }

    public void addChild(InterpolatedBone children) {
        if (children.parent != null) {
            children.parent.children.remove(children);
        }

        this.children.add(children);
        children.parent = this;
    }

    public void setParent(InterpolatedBone parent) {
        this.parent = parent;
        parent.children.add(this);
    }

    public Matrix4f getModelSpaceTransformMatrix(PoseStack pPoseStack, float partialTick) {
        InterpolatedBone parent = this.parent;
        if (parent != null) {
            parent.getModelSpaceTransformMatrix(pPoseStack, partialTick);
        }
        this.transform(pPoseStack, partialTick);

        return pPoseStack.last().pose();
    }

    public void rotate(float angle, Direction.Axis axis) {
        switch (axis) {
            case X -> this.rotation.rotateX(angle);
            case Y -> this.rotation.rotateY(angle);
            case Z -> this.rotation.rotateZ(angle);
        }
    }
}
