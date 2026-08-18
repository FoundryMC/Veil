package foundry.veil.api.client.render.framebuffer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.api.client.render.post.PostProcessingManager;
import foundry.veil.api.client.render.texture.TextureFilter;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL14C.*;
import static org.lwjgl.opengl.GL21C.GL_SRGB8;
import static org.lwjgl.opengl.GL21C.GL_SRGB8_ALPHA8;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL31C.*;
import static org.lwjgl.opengl.GL33C.GL_RGB10_A2UI;
import static org.lwjgl.opengl.GL41C.GL_RGB565;

/**
 * Represents a framebuffer attachment that can be turned into a real framebuffer.
 *
 * @param type   The type of attachment this is
 * @param format The internal format of the data
 * @param depth  Whether this is a color or depth attachment
 * @param filter The texture filtering to apply. Only applies to texture buffers
 * @param levels The number of mipmaps for textures and samples for render buffers
 * @param name   The custom name to use when uploading this as a sampler to shaders
 * @author Ocelot
 * @see AdvancedFbo
 * @see FramebufferManager
 * @see PostProcessingManager
 */
public record FramebufferAttachmentDefinition(FramebufferAttachmentDefinition.Type type,
                                              FramebufferAttachmentDefinition.Format format,
                                              boolean depth,
                                              TextureFilter filter,
                                              int levels,
                                              @Nullable String name) {

    public static final Codec<FramebufferAttachmentDefinition> COLOR_CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Type.CODEC.optionalFieldOf("type", Type.TEXTURE)
                            .forGetter(FramebufferAttachmentDefinition::type),
                    Format.CODEC.optionalFieldOf("format", Format.RGBA8)
                            .forGetter(FramebufferAttachmentDefinition::format),
                    TextureFilter.CLAMP_DEFAULT_CODEC.optionalFieldOf("filter", TextureFilter.CLAMP)
                            .forGetter(FramebufferAttachmentDefinition::filter),
                    Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("levels", 1)
                            .forGetter(FramebufferAttachmentDefinition::levels),
                    Codec.STRING.optionalFieldOf("name")
                            .forGetter(attachment -> Optional.ofNullable(attachment.name()))
            ).apply(instance, (type, format, filter, levels, name) ->
                    new FramebufferAttachmentDefinition(type, format, false, filter, levels, name.orElse(null))));
    public static final Codec<FramebufferAttachmentDefinition> DEPTH_CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Type.CODEC.optionalFieldOf("type", Type.TEXTURE)
                            .forGetter(FramebufferAttachmentDefinition::type),
                    Format.CODEC.optionalFieldOf("format", Format.DEPTH_COMPONENT)
                            .forGetter(FramebufferAttachmentDefinition::format),
                    TextureFilter.CLAMP_DEFAULT_CODEC.optionalFieldOf("filter", TextureFilter.CLAMP)
                            .forGetter(FramebufferAttachmentDefinition::filter),
                    Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("levels", 1)
                            .forGetter(FramebufferAttachmentDefinition::levels),
                    Codec.STRING.optionalFieldOf("name")
                            .forGetter(attachment -> Optional.ofNullable(attachment.name()))
            ).apply(instance, (type, format, filter, levels, name) ->
                    new FramebufferAttachmentDefinition(type, format, true, filter, levels, name.orElse(null))));

    /**
     * @return Whether this attachment can be represented as <code>"depth": true</code> in the JSON
     */
    public boolean isCompactDepthAttachment() {
        return this.type == Type.TEXTURE && this.format == Format.DEPTH_COMPONENT && this.filter.equals(TextureFilter.CLAMP) && this.levels == 1 && this.name == null;
    }

    /**
     * The type of attachments.
     */
    public enum Type {

        TEXTURE("Texture"),
        RENDER_BUFFER("Render Buffer");

        public static final Type[] VALUES = Type.values();
        public static final Codec<Type> CODEC = Codec.STRING.flatXmap(name -> {
            for (Type type : VALUES) {
                if (type.name().equalsIgnoreCase(name)) {
                    return DataResult.success(type);
                }
            }
            return DataResult.error(() -> "Unknown attachment type: " + name);
        }, type -> DataResult.success(type.name()));

        private final String displayName;

        Type(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return this.displayName;
        }
    }

    /**
     * The formats for attachments.
     */
    public enum Format {

        RED(GL_RED, GL_RED),
        RG(GL_RG, GL_RG),
        RGB(GL_RGB, GL_RGB),
        BGR(GL_BGR, GL_BGR),
        RGBA(GL_RGBA, GL_RGBA),
        BGRA(GL_BGRA, GL_BGRA),
        DEPTH_COMPONENT(GL_DEPTH_COMPONENT, GL_DEPTH_COMPONENT),
        DEPTH_STENCIL(GL_DEPTH_STENCIL, GL_DEPTH_STENCIL),
        R8(GL_RED, GL_R8),
        R8_SNORM(GL_RED, GL_R8_SNORM),
        R16(GL_RED, GL_R16),
        R16_SNORM(GL_RED, GL_R16_SNORM),
        RG8(GL_RG, GL_RG8),
        RG8_SNORM(GL_RG, GL_RG8_SNORM),
        RG16(GL_RG, GL_RG16),
        RG16_SNORM(GL_RG, GL_RG16_SNORM),
        R3_G3_B2(GL_RGB, GL_R3_G3_B2),
        RGB4(GL_RGB, GL_RGB4),
        RGB5(GL_RGB, GL_RGB5),
        RGB565(GL_RGB, GL_RGB565),
        RGB8(GL_RGB, GL_RGB8),
        RGB8_SNORM(GL_RGB, GL_RGB8_SNORM),
        RGB10(GL_RGB, GL_RGB10),
        RGB12(GL_RGB, GL_RGB12),
        RGB16(GL_RGB, GL_RGB16),
        RGB16_SNORM(GL_RGB, GL_RGB16_SNORM),
        RGBA2(GL_RGBA, GL_RGBA2),
        RGBA4(GL_RGBA, GL_RGBA4),
        RGB5_A1(GL_RGBA, GL_RGB5_A1),
        RGBA8(GL_RGBA, GL_RGBA8),
        RGBA8_SNORM(GL_RGBA, GL_RGBA8_SNORM),
        RGB10_A2(GL_RGBA, GL_RGB10_A2),
        RGB10_A2UI(GL_RGBA_INTEGER, GL_RGB10_A2UI),
        RGBA12(GL_RGBA, GL_RGBA12),
        RGBA16(GL_RGBA, GL_RGBA16),
        RGBA16_SNORM(GL_RGBA, GL_RGBA16_SNORM),
        SRGB(GL_RGB, GL_SRGB),
        SRGB8(GL_RGB, GL_SRGB8),
        SRGB_ALPHA(GL_RGBA, GL_SRGB_ALPHA),
        SRGB8_ALPHA8(GL_RGBA, GL_SRGB8_ALPHA8),
        COMPRESSED_SRGB(GL_RGB, GL_COMPRESSED_SRGB),
        COMPRESSED_SRGB_ALPHA(GL_RGBA, GL_COMPRESSED_SRGB_ALPHA),
        R16F(GL_RED, GL_R16F),
        RG16F(GL_RG, GL_RG16F),
        RGB16F(GL_RGB, GL_RGB16F),
        RGBA16F(GL_RGBA, GL_RGBA16F),
        R32F(GL_RED, GL_R32F),
        RG32F(GL_RG, GL_R32F),
        RGB32F(GL_RGB, GL_RGB32F),
        RGB9_E5(GL_RGB, GL_RGB9_E5),
        RGBA32F(GL_RGBA, GL_RGBA32F),
        R11F_G11F_B10F(GL_RGBA, GL_R11F_G11F_B10F),
        R8I(GL_RED_INTEGER, GL_R8I),
        R8UI(GL_RED_INTEGER, GL_R8UI),
        R16I(GL_RED_INTEGER, GL_R16I),
        R16UI(GL_RED_INTEGER, GL_R16UI),
        R32I(GL_RED_INTEGER, GL_R32I),
        R32UI(GL_RED_INTEGER, GL_R32UI),
        RG8I(GL_RG_INTEGER, GL_RG8I),
        RG8UI(GL_RG_INTEGER, GL_RG8UI),
        RG16I(GL_RG_INTEGER, GL_RG16I),
        RG16UI(GL_RG_INTEGER, GL_RG16UI),
        RG32I(GL_RG_INTEGER, GL_RG32I),
        RG32UI(GL_RG_INTEGER, GL_RG32UI),
        RGB8I(GL_RGB_INTEGER, GL_RGB8I),
        RGB8UI(GL_RGB_INTEGER, GL_RGB8UI),
        RGB16I(GL_RGB_INTEGER, GL_RGB16I),
        RGB16UI(GL_RGB_INTEGER, GL_RGB16UI),
        RGB32I(GL_RGB_INTEGER, GL_RGB32I),
        RGB32UI(GL_RGB_INTEGER, GL_RGB32UI),
        RGBA8I(GL_RGBA_INTEGER, GL_RGBA8I),
        RGBA8UI(GL_RGBA_INTEGER, GL_RGBA8UI),
        RGBA16I(GL_RGBA_INTEGER, GL_RGBA16I),
        RGBA16UI(GL_RGBA_INTEGER, GL_RGBA16UI),
        RGBA32I(GL_RGBA_INTEGER, GL_RGBA32I),
        RGBA32UI(GL_RGBA_INTEGER, GL_RGBA32UI),
        DEPTH_COMPONENT16(GL_DEPTH_COMPONENT, GL_DEPTH_COMPONENT16),
        DEPTH_COMPONENT24(GL_DEPTH_COMPONENT, GL_DEPTH_COMPONENT24),
        DEPTH_COMPONENT32(GL_DEPTH_COMPONENT, GL_DEPTH_COMPONENT32),
        DEPTH_COMPONENT32F(GL_DEPTH_COMPONENT, GL_DEPTH_COMPONENT32F),
        DEPTH24_STENCIL8(GL_DEPTH_STENCIL, GL_DEPTH24_STENCIL8),
        DEPTH32F_STENCIL8(GL_DEPTH_STENCIL, GL_DEPTH32F_STENCIL8);

        public static final Format[] VALUES = Format.values();

        /**
         * <p>Resolves the concrete depth format used by Minecraft's main render target.</p>
         *
         * <p>Unsized formats such as {@link #DEPTH_COMPONENT} leave the choice of a concrete format to the driver, and
         * drivers do not all choose the same one. Minecraft asks for {@code GL_DEPTH_COMPONENT} with a
         * {@code GL_FLOAT} pixel type while Veil asks with {@code GL_UNSIGNED_BYTE}, and some drivers take that as a
         * hint and hand back different formats. {@code glBlitFramebuffer} requires depth formats to match exactly, so
         * where a framebuffer is going to exchange depth with the main render target it has to use whatever format
         * that target actually ended up with, rather than assume.</p>
         *
         * @param fallback The format to use when the main render target cannot be inspected
         * @return The depth format of the main render target
         */
        public static Format getMainDepthFormat(Format fallback) {
            Minecraft client = Minecraft.getInstance();
            RenderTarget mainRenderTarget = client != null ? client.getMainRenderTarget() : null;
            if (mainRenderTarget == null) {
                return fallback;
            }

            int depthTexture = mainRenderTarget.getDepthTextureId();
            if (depthTexture <= 0) {
                return fallback;
            }

            int previousTexture = glGetInteger(GL_TEXTURE_BINDING_2D);
            glBindTexture(GL_TEXTURE_2D, depthTexture);
            int internalFormat = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_INTERNAL_FORMAT);
            glBindTexture(GL_TEXTURE_2D, previousTexture);

            for (Format format : VALUES) {
                if (format.internalId == internalFormat && (format.id == GL_DEPTH_COMPONENT || format.id == GL_DEPTH_STENCIL)) {
                    return format;
                }
            }
            return fallback;
        }

        public static final Codec<Format> CODEC = Codec.STRING.flatXmap(name -> {
            for (Format type : VALUES) {
                if (type.name().equalsIgnoreCase(name)) {
                    return DataResult.success(type);
                }
            }
            return DataResult.error(() -> "Unknown attachment format: " + name);
        }, type -> DataResult.success(type.name()));

        private final int id;
        private final int internalId;

        Format(int id, int internalId) {
            this.id = id;
            this.internalId = internalId;
        }

        /**
         * @return The OpenGL id of this format
         */
        public int getFormat() {
            return this.id;
        }

        /**
         * @return The OpenGL id of this internal format
         */
        public int getInternalFormat() {
            return this.internalId;
        }
    }
}
