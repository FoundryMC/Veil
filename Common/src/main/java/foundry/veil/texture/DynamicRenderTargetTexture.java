package foundry.veil.texture;

import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.Veil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DynamicRenderTargetTexture extends AbstractTexture {

    //runs when texture is initialized and populates it. Runs each tick if its tickable
    protected final Consumer<DynamicRenderTargetTexture> drawingFunction;
    private boolean initialized = false;

    //thing where it renders stuff on
    private RenderTarget frameBuffer;

    private final int width;
    private final int height;
    private final ResourceLocation resourceLocation;

    //cpu side of the texture. used to dynamically edit it
    @Nullable
    private NativeImage cpuImage;

    public DynamicRenderTargetTexture(ResourceLocation resourceLocation, int width, int height,
                                           @Nullable Consumer<DynamicRenderTargetTexture> textureDrawingFunction) {
        this.width = width;
        this.height = height;
        this.resourceLocation = resourceLocation;
        this.drawingFunction = textureDrawingFunction;
    }

    public DynamicRenderTargetTexture(ResourceLocation resourceLocation, int size,
                                           @Nullable Consumer<DynamicRenderTargetTexture> textureDrawingFunction) {
        this(resourceLocation, size, size, textureDrawingFunction);
    }

    public void initialize() {
        this.initialized = true;
        //this.bind(); // assign gpu texture id
        //register this texture. Call at the right time or stuff will get messed up
        Minecraft.getInstance().getTextureManager().register(resourceLocation, this);
        redraw();
    }

    /**
     * Force redraw using provided render function. You can also redraw manually
     */
    public void redraw() {
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(() -> {
                bind();
                if (drawingFunction != null) {
                    drawingFunction.accept(this);
                }
            });
        } else {
            bind();
            if (drawingFunction != null) {
                drawingFunction.accept(this);
            }
        }
    }

    /**
     * Call to register this texture
     */
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void load(ResourceManager manager) {
    }

    public RenderTarget getFrameBuffer() {
        //initAfterSetup the frame buffer (do not touch, magic code)
        if (this.frameBuffer == null) {
            this.frameBuffer = new MainTarget(width, height);
            this.id = this.frameBuffer.getColorTextureId(); // just in case
        }
        return this.frameBuffer;
    }

    //sets current frame buffer to this. Further, render calls will draw here
    public void bindWrite() {
        getFrameBuffer().bindWrite(true);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public ResourceLocation getTextureLocation() {
        return resourceLocation;
    }

    @Override
    public int getId() {
        return this.getFrameBuffer().getColorTextureId();
    }

    @Override
    public void releaseId() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(this::clearGlId0);
        } else {
            this.clearGlId0();
        }
    }

    private void clearGlId0() {
        if (this.frameBuffer != null) {
            this.frameBuffer.destroyBuffers();
            this.frameBuffer = null;
        }
        this.id = -1;
    }

    @Override
    public void close() {
        //releases texture id
        this.releaseId();
        //closes native image and texture
        if (this.cpuImage != null) {
            this.cpuImage.close();
            this.cpuImage = null;
        }
        //destroy render buffer
        //dont do this, it causes many issues
        // if (this.initialized) Minecraft.getInstance().getTextureManager().release(resourceLocation);
    }

    public NativeImage getPixels() {
        if (this.cpuImage == null) {
            this.cpuImage = new NativeImage(width, height, false);
        }
        return cpuImage;
    }

    /**
     * Downloads the GPU texture to CPU for edit
     */
    public void download() {
        this.bind();
        getPixels().downloadTexture(0, false);
        //cpuImage.flipY();
    }

    /**
     * Uploads the image to GPU and closes its CPU side one
     */
    public void upload() {
        if (this.cpuImage != null) {
            this.bind();
            this.cpuImage.upload(0, 0, 0, false);
            this.cpuImage.close();
            this.cpuImage = null;
        } else {
            Veil.LOGGER.warn("Trying to upload disposed texture {}", this.getId());
        }
    }

    public List<Path> saveTextureToFile(Path texturesDir) throws IOException {
        return saveTextureToFile(texturesDir, this.resourceLocation.getPath().replace("/", "_"));
    }

    public List<Path> saveTextureToFile(Path texturesDir, String name) throws IOException {
        this.bind();

        GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

        List<Path> textureFiles = new ArrayList<>();

        int width = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
        int height = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
        int size = width * height;
        if (size == 0) {
            return List.of();
        }

        BufferedImage bufferedimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Path output = texturesDir.resolve(name + ".png");
        IntBuffer buffer = BufferUtils.createIntBuffer(size);
        int[] data = new int[size];

        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, buffer);
        buffer.get(data);
        bufferedimage.setRGB(0, 0, width, height, data, 0, width);

        ImageIO.write(bufferedimage, "png", output.toFile());
        textureFiles.add(output);

        return textureFiles;
    }
}