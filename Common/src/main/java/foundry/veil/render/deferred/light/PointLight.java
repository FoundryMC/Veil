package foundry.veil.render.deferred.light;

import foundry.veil.render.wrapper.CullFrustum;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.nio.ByteBuffer;

/**
 * Represents a light where all rays come from a position in space.
 *
 * @author Ocelot
 */
public class PointLight extends Light implements InstancedLight, PositionedLight<PointLight> {

    protected final Vector3f position;
    protected float radius;
    protected float falloff;

    public PointLight() {
        this.position = new Vector3f();
        this.radius = 1.0F;
        this.falloff = 0.0F;
    }

    @Override
    public void store(ByteBuffer buffer) {
        this.position.get(buffer.position(), buffer);
        this.color.get(buffer.position() + Float.BYTES * 3, buffer);
        buffer.position(buffer.position() + Float.BYTES * 6);
        buffer.putFloat(this.radius);
        buffer.putFloat(this.falloff);
    }

    @Override
    public boolean isVisible(CullFrustum frustum) {
        float minX = this.position.x() - this.radius;
        float minY = this.position.y() - this.radius;
        float minZ = this.position.z() - this.radius;
        float maxX = this.position.x() + this.radius;
        float maxY = this.position.y() + this.radius;
        float maxZ = this.position.z() + this.radius;
        return frustum.testAab(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /**
     * @return The position of this light
     */
    public Vector3f getPosition() {
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
    public PointLight setPosition(float x, float y, float z) {
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
                .setColor(this.color);
    }
}
