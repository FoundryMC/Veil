package foundry.veil.api.client.render.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.NativeResource;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.lwjgl.opengl.GL11C.GL_RGBA;

public class SimpleArrayTexture extends ArrayTexture implements VeilPreloadedTexture {

    private static final Logger LOGGER = LogUtils.getLogger();

    protected final ResourceLocation[] locations;

    private CompletableFuture<TextureImage[]> imagesFuture;

    public SimpleArrayTexture(ResourceLocation... locations) {
        if (locations.length == 0) {
            throw new IllegalStateException("SimpleArrayTexture requires at least one location");
        }
        this.locations = locations;
        this.imagesFuture = null;
    }

    @Override
    public CompletableFuture<?> preload(ResourceManager resourceManager, Executor backgroundExecutor) {
        if (this.imagesFuture == null || this.imagesFuture.isDone()) {
            this.imagesFuture = CompletableFuture.supplyAsync(() -> TextureImage.load(resourceManager, this.locations), backgroundExecutor);
        }
        return this.imagesFuture;
    }

    @Override
    public void load(@NotNull ResourceManager resourceManager) throws IOException {
        TextureImage[] textureImages = this.getTextureImages(resourceManager);
        NativeImage[] images = new NativeImage[textureImages.length];
        try {
            for (int i = 0; i < textureImages.length; i++) {
                images[i] = textureImages[i].getImage();
            }
        } catch (IOException e) {
            for (TextureImage textureImage : textureImages) {
                textureImage.free();
            }
            throw e;
        }

        TextureMetadataSection texturemetadatasection = textureImages[0].getTextureMetadata();
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
            RenderSystem.recordRenderCall(() -> this.loadImages(images));
        } else {
            this.loadImages(images);
        }
    }

    private void loadImages(NativeImage[] images) {
        try {
            this.init(GL_RGBA, 0, images[0].getWidth(), images[0].getHeight(), images.length);
            this.upload(images);
        } finally {
            for (NativeImage image : images) {
                image.close();
            }
        }
    }

    @Override
    public void reset(@NotNull TextureManager textureManager, @NotNull ResourceManager resourceManager, @NotNull ResourceLocation location, @NotNull Executor gameExecutor) {
        this.preload(resourceManager, Util.backgroundExecutor()).thenRunAsync(() -> textureManager.register(location, this), gameExecutor);
    }

    protected TextureImage[] getTextureImages(ResourceManager resourceManager) {
        if (this.imagesFuture != null) {
            TextureImage[] images = this.imagesFuture.join();
            this.imagesFuture = null;
            return images;
        } else {
            return TextureImage.load(resourceManager, this.locations);
        }
    }

    public static class TextureImage implements NativeResource {

        private final NativeImage image;
        private final TextureMetadataSection metadata;
        private final IOException exception;

        public TextureImage(IOException exception) {
            this.image = null;
            this.metadata = null;
            this.exception = Objects.requireNonNull(exception, "exception");
        }

        public TextureImage(NativeImage image, @Nullable TextureMetadataSection metadata) {
            this.image = Objects.requireNonNull(image, "image");
            this.metadata = metadata;
            this.exception = null;
        }

        public static TextureImage[] load(ResourceManager resourceManager, ResourceLocation... locations) {
            TextureImage[] images = new TextureImage[locations.length];
            if (locations.length == 0) {
                return images;
            }

            int width = 0;
            int height = 0;
            for (int i = 0; i < locations.length; i++) {
                NativeImage image = null;
                try {
                    ResourceLocation location = locations[i];
                    Resource resource = resourceManager.getResourceOrThrow(location);

                    try (InputStream inputstream = resource.open()) {
                        image = NativeImage.read(inputstream);
                    }
                    if (width == 0 || height == 0) {
                        width = image.getWidth();
                        height = image.getHeight();
                    } else if (image.getWidth() != width || image.getHeight() != height) {
                        throw new IOException("Layer " + i + " dimensions don't match");
                    }

                    TextureMetadataSection metadata = null;
                    try {
                        metadata = resource.metadata().getSection(TextureMetadataSection.SERIALIZER).orElse(null);
                    } catch (Exception e) {
                        LOGGER.warn("Failed reading metadata of: {}", location, e);
                    }

                    images[i] = new TextureImage(image, metadata);
                } catch (IOException e) {
                    if (image != null) {
                        image.close();
                    }
                    images[i] = new TextureImage(e);
                }
            }

            return images;
        }

        public NativeImage getImage() throws IOException {
            if (this.exception != null) {
                throw this.exception;
            } else {
                return this.image;
            }
        }

        public @Nullable TextureMetadataSection getTextureMetadata() {
            return this.metadata;
        }

        public @Nullable IOException getException() {
            return this.exception;
        }

        @Override
        public void free() {
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
