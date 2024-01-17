package foundry.veil.quasar.client.particle.data;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import foundry.veil.api.client.render.shader.RenderTypeRegistry;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

public class QuasarParticleRenderType implements ParticleRenderType {
    private ResourceLocation texture;

    public QuasarParticleRenderType() {
    }

    public QuasarParticleRenderType setTexture(ResourceLocation texture) {
        this.texture = texture;
        return this;
    }

    @Override
    public void begin(BufferBuilder builder, TextureManager textureManager) {
        RenderSystem.setShader(() -> RenderTypeRegistry.QUASAR_PARTICLE_ADDITIVE_MULTIPLY);
        RenderSystem.setShaderTexture(0, this.texture);

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

//        RenderSystem.depthMask(true);
//        RenderSystem.disableBlend();

        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
    }

    @Override
    public void end(Tesselator tessellator) {
        tessellator.end();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }
}
