package foundry.veil.impl.client.render.light;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import foundry.veil.Veil;
import foundry.veil.api.client.color.Colorc;
import foundry.veil.api.client.render.CullFrustum;
import foundry.veil.api.client.render.light.data.DirectionalLightData;
import foundry.veil.api.client.render.light.renderer.LightRenderHandle;
import foundry.veil.api.client.render.light.renderer.LightRenderer;
import foundry.veil.api.client.render.light.renderer.LightTypeRenderer;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import foundry.veil.api.client.render.vertex.VertexArray;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@ApiStatus.Internal
public class DirectionalLightRenderer implements LightTypeRenderer<DirectionalLightData> {

    private static final Vector3f DIRECTION = new Vector3f();
    private static final ResourceLocation RENDER_TYPE = Veil.veilPath("light/directional");

    private final List<LightHandle> lights;
    private final VertexArray vertexArray;

    private boolean freed;

    public DirectionalLightRenderer() {
        this.lights = new LinkedList<>();

        this.vertexArray = VertexArray.create();
        this.vertexArray.upload(createMesh(), VertexArray.DrawUsage.STATIC);
        VertexArray.unbind();
    }

    private static MeshData createMesh() {
        Tesselator tesselator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION);
        LightTypeRenderer.createQuad(bufferBuilder);
        return bufferBuilder.buildOrThrow();
    }

    @Override
    public LightRenderHandle<DirectionalLightData> addLight(DirectionalLightData light) {
        LightHandle handle = new LightHandle(light);
        this.lights.add(handle);
        return handle;
    }

    @Override
    public LightRenderHandle<DirectionalLightData> steal(LightRenderHandle<DirectionalLightData> handle) {
        if (!(handle instanceof LightHandle)) {
            handle.free();
            return this.addLight(handle.getLightData());
        }
        return handle;
    }

    @Override
    public void prepareLights(LightRenderer lightRenderer, CullFrustum frustum) {
    }

    @Override
    public void renderLights(LightRenderer lightRenderer) {
        if (this.lights.isEmpty()) {
            return;
        }

        RenderType renderType = VeilRenderType.get(RENDER_TYPE);
        if (renderType == null) {
            return;
        }

        this.vertexArray.bind();
        this.vertexArray.setup(renderType);
        this.render();
        this.vertexArray.clear(renderType);
        if (renderType instanceof VeilRenderType.LayeredRenderType layeredRenderType) {
            for (RenderType layer : layeredRenderType.getLayers()) {
                this.vertexArray.setup(layer);
                this.render();
                this.vertexArray.clear(layer);
            }
        }
    }

    private void render() {
        ShaderInstance shader = RenderSystem.getShader();
        if (shader == null) {
            return;
        }

        for (LightHandle handle : this.lights) {
            DirectionalLightData light = handle.getLightData();

            Uniform lightColorUniform = shader.getUniform("LightColor");
            if (lightColorUniform != null) {
                Colorc lightColor = light.getColor();
                float brightness = light.getBrightness();
                lightColorUniform.set(lightColor.red() * brightness, lightColor.green() * brightness, lightColor.blue() * brightness);
                lightColorUniform.upload();
            }

            Uniform lightDirection = shader.getUniform("LightDirection");
            if (lightDirection != null) {
                lightDirection.set(light.getDirection().normalize(DIRECTION));
                lightDirection.upload();
            }

            this.vertexArray.draw();
        }
    }

    @Override
    public Collection<? extends LightRenderHandle<DirectionalLightData>> getLights() {
        return this.lights;
    }

    @Override
    public int getVisibleLights() {
        return this.lights.size();
    }

    @Override
    public void free() {
        this.vertexArray.close();
        this.freed = true;
    }

    private class LightHandle implements LightRenderHandle<DirectionalLightData> {

        private final DirectionalLightData data;

        private LightHandle(DirectionalLightData data) {
            this.data = data;
        }

        @Override
        public DirectionalLightData getLightData() {
            return this.data;
        }

        @Override
        public void markDirty() {
        }

        @Override
        public boolean isValid() {
            return !DirectionalLightRenderer.this.freed;
        }

        @Override
        public void free() {
            DirectionalLightRenderer.this.lights.remove(this);
        }
    }
}
