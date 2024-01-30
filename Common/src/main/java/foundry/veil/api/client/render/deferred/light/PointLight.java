package foundry.veil.api.client.render.deferred.light;

import foundry.veil.api.client.render.CullFrustum;
import org.joml.Vector3d;
import org.joml.Vector3fc;

import java.nio.ByteBuffer;

/**
 * Represents a light where all rays come from a position in space.
 *
 * @author Ocelot
 */
public class PointLight extends Light implements InstancedLight, PositionedLight<PointLight> {

    protected final Vector3d position;
    protected float radius;
    protected float falloff;

    public PointLight() {
        this.position = new Vector3d();
        this.radius = 1.0F;
        this.falloff = 0.0F;
    }

    @Override
    public void store(ByteBuffer buffer) {
        this.position.getf(buffer.position(), buffer);
        buffer.position(buffer.position() + Float.BYTES * 3);
        buffer.putFloat(this.color.x() * this.brightness);
        buffer.putFloat(this.color.y() * this.brightness);
        buffer.putFloat(this.color.z() * this.brightness);
        buffer.putFloat(this.radius);
        buffer.putFloat(this.falloff);
    }

    @Override
    public boolean isVisible(CullFrustum frustum) {
        double minX = this.position.x() - this.radius;
        double minY = this.position.y() - this.radius;
        double minZ = this.position.z() - this.radius;
        double maxX = this.position.x() + this.radius;
        double maxY = this.position.y() + this.radius;
        double maxZ = this.position.z() + this.radius;
        return frustum.testAab(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public Vector3d getPosition() {
        return this.position;
    }

    /**
     * @return The maximum distance the light can travel
     */
    public float getRadius() {
        return this.radius;
    }

    /**
     * @return The additional linear falloff applied to the light
     */
    public float getFalloff() {
        return this.falloff;
    }

    @Override
    public PointLight setColor(float red, float green, float blue) {
        return (PointLight) super.setColor(red, green, blue);
    }

    @Override
    public PointLight setColor(Vector3fc color) {
        return (PointLight) super.setColor(color);
    }

    @Override
    public PointLight setBrightness(float brightness) {
        return (PointLight) super.setBrightness(brightness);
    }

    @Override
    public PointLight setPosition(double x, double y, double z) {
        this.position.set(x, y, z);
        this.markDirty();
        return this;
    }

    /**
     * Sets the maximum radius the light can influence.
     *
     * @param radius The maximum area of influence for the light
     */
    public PointLight setRadius(float radius) {
        this.radius = radius;
        this.markDirty();
        return this;
    }

    /**
     * Sets the additional linear falloff factor for attenuation.
     *
     * @param falloff The linear falloff factor
     */
    public PointLight setFalloff(float falloff) {
        this.falloff = falloff;
        this.markDirty();
        return this;
    }

    @Override
    public Type getType() {
        return Type.POINT;
    }

    @Override
    public PointLight clone() {
        return new PointLight()
                .setPosition(this.position)
                .setColor(this.color)
                .setRadius(this.radius)
                .setBrightness(this.brightness);
    }
}
