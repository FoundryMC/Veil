package foundry.veil.api.client.render.deferred.light;

import foundry.veil.api.client.render.CullFrustum;
import org.joml.*;

import java.lang.Math;
import java.nio.ByteBuffer;

public class AreaLight extends Light implements InstancedLight, PositionedLight<AreaLight> {
    protected final Vector3d position;
    protected final Quaternionf orientation;
    private final Matrix4d matrix;

    protected final Vector2f size;

    protected float angle;
    protected float distance;
    protected float falloff;

    public AreaLight() {
        this.matrix = new Matrix4d();
        this.position = new Vector3d();
        this.orientation = new Quaternionf();

        this.size = new Vector2f(1.0F, 1.0F);

        this.angle = 0.7854F;
        this.distance = 1.0F;
        this.falloff = 0.0F;
    }

    @Override
    public void store(ByteBuffer buffer) {
        this.matrix.getFloats(buffer.position(), buffer);
        buffer.position(buffer.position() + Float.BYTES * 16);

        buffer.putFloat(this.color.x() * this.brightness);
        buffer.putFloat(this.color.y() * this.brightness);
        buffer.putFloat(this.color.z() * this.brightness);

        this.size.get(buffer.position(), buffer);
        buffer.position(buffer.position() + Float.BYTES * 2);

        buffer.putFloat(this.angle);
        buffer.putFloat(this.distance);
        buffer.putFloat(this.falloff);
    }

    // the bounding box here isn't particularly tight, but it should always encapsulate the light's area.
    // see AreaLightRenderer#createMesh() for like how to fix this i think. too lazy to do it right now.
    @Override
    public boolean isVisible(CullFrustum frustum) {
        float radius = Math.max(this.size.x, this.size.y) + this.distance;
        double minX = this.position.x() - radius;
        double minY = this.position.y() - radius;
        double minZ = this.position.z() - radius;
        double maxX = this.position.x() + radius;
        double maxY = this.position.y() + radius;
        double maxZ = this.position.z() + radius;
        return frustum.testAab(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public Type getType() {
        return Type.AREA;
    }

    @Override
    public Vector3d getPosition() {
        return this.position;
    }

    /**
     * @return The current orientation of the light.
     */
    public Quaternionf getOrientation() {
        return this.orientation;
    }

    /**
     * @return The size of the light's surface
     */
    public Vector2f getSize() {
        return this.size;
    }

    /**
     * @return The maximum angle of the light from the plane's surface.
     */
    public float getAngle() {
        return this.angle;
    }
    
    /**
     * @return The maximum distance the light can travel
     */
    public float getDistance() {
        return this.distance;
    }

    /**
     * @return The additional linear falloff applied to the light
     */
    public float getFalloff() {
        return this.falloff;
    }

    
    @Override
    public AreaLight setColor(float red, float green, float blue) {
        return (AreaLight) super.setColor(red, green, blue);
    }

    @Override
    public AreaLight setColor(Vector3fc color) {
        return (AreaLight) super.setColor(color);
    }

    @Override
    public AreaLight setBrightness(float brightness) {
        return (AreaLight) super.setBrightness(brightness);
    }
    
    @Override
    public AreaLight setPosition(double x, double y, double z) {
        this.position.set(x, y, z);
        this.updateMatrix();
        return this;
    }

    /**
     * Sets the orientation of the light's surface
     *
     * @param orientation The orientation of the light's surface.
     */
    public AreaLight setOrientation(Quaternionf orientation) {
        this.orientation.set(orientation).normalize();
        this.updateMatrix();
        return this;
    }

    /**
     * Sets the size of the light's surface
     *
     * @param x The length, in blocks, of the light's surface.
     * @param y The width, in blocks, of the light's surface.
     */
    public AreaLight setSize(double x, double y) {
        this.size.set(x, y);
        this.markDirty();
        return this;
    }

    /**
     * Sets the maximum angle the light can influence.
     *
     * @param angle The maximum angle of the light's influence
     */
    public AreaLight setAngle(float angle) {
        this.angle = angle;
        this.markDirty();
        return this;
    }
    
    /**
     * Sets the maximum distance the light can influence.
     *
     * @param distance The maximum area of influence for the light
     */
    public AreaLight setDistance(float distance) {
        this.distance = distance;
        this.markDirty();
        return this;
    }

    /**
     * Sets the additional linear falloff factor for attenuation.
     *
     * @param falloff The linear falloff factor
     */
    public AreaLight setFalloff(float falloff) {
        this.falloff = falloff;
        this.markDirty();
        return this;
    }

    protected void updateMatrix() {
        Vector3d position = this.getPosition();
        Quaternionf orientation = this.getOrientation();
        this.matrix.rotation(orientation).translate(position);
        this.markDirty();
    }

    @Override
    public AreaLight clone() {
        AreaLight light = new AreaLight();
        light.matrix.set(this.matrix);
        light.size.set(this.size);
        light.angle = this.angle;
        light.distance = this.distance;
        light.falloff = this.falloff;
        light.markDirty();
        return light;
    }
}
