package foundry.veil.api.client.render.framebuffer;

import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.ext.VeilDebug;
import foundry.veil.api.client.render.texture.TextureFilter;
import foundry.veil.impl.client.render.framebuffer.AdvancedFboImpl;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.Objects;

import static org.lwjgl.opengl.ARBDirectStateAccess.glNamedFramebufferTexture;
import static org.lwjgl.opengl.ARBDirectStateAccess.glTextureParameteri;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.*;
import static org.lwjgl.opengl.GL14C.GL_TEXTURE_LOD_BIAS;
import static org.lwjgl.opengl.GL30C.*;

/**
 * An attachment for an {@link AdvancedFboImpl} that represents a color texture buffer.
 *
 * @author Ocelot
 */
public class AdvancedFboTextureAttachment extends AbstractTexture implements AdvancedFboAttachment {

    private final int attachmentType;
    private final int format;
    private final int internalFormat;
    private final int width;
    private final int height;
    private final int mipmapLevels;
    private final TextureFilter filter;
    private final String name;

    /**
     * Creates a new attachment that adds a texture.
     *
     * @param attachmentType The attachment point to put this on
     * @param format         The format of the image data when initializing
     * @param internalFormat The internal format of the image data
     * @param width          The width of the attachment
     * @param height         The height of the attachment
     * @param mipmapLevels   The number of mipmaps levels to have
     * @param name           The custom name of this attachment for shader references
     */
    public AdvancedFboTextureAttachment(int attachmentType,
                                        int internalFormat,
                                        int format,
                                        int width,
                                        int height,
                                        int mipmapLevels,
                                        TextureFilter filter,
                                        @Nullable String name) {
        this.attachmentType = attachmentType;
        this.format = format;
        this.internalFormat = internalFormat;
        this.width = width;
        this.height = height;
        this.mipmapLevels = mipmapLevels;
        this.filter = filter;
        this.name = name;
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
            int texture = this.getId();
            glTextureParameteri(texture, GL_TEXTURE_MIN_FILTER, minFilter);
            glTextureParameteri(texture, GL_TEXTURE_MAG_FILTER, magFilter);
        } else {
            this.bind();
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minFilter);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, magFilter);
        }
    }

    @Override
    public void attach(AdvancedFbo framebuffer, int attachment) {
        Validate.isTrue(this.attachmentType < GL_DEPTH_ATTACHMENT || attachment == 0, "Only one depth buffer attachment is supported.");

        int id = this.getId();
        if (VeilRenderSystem.directStateAccessSupported()) {
            glNamedFramebufferTexture(framebuffer.getId(),
                    this.attachmentType + attachment,
                    id,
                    0); // Only draw into the first level
        } else {
            glFramebufferTexture2D(GL_FRAMEBUFFER,
                    this.attachmentType + attachment,
                    GL_TEXTURE_2D,
                    id,
                    0); // Only draw into the first level
        }

        String debugLabel = framebuffer.getDebugLabel();
        if (debugLabel != null) {
            String label = switch (this.attachmentType) {
                case GL_DEPTH_ATTACHMENT -> "Advanced Fbo " + debugLabel + " Depth Texture";
                case GL_DEPTH_STENCIL_ATTACHMENT -> "Advanced Fbo " + debugLabel + " Depth Stencil Texture";
                default -> "Advanced Fbo " + debugLabel + " Texture " + (this.name != null ? this.name : attachment);
            };
            VeilDebug.get().objectLabel(GL_TEXTURE, id, label);
        }
    }

    // Don't use DSA here so the unsized internal formats are valid
    @Override
    public void create() {
        this.bind();
        this.setFilter(this.filter.blur(), this.filter.mipmap());
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, this.mipmapLevels - 1);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_LOD, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LOD, this.mipmapLevels - 1);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_LOD_BIAS, 0.0F);
        this.filter.applyToTextureTarget(GL_TEXTURE_2D);

        for (int i = 0; i < this.mipmapLevels; i++) {
            glTexImage2D(GL_TEXTURE_2D, i, this.internalFormat, this.width >> i, this.height >> i, 0, this.format, GL_UNSIGNED_BYTE, (ByteBuffer) null);
        }
    }

    @Override
    public int getId() {
        RenderSystem.assertOnRenderThreadOrInit();
        if (this.id == -1) {
            this.id = VeilRenderSystem.createTextures(GL_TEXTURE_2D);
        }

        return this.id;
    }

    @Override
    public AdvancedFboTextureAttachment clone() {
        return new AdvancedFboTextureAttachment(this.attachmentType, this.internalFormat, this.format, this.width, this.height, this.mipmapLevels, this.filter, this.name);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        AdvancedFboTextureAttachment that = (AdvancedFboTextureAttachment) o;
        return this.attachmentType == that.attachmentType && this.internalFormat == that.internalFormat && this.width == that.width && this.height == that.height && this.mipmapLevels == that.mipmapLevels && this.filter.equals(that.filter) && Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode() {
        int result = this.attachmentType;
        result = 31 * result + this.internalFormat;
        result = 31 * result + this.width;
        result = 31 * result + this.height;
        result = 31 * result + this.mipmapLevels;
        result = 31 * result + this.filter.hashCode();
        result = 31 * result + Objects.hashCode(this.name);
        return result;
    }

    @Override
    public void bindAttachment() {
        this.bind();
    }

    @Override
    public void unbindAttachment() {
        VeilRenderSystem.renderThreadExecutor().execute(() -> RenderSystem.bindTexture(0));
    }

    @Override
    public int getAttachmentType() {
        return this.attachmentType;
    }

    @Override
    public int getFormat() {
        return this.internalFormat;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    @Override
    public int getLevels() {
        return this.mipmapLevels;
    }

    public TextureFilter getFilter() {
        return this.filter;
    }

    @Override
    public boolean canSample() {
        return true;
    }

    @Override
    public @Nullable String getName() {
        return this.name;
    }

    @Override
    public void free() {
        this.releaseId();
    }

    @Override
    public void load(@NotNull ResourceManager manager) {
    }
}
