package foundry.veil.api.client.render.deferred.light;

import foundry.veil.api.client.render.CullFrustum;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Represents a light where all rays come from the same direction everywhere. (The sun)
 */
public class DirectionalLight extends Light {

    protected final Vector3f direction;

    public DirectionalLight() {
        this.direction = new Vector3f(0.0F, -1.0F, 0.0F);
    }

    @Override
    public boolean isVisible(CullFrustum frustum) {
        return true;
    }

    /**
     * @return The direction this light is facing
     */
    public Vector3fc getDirection() {
        return this.direction;
    }

    @Override
    public DirectionalLight setColor(float red, float green, float blue) {
        return (DirectionalLight) super.setColor(red, green, blue);
    }

    @Override
    public DirectionalLight setColor(Vector3fc color) {
        return (DirectionalLight) super.setColor(color);
    }

    @Override
    public DirectionalLight setColor(int color) {
        return (DirectionalLight) super.setColor(color);
    }

    /**
     * Sets the direction of this light.
     *
     * @param direction The new direction
     */
    public DirectionalLight setDirection(Vector3fc direction) {
        return this.setDirection(direction.x(), direction.y(), direction.z());
    }

    /**
     * Sets the direction of this light.
     *
     * @param x The new x direction
     * @param y The new y direction
     * @param z The new z direction
     */
    public DirectionalLight setDirection(float x, float y, float z) {
        this.direction.set(x, y, z).normalize();
        this.markDirty();
        return this;
    }

    @Override
    public Type getType() {
        return Type.DIRECTIONAL;
    }

    @Override
    public DirectionalLight clone() {
        return new DirectionalLight()
                .setColor(this.color)
                .setDirection(this.direction);
    }
}
