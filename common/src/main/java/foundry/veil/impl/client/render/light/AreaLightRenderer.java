package foundry.veil.impl.client.render.light;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import foundry.veil.Veil;
import foundry.veil.api.client.render.light.data.AreaLightData;
import foundry.veil.api.client.render.light.renderer.InstancedLightRenderer;
import foundry.veil.api.client.render.light.renderer.LightRenderHandle;
import foundry.veil.api.client.render.light.renderer.LightTypeRenderer;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import foundry.veil.api.client.render.vertex.VertexArrayBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@ApiStatus.Internal
public class AreaLightRenderer extends InstancedLightRenderer<AreaLightData> {

    private static final ResourceLocation RENDER_TYPE = Veil.veilPath("light/area");

    public AreaLightRenderer() {
        super(Float.BYTES * 22 + 2);
    }

    @Override
    protected MeshData createMesh() {
        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION);
        LightTypeRenderer.createInvertedCube(builder);
        return builder.buildOrThrow();
    }

    @Override
    protected void setupBufferState(VertexArrayBuilder builder) {
        builder.setVertexAttribute(1, 2, 4, VertexArrayBuilder.DataType.FLOAT, false, 0);
        builder.setVertexAttribute(2, 2, 4, VertexArrayBuilder.DataType.FLOAT, false, Float.BYTES * 4);
        builder.setVertexAttribute(3, 2, 4, VertexArrayBuilder.DataType.FLOAT, false, Float.BYTES * 8);
        builder.setVertexAttribute(4, 2, 4, VertexArrayBuilder.DataType.FLOAT, false, Float.BYTES * 12); // matrix !
        builder.setVertexAttribute(5, 2, 3, VertexArrayBuilder.DataType.FLOAT, false, Float.BYTES * 16); // color
        builder.setVertexAttribute(6, 2, 2, VertexArrayBuilder.DataType.FLOAT, false, Float.BYTES * 19); // size
        builder.setVertexAttribute(7, 2, 1, VertexArrayBuilder.DataType.UNSIGNED_SHORT, true, Float.BYTES * 21); // angle
        builder.setVertexAttribute(8, 2, 1, VertexArrayBuilder.DataType.FLOAT, false, Float.BYTES * 21 + 2); // distance
    }

    @Override
    protected @Nullable RenderType getRenderType(List<? extends LightRenderHandle<AreaLightData>> lights) {
        return VeilRenderType.get(RENDER_TYPE);
    }
}
