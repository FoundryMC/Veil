package foundry.veil.api.client.render.deferred.light;

import foundry.veil.impl.client.render.deferred.light.AreaLightRenderer;
import foundry.veil.impl.client.render.deferred.light.DirectionalLightRenderer;
import foundry.veil.impl.client.render.deferred.light.PointLightRenderer;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Supplier;

/**
 * A source of luminance in a scene. Drawn using {@link LightRenderer}.
 *
 * @author Ocelot
 */
public abstract class Light implements Cloneable {

    protected final Vector3f color;
    protected float brightness;
    private boolean dirty;

    public Light() {
        this.color = new Vector3f(1.0F);
        this.brightness = 1.0F;
        this.markDirty();
    }

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
     * Sets the brightness of the light. This acts as a multiplier on the light's color.
     *
     * @param brightness The new brightness of the light.
     */
    public Light setBrightness(float brightness) {
        this.brightness = brightness;
        this.markDirty();
        return this;
    }

    /**
     * @return The brightness multiplier of the light.
     */
    public float getBrightness() {
        return this.brightness;
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

    // FIXME Use a proper registry

    /**
     * Types of lights that can exist.
     *
     * @author Ocelot
     */
    public enum Type {
        DIRECTIONAL(DirectionalLightRenderer::new),
        POINT(PointLightRenderer::new),
        AREA(AreaLightRenderer::new);

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
