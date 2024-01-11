package foundry.veil.render.deferred.light;

import foundry.veil.render.deferred.LightRenderer;
import foundry.veil.render.wrapper.CullFrustum;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.nio.ByteBuffer;
import java.util.function.Supplier;

/**
 * A source of luminance in a scene. Drawn using {@link LightRenderer}.
 *
 * @author Ocelot
 */
public abstract class Light implements Cloneable {

    protected final Vector3f color;
    private boolean dirty;

    public Light() {
        this.color = new Vector3f(1.0F);
        this.markDirty();
    }

    /**
     * Stores the data of this light into the specified buffer.
     *
     * @param buffer The buffer to fill
     */
    public void store(ByteBuffer buffer) {
        this.color.get(buffer.position(), buffer);
    }

    /**
     * Checks whether this light can be seen in the specified frustum.
     *
     * @param frustum The frustum to check visibility with
     * @return Whether that light is visible
     */
    public abstract boolean isVisible(CullFrustum frustum);

    /**
     * Marks the data in this light as dirty and needing re-uploading.
     */
    public void markDirty() {
        this.dirty = true;
    }

    /**
     * Removes the dirty flag after this light has been uploaded.
     */
    public void clean() {
        this.dirty = false;
    }

    /**
     * @return The color of this light
     */
    public Vector3fc getColor() {
        return this.color;
    }

    /**
     * @return An integer representing the RGB of this light
     */
    public int getColorInt() {
        int red = (int) (this.color.x() / 255.0F) & 0xFF;
        int green = (int) (this.color.y() / 255.0F) & 0xFF;
        int blue = (int) (this.color.z() / 255.0F) & 0xFF;
        return red << 16 | green << 8 | blue;
    }

    /**
     * Sets the RGB color of this light.
     *
     * @param color The new color values
     */
    public Light setColor(Vector3fc color) {
        return this.setColor(color.x(), color.y(), color.z());
    }

    /**
     * Sets the RGB color of this light.
     *
     * @param red   The new red
     * @param green The new green
     * @param blue  The new blue
     */
    public Light setColor(float red, float green, float blue) {
        this.color.set(red, green, blue);
        this.markDirty();
        return this;
    }

    /**
     * Sets the RGB color of this light.
     *
     * @param color THe new RGB of this light
     */
    public Light setColor(int color) {
        this.color.set(((color >> 16) & 0xFF) / 255.0F, ((color >> 8) & 0xFF) / 255.0F, (color & 0xFF) / 255.0F);
        this.markDirty();
        return this;
    }

    /**
     * @return If this light needs to be re-uploaded to the renderer
     */
    public boolean isDirty() {
        return this.dirty;
    }

    /**
     * @return The type of light this is
     */
    public abstract Type getType();

    @Override
    public abstract Light clone();

    /**
     * Types of lights that can exist.
     *
     * @author Ocelot
     */
    public enum Type {
        DIRECTIONAL(DirectionalLightRenderer::new),
        POINT(PointLightRenderer::new);

        private final Supplier<LightTypeRenderer<?>> rendererFactory;

        Type(Supplier<LightTypeRenderer<?>> rendererFactory) {
            this.rendererFactory = rendererFactory;
        }

        /**
         * @return A new light renderer for this type of light
         */
        public LightTypeRenderer<?> createRenderer() {
            return this.rendererFactory.get();
        }
    }
}
