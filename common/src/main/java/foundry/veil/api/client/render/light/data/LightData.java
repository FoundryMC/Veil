package foundry.veil.api.client.render.light.data;

import foundry.veil.api.client.color.Color;
import foundry.veil.api.client.color.Colorc;
import foundry.veil.api.client.registry.LightTypeRegistry;
import foundry.veil.api.client.render.CullFrustum;
import foundry.veil.api.client.render.light.renderer.LightRenderer;
import net.minecraft.client.Camera;
import org.joml.Vector3fc;

/**
 * A source of luminance in a scene. Drawn using {@link LightRenderer}.
 *
 * @since 2.0.0
 */
public abstract class LightData {

    protected final Color color;
    protected float brightness;

    public LightData() {
        this.color = new Color(Color.WHITE);
        this.brightness = 1.0F;
    }

    /**
     * @return The color of this light
     */
    public Color getColor() {
        return this.color;
    }

    /**
     * @return The brightness multiplier of the light.
     */
    public float getBrightness() {
        return this.brightness;
    }

    /**
     * Sets the RGB color of this light.
     *
     * @param color The new color values
     */
    public LightData setColor(Vector3fc color) {
        return this.setColor(color.x(), color.y(), color.z());
    }

    /**
     * Sets the RGB color of this light.
     *
     * @param color The new color values
     */
    public LightData setColor(Colorc color) {
        return this.setColor(color.red(), color.green(), color.blue());
    }

    /**
     * Sets the RGB color of this light.
     *
     * @param red   The new red
     * @param green The new green
     * @param blue  The new blue
     */
    public LightData setColor(float red, float green, float blue) {
        this.color.set(red, green, blue);
        return this;
    }

    /**
     * Sets the RGB color of this light.
     *
     * @param color THe new RGB of this light
     */
    public LightData setColor(int color) {
        this.color.setRGB(color);
        return this;
    }

    /**
     * Sets the brightness of the light. This acts as a multiplier on the light's color.
     *
     * @param brightness The new brightness of the light.
     */
    public LightData setBrightness(float brightness) {
        this.brightness = brightness;
        return this;
    }

    /**
     * Checks if this light is visible to the camera.
     *
     * @param frustum The frustum to check against
     * @return Whether this light is visible
     */
    public abstract boolean isVisible(CullFrustum frustum);

    /**
     * Sets the light position/rotation to be the same as the specified camera.
     *
     * @param camera The camera to set relative to
     */
    public LightData setTo(Camera camera) {
        return this;
    }

    /**
     * @return The type of light this is
     */
    public abstract LightTypeRegistry.LightType<?> getType();

}
