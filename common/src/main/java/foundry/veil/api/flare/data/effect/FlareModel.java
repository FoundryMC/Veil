package foundry.veil.api.flare.data.effect;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.api.client.property.model.Mat4ModelProperty;
import foundry.veil.api.client.property.model.RotationModelProperty;
import foundry.veil.api.client.property.model.Vec3ModelProperty;
import foundry.veil.api.client.render.MatrixStack;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import foundry.veil.api.client.render.vertex.VertexArray;
import foundry.veil.api.flare.EffectHost;
import foundry.veil.api.flare.FlareEffectManager;
import foundry.veil.api.flare.model.BakedShell;
import foundry.veil.api.flare.modifier.PropertyModifier;
import foundry.veil.api.util.CodecUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @since 2.5.0
 */
public class FlareModel {

    public static final Codec<FlareModel> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("path").forGetter(FlareModel::getShell),
            CodecUtil.VECTOR3FC_CODEC.fieldOf("positionOffset").forGetter(FlareModel::getPositionOffset),
            CodecUtil.VECTOR3FC_CODEC.fieldOf("rotationOffset").forGetter(FlareModel::getRotationOffset),
            CodecUtil.VECTOR3FC_CODEC.fieldOf("scaleOffset").forGetter(FlareModel::getScaleOffset),
            CodecUtil.singleOrList(FlareMaterial.CODEC).fieldOf("materials").forGetter(FlareModel::getMaterials)
    ).apply(instance, FlareModel::new));
    
    public static final Matrix4f MODEL_TO_LOCAL_MATRIX = new Matrix4f();
    public static final Matrix4f MATRIX4F = new Matrix4f();

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
        Quaternionfc rotationOffset = this.rotationOffset.getRotation();
        Vector3fc scaleOffset = this.scaleOffset.getValue();
        
        MODEL_TO_LOCAL_MATRIX.set(matrixStack.position());
        MODEL_TO_LOCAL_MATRIX.translate(positionOffset.x(), positionOffset.y(), positionOffset.z());
        MODEL_TO_LOCAL_MATRIX.rotate(rotationOffset);
        MODEL_TO_LOCAL_MATRIX.scale(scaleOffset.x(), scaleOffset.y(), scaleOffset.z());
        
        BakedShell bakedShell = (shellOverrides != null && shellOverrides.containsKey(this.shell)) ?
                shellOverrides.get(this.shell) :
                FlareEffectManager.getInstance().getBakedShell(this.shell);
        
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        
        this.modelToWorld.modify(
                MODEL_TO_LOCAL_MATRIX.translate((float) cameraPos.x, (float) cameraPos.y, (float) cameraPos.z, MATRIX4F),
                PropertyModifier.PropertyModifierMode.REPLACE,
                Optional.empty()
        );

        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        MATRIX4F.set(modelViewStack);
        modelViewStack.mul(MODEL_TO_LOCAL_MATRIX);
        RenderSystem.applyModelViewMatrix();
        
        VertexArray vertexArray = bakedShell.getVertexArray();
        vertexArray.bind();
        
        List<FlareMaterial> flareMaterials = this.materials;
        for (int i = 0, size = flareMaterials.size(); i < size; i++) {
            FlareMaterial material = flareMaterials.get(i);
            RenderType renderType = VeilRenderType.get(material.renderTypeLocation());
            if (renderType == null) {
                continue;
            }
            
            this.draw(renderType, vertexArray, host, material, modifiers);
        }
        VertexArray.unbind();
        
        modelViewStack.set(MATRIX4F);
        RenderSystem.applyModelViewMatrix();
    }

    private void draw(RenderType renderType, VertexArray vertexArray, EffectHost host, FlareMaterial material, Map<String, List<PropertyModifier<?>>> modifiers) {
        while (renderType instanceof VeilRenderType.RenderTypeWrapper wrapper) {
            renderType = wrapper.get();
        }

        if (renderType == null) {
            return;
        }

        vertexArray.setup(renderType);
        ShaderInstance currentShader = RenderSystem.getShader();
        material.applyProperties(host, currentShader, modifiers);
        vertexArray.draw();
        material.resetProperties(host, currentShader);
        vertexArray.clear(renderType);

        if (renderType instanceof VeilRenderType.LayeredRenderType layeredRenderType) {
            List<RenderType> layers = layeredRenderType.getLayers();
            for (int i = 0, size = layers.size(); i < size; i++) {
                RenderType layer = layers.get(i);
                vertexArray.setup(layer);
                currentShader = RenderSystem.getShader();
                material.applyProperties(host, currentShader, modifiers);
                vertexArray.draw();
                material.resetProperties(host, currentShader);
                vertexArray.clear(layer);
            }
        }
    }

    public ResourceLocation getShell() {
        return this.shell;
    }

    public Vector3fc getPositionOffset() {
        return this.positionOffset.getValue();
    }

    public Vector3fc getRotationOffset() {
        return this.rotationOffset.getValue();
    }

    public Vector3fc getScaleOffset() {
        return this.scaleOffset.getValue();
    }

    public List<FlareMaterial> getMaterials() {
        return this.materials;
    }
}
