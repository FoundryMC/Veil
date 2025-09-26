package foundry.veil.api.flare.data.effect;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.api.client.property.model.Mat4ModelProperty;
import foundry.veil.api.client.property.model.RotationModelProperty;
import foundry.veil.api.client.property.model.Vec3ModelProperty;
import foundry.veil.api.client.render.MatrixStack;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import foundry.veil.api.client.render.vertex.VertexArray;
import foundry.veil.api.flare.FlareVertexArrayExtension;
import foundry.veil.api.flare.model.BakedShell;
import foundry.veil.api.flare.data.model.FlareBakedQuad;
import foundry.veil.api.flare.modifier.PropertyModifier;
import foundry.veil.api.resource.editor.ShellInspector;
import foundry.veil.api.util.CodecUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import foundry.veil.api.flare.EffectHost;
import foundry.veil.api.flare.FlareEffectManager;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class FlareModel {
    public static final Vector4fc EMPTY_COLOR = new Vector4f();
    public static final Codec<FlareModel> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("path").forGetter(FlareModel::getShell),
            CodecUtil.VECTOR3FC_CODEC.fieldOf("positionOffset").forGetter(FlareModel::getPositionOffset),
            CodecUtil.VECTOR3FC_CODEC.fieldOf("rotationOffset").forGetter(FlareModel::getRotationOffset),
            CodecUtil.VECTOR3FC_CODEC.fieldOf("scaleOffset").forGetter(FlareModel::getScaleOffset),
            CodecUtil.singleOrList(FlareMaterial.CODEC).fieldOf("materials").forGetter(FlareModel::getMaterials)
    ).apply(instance, FlareModel::new));
    public static final Supplier<FlareVertexArrayExtension> VAO = Suppliers.memoize(() -> new FlareVertexArrayExtension(VertexArray.create()));

    public static final Matrix4f dummyMatrix = new Matrix4f();
    public static final Vector3f dummyVector = new Vector3f();

    public static final String POSITION_PROPERTY_NAME = "model::position";
    public static final String ROTATION_PROPERTY_NAME = "model::rotation";
    public static final String SCALE_PROPERTY_NAME = "model::scale";

    private final ResourceLocation shell;
    final Vec3ModelProperty positionOffset;
    final RotationModelProperty rotationOffset;
    final Vec3ModelProperty scaleOffset;
    final Mat4ModelProperty modelToWorld;
    private final List<FlareMaterial> materials;

    public FlareModel(ResourceLocation shell, Vector3fc position, Vector3fc rotation, Vector3fc scale, List<FlareMaterial> materials) {
        this.shell = shell;
        this.positionOffset = new Vec3ModelProperty(new Vector3f(position));
        this.rotationOffset = new RotationModelProperty(new Vector3f(rotation));
        this.scaleOffset = new Vec3ModelProperty(new Vector3f(scale));
        this.modelToWorld = new Mat4ModelProperty(new Matrix4f());
        this.materials = materials;
    }

    public void render(EffectHost host, MatrixStack matrixStack, Map<String, List<PropertyModifier<?>>> modifiers, @Nullable Map<ResourceLocation, BakedShell> shellOverrides) {

        Vector3fc positionOffset = this.positionOffset.getValue();
        Vector3fc scaleOffset = this.scaleOffset.getValue();

        matrixStack.matrixPush();
        matrixStack.translate(positionOffset.x(), positionOffset.y(), positionOffset.z());
        matrixStack.rotate(rotationOffset.getRotation());
        matrixStack.applyScale(scaleOffset.x(), scaleOffset.y(), scaleOffset.z());
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

        modelToWorld.modify(
                matrixStack.position().translateLocal((float) cameraPos.x, (float) cameraPos.y, (float) cameraPos.z, dummyMatrix),
                PropertyModifier.PropertyModifierMode.REPLACE,
                Optional.empty()
        );

        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
        BakedShell bakedShell = (shellOverrides != null && shellOverrides.containsKey(shell)) ?
                shellOverrides.get(shell) :
                FlareEffectManager.getInstance().getShellManager().getBakedShell(shell);

        List<FlareBakedQuad> quads = bakedShell.getQuads();
        for (int i = 0, quadsSize = quads.size(); i < quadsSize; i++) {
            FlareBakedQuad quad = quads.get(i);
            quad.putBakedQuadInto(builder, matrixStack.pose());
        }

        FlareVertexArrayExtension vao = VAO.get();
        vao.upload(builder.buildOrThrow(), VertexArray.DrawUsage.STATIC);
        vao.setIndexCount(vao.getIndexCount(), VertexArray.IndexType.SHORT);

        vao.bind();
        for (int i = 0, materialsSize = materials.size(); i < materialsSize; i++) {
            FlareMaterial material = materials.get(i);
            RenderType renderType = VeilRenderType.get(material.renderTypeLocation());
            if (renderType == null) continue;
            vao.addSetup(() -> material.applyProperties(host, VeilRenderSystem.getShader(), modifiers));
            vao.addClear(() -> material.resetProperties(host, VeilRenderSystem.getShader()));
            vao.drawWithRenderType(renderType);
        }
        VertexArray.unbind();
        matrixStack.matrixPop();

    }

//Vector3fc positionOffset = this.positionOffset.getValue();
//        Vector3fc scaleOffset = this.scaleOffset.getValue();
//        matrixStack.matrixPush();
//        matrixStack.translate(positionOffset.x(), positionOffset.y(), positionOffset.z());
//        matrixStack.translate(0.5f, 0.0f, 0.5f);
//        matrixStack.rotate(rotationOffset.getRotation());
//        matrixStack.applyScale(scaleOffset.x(), scaleOffset.y(), scaleOffset.z());
//        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
//        modelToWorld.overrideUnsafe(PropertyModifier.PropertyModifierMode.REPLACE, matrixStack.position().translateLocal((float) cameraPos.x, (float) cameraPos.y, (float) cameraPos.z, dummyMatrix), Optional.empty());
//
//        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
//        BakedShell bakedShell = Flare.getInstance().getShellManager().getBakedShell(shell);
//
//        for (FlareBakedQuad quad : bakedShell.getQuads()) {
//            quad.putBakedQuadInto(buffer, matrixStack.pose(), EMPTY_COLOR);
//        }
//
//        RenderType[] renderTypes = new RenderType[materials.size()];
//        for (int i = 0, materialsSize = materials.size(); i < materialsSize; i++) {
//            renderTypes[i] = VeilRenderType.get(materials.get(i).renderTypeLocation());
//        }
//        RenderType renderType = VeilRenderType.layered(renderTypes);
//        if (renderType != null) {
//            for (FlareMaterial material : materials) {
//                ((RenderTypeExtension) renderType).cassini$addPreDraw(() -> material.applyProperties(VeilRenderSystem.getShader()));
//            }
//            renderType.draw(buffer.buildOrThrow());
//        }
//        matrixStack.matrixPop();

    public ResourceLocation getShell() {
        return shell;
    }

    public Vector3fc getPositionOffset() {
        return positionOffset.getValue();
    }

    public Vector3fc getRotationOffset() {
        return rotationOffset.getValue();
    }

    public Vector3fc getScaleOffset() {
        return scaleOffset.getValue();
    }

    public List<FlareMaterial> getMaterials() {
        return materials;
    }
}
