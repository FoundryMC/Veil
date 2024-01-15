package foundry.veil.api.client.graveyard.skeleton;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import foundry.veil.api.client.graveyard.AnimationProperties;
import foundry.veil.api.client.graveyard.constraint.Constraint;
import foundry.veil.api.client.graveyard.render.mesh.DynamicMesh;
import foundry.veil.api.client.graveyard.render.mesh.ModelMesh;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class InterpolatedSkeleton {

    private final List<InterpolatedBone> roots;
    private final List<Constraint> constraints;
    private final Map<String, InterpolatedBone> parts;
    private final Map<String, ModelMesh> meshes;
    private final Map<String, DynamicMesh> dynamicMeshes;

    private int ticksExisted;

    public InterpolatedSkeleton() {
        this.roots = new ArrayList<>();
        this.constraints = new ArrayList<>();
        this.parts = new HashMap<>();
        this.meshes = new HashMap<>();
        this.dynamicMeshes = new HashMap<>();
    }

    protected void updatePreviousPosition() {
        for (InterpolatedBone part : this.parts.values()) {
            part.updatePreviousPosition();
        }
    }

    public void tick(AnimationProperties properties) {
        this.ticksExisted++;
        this.updatePreviousPosition();
        for (InterpolatedBone part : this.parts.values()) {
            part.tick(1.0F / 20.0F);
        }
        this.animate(properties);
        for (Constraint constraint : this.constraints) {
            constraint.apply();
        }
        //this.applyConstraints(8);
    }

    public void addAnimationProperties(AnimationProperties properties, InterpolatedSkeletonParent parent) {
        if (parent instanceof LivingEntity entity) {
            properties.addProperty("entity", entity);
            properties.addProperty("limbSwing", entity.walkAnimation.position());
            properties.addProperty("limbSwingAmount", entity.walkAnimation.speed());
            properties.addProperty("ageInTicks", entity.tickCount);
            properties.addProperty("bodyYaw", 180 - entity.yBodyRot);
            properties.addProperty("netHeadYaw", -(entity.yHeadRot - entity.yBodyRot));
            properties.addProperty("headPitch", -entity.getViewXRot(1.0F));
        }
    }

    public abstract void animate(AnimationProperties properties);

//    protected void applyConstraints(int iterations) {
//        for (Constraint constraint : this.constraints) {
//            constraint.initialize();
//        }
//
//        int satisfiedConstraints = 0;
//        for (int i = 0; i < iterations; i++) {
//            for (Constraint constraint : this.constraints) {
//                if (!constraint.isSatisfied()) {
//                    constraint.apply();
//                } else {
//                    satisfiedConstraints++;
//                }
//            }
//
//            if (satisfiedConstraints == this.constraints.size()) {
//                return;
//            }
//        }
//    }

    public void render(PoseStack pPoseStack, VertexConsumer pVertexConsumer, int pPackedLight, int pPackedOverlay, float partialTicks, float pRed, float pGreen, float pBlue, float pAlpha) {
        for (Map.Entry<String, DynamicMesh> entry : this.dynamicMeshes.entrySet()) {
            entry.getValue().update(this.parts.get(entry.getKey()), this, this.ticksExisted, partialTicks);
        }

        for (InterpolatedBone part : this.roots) {
            part.render(this.meshes, partialTicks, pPoseStack, pVertexConsumer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha, true);
        }
    }

    public void renderDebug(InterpolatedSkeletonParent entity, PoseStack poseStack, MultiBufferSource buffer, float partialTicks) {
        for (Constraint constraint : this.constraints) {
            constraint.renderDebugInfo(this, entity, partialTicks, poseStack, buffer);
        }
    }

    public void addBone(InterpolatedBone part, ModelMesh mesh) {
        this.parts.put(part.identifier, part);
        this.meshes.put(part.identifier, mesh);
        if (mesh instanceof DynamicMesh dynamicMesh) {
            this.dynamicMeshes.put(part.identifier, dynamicMesh);
        }
    }

    public void addConstraint(Constraint constraint) {
        this.constraints.add(constraint);
    }

    public void buildRoots() {
        for (InterpolatedBone part : this.parts.values()) {
            if (part.parent == null) {
                this.roots.add(part);
                continue;
            }

            InterpolatedBone parentBone = part.parent;
            while (parentBone != null) {
                part.parentChain.add(0, parentBone);
                parentBone = parentBone.parent;
            }
        }
    }
}
