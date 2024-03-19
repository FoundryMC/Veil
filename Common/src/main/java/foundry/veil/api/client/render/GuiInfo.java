package foundry.veil.api.client.render;

import com.mojang.blaze3d.platform.Window;
import foundry.veil.api.client.render.shader.definition.ShaderBlock;
import net.minecraft.client.Minecraft;
import org.lwjgl.system.NativeResource;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL31C.GL_UNIFORM_BUFFER;

/**
 * Manages the global gui context variables.
 *
 * @author Ocelot
 */
public class GuiInfo implements NativeResource {

    private static final int SIZE = Float.BYTES;

    private final ShaderBlock<GuiInfo> block;
    private float guiScale;

    /**
     * Creates a new set of camera matrices.
     */
    public GuiInfo() {
        this.block = ShaderBlock.withSize(GL_UNIFORM_BUFFER, GuiInfo.SIZE, GuiInfo::write);
        this.guiScale = 0.0F;
    }

    private void write(ByteBuffer buffer) {
        buffer.putFloat(0, this.guiScale);
    }

    /**
     * Updates the camera matrices to match the current render system projection.
     */
    public void update() {
        Window window = Minecraft.getInstance().getWindow();
        this.guiScale = (float) window.getGuiScale();
        this.block.set(this);
        VeilRenderSystem.bind("GuiInfo", this.block);
    }

    /**
     * Unbinds this shader block.
     */
    public void unbind() {
        VeilRenderSystem.unbind(this.block);
    }

    /**
     * @return The far clipping plane of the frustum
     */
    public float getGuiScale() {
        return this.guiScale;
    }

    @Override
    public void free() {
        this.block.free();
    }
}
