package foundry.veil.api.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * A utility class to manage scissor clipping regions.
 * This allows for restricting rendering to specific rectangular areas.
 *
 * @author amo, Ocelot
 */
public final class ScissorStack {

    private final Deque<ScreenRectangle> regions;

    public ScissorStack() {
        this.regions = new ArrayDeque<>();
    }

    private void apply(ScreenRectangle rectangle) {
        RenderSystem.enableScissor(
                rectangle.left(),
                Minecraft.getInstance().getWindow().getHeight() - rectangle.bottom(),
                rectangle.width(),
                rectangle.height());
    }

    /**
     * Pushes a new scissor clipping region onto the stack.
     * The region is automatically constrained by any existing regions on the stack.
     *
     * @param x      The x-coordinate of the top-left corner
     * @param y      The y-coordinate of the top-left corner
     * @param width  The width of the region
     * @param height The height of the region
     */
    public void push(int x, int y, int width, int height) {
        this.push((float) x, (float) y, (float) width, (float) height);
    }

    /**
     * Pushes a new scissor clipping region onto the stack.
     * The region is automatically constrained by any existing regions on the stack.
     *
     * @param x      The x-coordinate of the top-left corner
     * @param y      The y-coordinate of the top-left corner
     * @param width  The width of the region
     * @param height The height of the region
     * @since 1.3.0
     */
    public void push(float x, float y, float width, float height) {
        if (!this.regions.isEmpty()) {
            float scale = (float) Minecraft.getInstance().getWindow().getGuiScale();
            float x2 = x + width;
            float y2 = y + height;

            ScreenRectangle parent = this.regions.peek();
            x = Mth.clamp(x * scale, parent.left(), parent.right());
            width = Mth.clamp(x2 * scale, parent.left(), parent.right()) - x;
            y = Mth.clamp(y * scale, parent.top(), parent.bottom());
            height = Mth.clamp(y2 * scale, parent.top(), parent.bottom()) - y;
        }

        ScreenRectangle region = new ScreenRectangle((int) x, (int) y, (int) width, (int) height);
        this.regions.push(region);
        this.apply(region);
    }

    /**
     * Removes the top scissor clipping region from the stack.
     * If there are any regions remaining, the previous region is reapplied.
     */
    public void pop() {
        this.regions.pop();
        if (this.regions.isEmpty()) {
            RenderSystem.disableScissor();
        } else {
            this.apply(this.regions.peek());
        }
    }

    /**
     * @return The top of the scissor stack or <code>null</code> if scissoring is disabled
     * @since 1.3.0
     */
    public @Nullable ScreenRectangle getTop() {
        return this.regions.isEmpty() ? null : this.regions.peek();
    }

    /**
     * Checks if the current scissor region contains the x, y point.
     *
     * @param x The x position to check
     * @param y The y position to check
     * @return Whether that position is contained in the scissor bounds
     * @since 1.3.0
     */
    public boolean containsPoint(float x, float y) {
        if (this.regions.isEmpty()) {
            return true;
        }

        float scale = (float) Minecraft.getInstance().getWindow().getGuiScale();
        return this.regions.peek().containsPoint((int) (x * scale), (int) (y * scale));
    }

    /**
     * @return The number of regions in the stack
     * @since 1.3.0
     */
    public int size() {
        return this.regions.size();
    }

    /**
     * @return Whether all regions have been popped
     * @since 1.3.0
     */
    public boolean isEmpty() {
        return this.regions.isEmpty();
    }

    /**
     * Clears all scissored regions and disables scissoring.
     *
     * @since 1.3.0
     */
    public void clear() {
        this.regions.clear();
        RenderSystem.disableScissor();
    }
}