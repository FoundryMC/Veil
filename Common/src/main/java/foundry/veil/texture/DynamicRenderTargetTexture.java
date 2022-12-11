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

    protected final Consumer<DynamicRenderTargetTexture> updateCallback;
    private boolean initialized = false;

    private RenderTarget renderTarget;

    private final int width, height;
    private final ResourceLocation resourceLocation;

    @Nullable
    private NativeImage cpuImage;

    public DynamicRenderTargetTexture(ResourceLocation location, int width, int height, @Nullable Consumer<DynamicRenderTargetTexture> updateCallback) {
        this.resourceLocation = location;
        this.width = width;
        this.height = height;
        this.updateCallback = updateCallback;
    }

    public DynamicRenderTargetTexture(ResourceLocation location, int size,  @Nullable Consumer<DynamicRenderTargetTexture> updateCallback) {
        this(location, size, size, updateCallback);
    }

    public void initialize(){
        this.initialized = true;
        Minecraft.getInstance().getTextureManager().register(resourceLocation, this);
        redraw();
    }

    public void redraw(){
        if(!RenderSystem.isOnRenderThreadOrInit()){
            RenderSystem.recordRenderCall(() -> {
                bind();
                if(updateCallback != null){
                    updateCallback.accept(this);
                }
            });
        } else {
            bind();
            if(updateCallback != null){
                updateCallback.accept(this);
            }
        }
    }

    public boolean isInitialized(){
        return initialized;
    }

    @Override
    public void load(ResourceManager resourceManager) throws IOException {
    }

    public RenderTarget getRenderTarget(){
        if(renderTarget == null){
            renderTarget = new MainTarget(width, height);
            this.id = this.renderTarget.getColorTextureId();
        }
        return renderTarget;
    }
    public void bindWrite(){
        getRenderTarget().bindWrite(true);
    }

    public int getWidth(){
        return width;
    }

    public int getHeight(){
        return height;
    }

    public ResourceLocation getResourceLocation(){
        return resourceLocation;
    }

    @Override
    public int getId() {
        return this.renderTarget.getColorTextureId();
    }

    @Override
    public void releaseId() {
        if(!RenderSystem.isOnRenderThread()){
            RenderSystem.recordRenderCall(this::clearGlId0);
        } else {
            this.clearGlId0();
        }
    }

    private void clearGlId0(){
        if(this.renderTarget != null){
            this.renderTarget.destroyBuffers();
            this.renderTarget = null;
        }
        this.id = -1;
    }

    @Override
    public void close() {
        this.releaseId();
        if(this.cpuImage != null){
            this.cpuImage.close();
            this.cpuImage = null;
        }
    }

    public NativeImage getPixels(){
        if(this.cpuImage == null){
            this.cpuImage = new NativeImage(width, height, false);
        }
        return this.cpuImage;
    }

    public void download(){
        this.bind();
        getPixels().downloadTexture(0, false);
    }

    public void upload(){
        if(this.cpuImage != null){
            this.bind();
            this.cpuImage.upload(0,0,0,false);
            this.cpuImage.close();
            this.cpuImage = null;
        } else {
            Veil.LOGGER.warn("Tried to upload pixels to texture {} but there were no pixels to upload!", resourceLocation);
        }
    }

    public List<Path> saveTextureToFile(Path texturePath, String name) throws IOException {
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
        Path output = texturePath.resolve(name + ".png");
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
