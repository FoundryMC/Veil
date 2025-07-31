package foundry.veil.api.client.render.texture;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.mixin.pipeline.accessor.PipelineNativeImageAccessor;
import net.minecraft.client.renderer.texture.AbstractTexture;

import static org.lwjgl.opengl.ARBDirectStateAccess.glTextureParameteri;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.*;
import static org.lwjgl.opengl.GL14C.GL_TEXTURE_LOD_BIAS;
import static org.lwjgl.opengl.GL30C.GL_TEXTURE_2D_ARRAY;

public abstract class ArrayTexture extends AbstractTexture {

    private int width;
    private int height;

    protected void init(int format, int mipmapLevel, int width, int height, int depth) {
        RenderSystem.assertOnRenderThreadOrInit();
        this.bind();
        if (mipmapLevel >= 0) {
            GlStateManager._texParameter(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAX_LEVEL, mipmapLevel);
            GlStateManager._texParameter(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_LOD, 0);
            GlStateManager._texParameter(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAX_LOD, mipmapLevel);
            GlStateManager._texParameter(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_LOD_BIAS, 0.0F);
        }

        this.width = width;
        this.height = height;
        for (int i = 0; i <= mipmapLevel; i++) {
            glTexImage3D(GL_TEXTURE_2D_ARRAY, i, format, width >> i, height >> i, depth, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0L);
        }
    }

    protected void upload(NativeImage... images) {
        this.bind();
        for (int i = 0; i < images.length; i++) {
            NativeImage image = images[i];
            if (image.getWidth() != this.width || image.getHeight() != this.height) {
                throw new IllegalArgumentException("Image dimensions don't match");
            }
            glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0, 0, 0, i, image.getWidth(), image.getHeight(), 1, image.format().glFormat(), GL_UNSIGNED_BYTE, ((PipelineNativeImageAccessor) (Object) image).getPixels());
        }
    }

    @Override
    public void setFilter(boolean blur, boolean mipmap) {
        RenderSystem.assertOnRenderThreadOrInit();
        this.blur = blur;
        this.mipmap = mipmap;
        int minFilter;
        int magFilter;
        if (blur) {
            minFilter = mipmap ? GL_LINEAR_MIPMAP_LINEAR : GL_LINEAR;
            magFilter = GL_LINEAR;
        } else {
            minFilter = mipmap ? GL_NEAREST_MIPMAP_LINEAR : GL_NEAREST;
            magFilter = GL_NEAREST;
        }

        if (VeilRenderSystem.directStateAccessSupported()) {
            int id = this.getId();
            glTextureParameteri(id, GL_TEXTURE_MIN_FILTER, minFilter);
            glTextureParameteri(id, GL_TEXTURE_MAG_FILTER, magFilter);
        } else {
            this.bind();
            glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, minFilter);
            glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, magFilter);
        }
    }

    @Override
    public int getId() {
        RenderSystem.assertOnRenderThreadOrInit();
        if (this.id == -1) {
            this.id = VeilRenderSystem.createTextures(GL_TEXTURE_2D_ARRAY);
        }

        return this.id;
    }

    @Override
    public void bind() {
        glBindTexture(GL_TEXTURE_2D_ARRAY, this.getId());
    }
}
