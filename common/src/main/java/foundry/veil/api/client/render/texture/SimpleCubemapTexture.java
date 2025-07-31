package foundry.veil.api.client.render.texture;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import foundry.veil.mixin.pipeline.accessor.PipelineNativeImageAccessor;
import net.minecraft.Util;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.lwjgl.opengl.GL12C.*;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL14C.GL_TEXTURE_LOD_BIAS;

/**
 * A cubemap texture type that loads from 2D textures.
 *
 * @since 3.1.0
 */
public class SimpleCubemapTexture extends CubemapTexture implements VeilPreloadedTexture {

    private static final Logger LOGGER = LogUtils.getLogger();

    protected final ResourceLocation location;
    private CompletableFuture<TextureImage> imageFuture;

    public SimpleCubemapTexture(ResourceLocation location) {
        this.location = location;
        this.imageFuture = null;
    }

    @Override
    public CompletableFuture<?> preload(ResourceManager resourceManager, Executor backgroundExecutor) {
        if (this.imageFuture == null || this.imageFuture.isDone()) {
            this.imageFuture = CompletableFuture.supplyAsync(() -> TextureImage.load(resourceManager, this.location), backgroundExecutor);
        }
        return this.imageFuture;
    }

    @Override
    public void load(@NotNull ResourceManager resourceManager) throws IOException {
        TextureImage textureImages = this.getTextureImage(resourceManager);
        try (NativeImage image = textureImages.getImage()) {
            int width = image.getWidth();
            int height = image.getHeight();
            if (height != width * 3 / 4) {
                throw new IOException("Expected cubemap image to be " + width + "x" + (width * 3 / 4) + ". Was " + width + "x" + height);
            }

            TextureMetadataSection texturemetadatasection = textureImages.getTextureMetadata();
            boolean blur;
            boolean clamp;
            if (texturemetadatasection != null) {
                blur = texturemetadatasection.isBlur();
                clamp = texturemetadatasection.isClamp();
            } else {
                blur = false;
                clamp = false;
            }

            this.setFilter(blur, clamp);
            if (!RenderSystem.isOnRenderThreadOrInit()) {
                RenderSystem.recordRenderCall(() -> this.loadImages(image));
            } else {
                this.loadImages(image);
            }
        }
    }

    private void loadImages(NativeImage image) {
        try (image) {
            this.bind();
            GlStateManager._texParameter(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAX_LEVEL, 0);
            GlStateManager._texParameter(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_LOD, 0);
            GlStateManager._texParameter(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAX_LOD, 0);
            GlStateManager._texParameter(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_LOD_BIAS, 0.0F);

            int tileSize = image.getWidth() / 4;
            PipelineNativeImageAccessor accessor = (PipelineNativeImageAccessor) (Object) image;
            accessor.invokeCheckAllocated();
            NativeImage.Format format = image.format();
            format.setUnpackPixelStoreState();
            long pixels = accessor.getPixels();

            GlStateManager._pixelStore(GL_UNPACK_ROW_LENGTH, image.getWidth());

            // Top
            GlStateManager._pixelStore(GL_UNPACK_SKIP_PIXELS, tileSize);
            GlStateManager._pixelStore(GL_UNPACK_SKIP_ROWS, 0);
            glTexImage2D(getGlFace(Direction.UP), 0, GL_RGBA8, tileSize, tileSize, 0, format.glFormat(), GL_UNSIGNED_BYTE, pixels);

            // Left
            GlStateManager._pixelStore(GL_UNPACK_SKIP_PIXELS, 0);
            GlStateManager._pixelStore(GL_UNPACK_SKIP_ROWS, tileSize);
            glTexImage2D(getGlFace(Direction.WEST), 0, GL_RGBA8, tileSize, tileSize, 0, format.glFormat(), GL_UNSIGNED_BYTE, pixels);

            // Front
            GlStateManager._pixelStore(GL_UNPACK_SKIP_PIXELS, tileSize);
            GlStateManager._pixelStore(GL_UNPACK_SKIP_ROWS, tileSize);
            glTexImage2D(getGlFace(Direction.SOUTH), 0, GL_RGBA8, tileSize, tileSize, 0, format.glFormat(), GL_UNSIGNED_BYTE, pixels);

            // Right
            GlStateManager._pixelStore(GL_UNPACK_SKIP_PIXELS, tileSize * 2);
            GlStateManager._pixelStore(GL_UNPACK_SKIP_ROWS, tileSize);
            glTexImage2D(getGlFace(Direction.EAST), 0, GL_RGBA8, tileSize, tileSize, 0, format.glFormat(), GL_UNSIGNED_BYTE, pixels);

            // Back
            GlStateManager._pixelStore(GL_UNPACK_SKIP_PIXELS, tileSize * 3);
            GlStateManager._pixelStore(GL_UNPACK_SKIP_ROWS, tileSize);
            glTexImage2D(getGlFace(Direction.NORTH), 0, GL_RGBA8, tileSize, tileSize, 0, format.glFormat(), GL_UNSIGNED_BYTE, pixels);

            // Down
            GlStateManager._pixelStore(GL_UNPACK_SKIP_PIXELS, tileSize);
            GlStateManager._pixelStore(GL_UNPACK_SKIP_ROWS, tileSize * 2);
            glTexImage2D(getGlFace(Direction.DOWN), 0, GL_RGBA8, tileSize, tileSize, 0, format.glFormat(), GL_UNSIGNED_BYTE, pixels);
        }
    }

    @Override
    public void reset(@NotNull TextureManager textureManager, @NotNull ResourceManager resourceManager, @NotNull ResourceLocation location, @NotNull Executor gameExecutor) {
        this.preload(resourceManager, Util.backgroundExecutor()).thenRunAsync(() -> textureManager.register(location, this), gameExecutor);
    }

    protected TextureImage getTextureImage(ResourceManager resourceManager) {
        if (this.imageFuture != null) {
            TextureImage image = this.imageFuture.join();
            this.imageFuture = null;
            return image;
        } else {
            return TextureImage.load(resourceManager, this.location);
        }
    }

    protected static class TextureImage implements Closeable {
        @Nullable
        private final TextureMetadataSection metadata;
        @Nullable
        private final NativeImage image;
        @Nullable
        private final IOException exception;

        public TextureImage(IOException exception) {
            this.exception = exception;
            this.metadata = null;
            this.image = null;
        }

        public TextureImage(@Nullable TextureMetadataSection metadata, NativeImage image) {
            this.exception = null;
            this.metadata = metadata;
            this.image = image;
        }

        public static TextureImage load(ResourceManager resourceManager, ResourceLocation location) {
            try {
                Resource resource = resourceManager.getResourceOrThrow(location);

                NativeImage nativeimage;
                try (InputStream inputstream = resource.open()) {
                    nativeimage = NativeImage.read(inputstream);
                }

                TextureMetadataSection texturemetadatasection = null;

                try {
                    texturemetadatasection = resource.metadata().getSection(TextureMetadataSection.SERIALIZER).orElse(null);
                } catch (RuntimeException runtimeexception) {
                    LOGGER.warn("Failed reading metadata of: {}", location, runtimeexception);
                }

                return new TextureImage(texturemetadatasection, nativeimage);
            } catch (IOException ioexception) {
                return new TextureImage(ioexception);
            }
        }

        @Nullable
        public TextureMetadataSection getTextureMetadata() {
            return this.metadata;
        }

        public NativeImage getImage() throws IOException {
            if (this.exception != null) {
                throw this.exception;
            } else {
                return this.image;
            }
        }

        @Override
        public void close() {
            if (this.image != null) {
                this.image.close();
            }
        }

        public void throwIfError() throws IOException {
            if (this.exception != null) {
                throw this.exception;
            }
        }
    }
}
