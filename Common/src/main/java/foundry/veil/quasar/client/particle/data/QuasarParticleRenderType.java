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
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;

public class QuasarParticleRenderType implements ParticleRenderType {
    private ResourceLocation texture;
    public QuasarParticleRenderType(){
    }

    public QuasarParticleRenderType setTexture(ResourceLocation texture){
        this.texture = texture;
        return this;
    }
    @Override
    public void begin(BufferBuilder builder, TextureManager textureManager) {
        RenderSystem.enableBlend();
        RenderSystem.depthFunc(GL11C.GL_LEQUAL);

        //
        RenderSystem.setShader(() -> RenderTypeRegistry.QUASAR_PARTICLE_ADDITIVE_MULTIPLY);
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        // opaque
//			RenderSystem.depthMask(true);
//			RenderSystem.disableBlend();
//			RenderSystem.enableLighting();

        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
    }

    @Override
    public void end(Tesselator tessellator) {
        tessellator.end();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }
}
