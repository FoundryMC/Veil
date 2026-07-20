package foundry.veil.impl.client.render.light;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import foundry.veil.Veil;
import foundry.veil.api.client.render.light.data.PointLightData;
import foundry.veil.api.client.render.light.renderer.*;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import foundry.veil.api.client.render.vertex.VertexArrayBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

import java.util.List;

@ApiStatus.Internal
public class InstancedPointLightRenderer extends InstancedLightRenderer<PointLightData> implements DDALightRenderer<PointLightData> {

    private static final ResourceLocation RENDER_TYPE = Veil.veilPath("light/point");
    private static final ResourceLocation INSCATTERING_RENDER_TYPE = Veil.veilPath("light/inscattering/point");

    public InstancedPointLightRenderer() {
        super(Float.BYTES * 9);
    }

    @Override
    protected MeshData createMesh() {
        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION);
        LightTypeRenderer.createInvertedCube(builder);
        return builder.buildOrThrow();
    }

    @Override
    protected void setupBufferState(VertexArrayBuilder builder) {
        builder.setVertexAttribute(1, 2, 3, VertexArrayBuilder.DataType.FLOAT, false, 0); // LightPosition
        builder.setVertexAttribute(2, 2, 3, VertexArrayBuilder.DataType.FLOAT, false, Float.BYTES * 3); // Color
        builder.setVertexAttribute(3, 2, 1, VertexArrayBuilder.DataType.FLOAT, false, Float.BYTES * 6); // Distance
        builder.setVertexAttribute(4, 2, 1, VertexArrayBuilder.DataType.FLOAT, false, Float.BYTES * 7); // Occluded
        builder.setVertexAttribute(5, 2, 1, VertexArrayBuilder.DataType.FLOAT, false, Float.BYTES * 8); // In-scattering
    }

    @Override
    protected @Nullable RenderType getRenderType(List<? extends LightRenderHandle<PointLightData>> lights) {
        return VeilRenderType.get(RENDER_TYPE);
    }

    @Override
    protected @Nullable RenderType getInscatteringRenderType(List<? extends LightRenderHandle<PointLightData>> lights) {
        return VeilRenderType.get(INSCATTERING_RENDER_TYPE);
    }

    @Override
    public void uploadVoxelGridUniforms(int voxelGridTexture, Vector3fc voxelGridOrigin) {
        RenderType renderType = VeilRenderType.get(RENDER_TYPE);
        if (renderType == null) {
            return;
        }

        ResourceLocation veilShaderId = VeilRenderType.getShards(renderType).veilShaderId();
        if (veilShaderId != null) {
            DDALightRenderer.uploadVoxelGridUniforms(veilShaderId, voxelGridTexture, voxelGridOrigin);
        }
    }
}
