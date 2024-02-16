package foundry.veil.api.client.render.deferred.light;

import org.joml.Vector3d;
import org.joml.Vector3fc;

import java.nio.ByteBuffer;

/**
 * Represents a light where all rays come from a position in space.
 *
 * @author Ocelot
 */
public class PointLight extends Light implements IndirectLight<PointLight> {

    protected final Vector3d position;
    protected float radius;

    public PointLight() {
        this.position = new Vector3d();
        this.radius = 1.0F;
    }

    @Override
    public void store(ByteBuffer buffer) {
        this.position.getf(buffer.position(), buffer);
        buffer.position(buffer.position() + Float.BYTES * 3);
        buffer.putFloat(this.color.x() * this.brightness);
        buffer.putFloat(this.color.y() * this.brightness);
        buffer.putFloat(this.color.z() * this.brightness);
        buffer.putFloat(this.radius);
    }

    @Override
    public Vector3d getPosition() {
        return this.position;
    }

    @Override
    public float getRadius() {
        return this.radius;
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

    @Override
    public PointLight setRadius(float radius) {
        this.radius = radius;
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
