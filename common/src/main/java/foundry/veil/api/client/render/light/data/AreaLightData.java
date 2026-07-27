package foundry.veil.api.client.render.light.data;

import com.mojang.blaze3d.vertex.VertexConsumer;
import foundry.veil.api.client.color.Colorc;
import foundry.veil.api.client.editor.EditorAttributeProvider;
import foundry.veil.api.client.registry.LightTypeRegistry;
import foundry.veil.api.client.render.CullFrustum;
import foundry.veil.api.client.render.MatrixStack;
import foundry.veil.api.client.render.light.DDALightData;
import foundry.veil.api.client.render.light.InstancedLightData;
import foundry.veil.api.client.render.light.LightGuideProvider;
import imgui.ImGui;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.joml.*;

import java.lang.Math;
import java.nio.ByteBuffer;

/**
 * Represents a light emitting quad in the world.
 *
 * @since 2.0.0
 */
public class AreaLightData extends LightData implements InstancedLightData, DDALightData, EditorAttributeProvider, LightGuideProvider {

    private static final float MAX_ANGLE_SIZE = (float) (65535.0 / 2.0 / Math.PI);

    protected final Vector3d position;
    protected final Quaternionf orientation;
    private final Matrix4d matrix;

    protected final Vector2f size;

    protected float angle;
    protected float distance;
    protected boolean occlusionEnabled;
    protected float inscattering;

    public AreaLightData() {
        this.matrix = new Matrix4d();
        this.position = new Vector3d();
        this.orientation = new Quaternionf();

        this.size = new Vector2f(1.0F, 1.0F);

        this.angle = (float) Math.toRadians(45);
        this.distance = 1.0F;
        this.occlusionEnabled = false;
        this.inscattering = 0.0F;
    }

    /**
     * @deprecated No longer used
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    @Deprecated
    protected void updateMatrix() {
        Quaternionfc orientation = this.getOrientation();
        this.matrix.rotation(orientation).translate(this.position);
    }

    @Override
    public LightTypeRegistry.LightType<?> getType() {
        return LightTypeRegistry.AREA.get();
    }

    /**
     * @return The XYZ position of this light in the world
     * @apiNote The return type will change to {@link Vector3dc} in 5.0.0. Use {@link #getPositionMutable()} to mutate this
     */
    public Vector3d getPosition() {
        return this.position;
    }

    /**
     * Allows the value to be safely modified.
     *
     * @return The XYZ position of this light in the world
     * @since 4.3.0
     */
    public Vector3d getPositionMutable() {
        this.markDirty();
        return this.position;
    }

    /**
     * @return The current orientation of the light
     * @apiNote The return type will change to {@link Quaternionfc} in 5.0.0. Use {@link #getOrientationMutable()} to mutate this
     */
    public Quaternionf getOrientation() {
        return this.orientation;
    }

    /**
     * Allows the value to be safely modified.
     *
     * @return The current orientation of the light
     * @since 4.3.0
     */
    public Quaternionf getOrientationMutable() {
        return this.orientation;
    }

    /**
     * @return The size of the light's surface
     * @apiNote The return type will change to {@link Vector2fc} in 5.0.0. Use {@link #getSizeMutable()} to mutate this
     */
    public Vector2f getSize() {
        return this.size;
    }

    /**
     * Allows the value to be safely modified.
     *
     * @return The size of the light's surface
     * @since 4.3.0
     */
    public Vector2f getSizeMutable() {
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

    @Override
    public boolean isOcclusionEnabled() {
        return this.occlusionEnabled;
    }

    /**
     * @return The strength of the light's in-scattering effect.
     */
    public float getInscatteringStrength() {
        return this.inscattering;
    }

    /**
     * Sets the size of the light's surface
     *
     * @param x The length, in blocks, of the light's surface.
     * @param y The width, in blocks, of the light's surface.
     */
    public AreaLightData setSize(double x, double y) {
        this.size.set(x, y);
        this.markDirty();
        return this;
    }

    /**
     * Sets the maximum angle the light can influence.
     *
     * @param angle The maximum angle of the light's influence in radians
     */
    public AreaLightData setAngle(float angle) {
        this.angle = angle;
        this.markDirty();
        return this;
    }

    /**
     * Sets the maximum distance the light can influence.
     *
     * @param distance The maximum area of influence for the light
     */
    public AreaLightData setDistance(float distance) {
        this.distance = distance;
        this.markDirty();
        return this;
    }

    public AreaLightData setOcclusionEnabled(boolean occlusionEnabled) {
        this.occlusionEnabled = occlusionEnabled;
        this.markDirty();
        return this;
    }

    public AreaLightData setInscatteringStrength(float inscattering) {
        this.inscattering = inscattering;
        this.markDirty();
        return this;
    }

    @Override
    public AreaLightData setColor(Vector3fc color) {
        super.setColor(color);
        return this;
    }

    @Override
    public AreaLightData setColor(Colorc color) {
        this.setColor(color.red(), color.green(), color.blue());
        return this;
    }

    @Override
    public AreaLightData setColor(float red, float green, float blue) {
        super.setColor(red, green, blue);
        return this;
    }

    @Override
    public AreaLightData setColor(int color) {
        super.setColor(color);
        return this;
    }

    @Override
    public AreaLightData setBrightness(float brightness) {
        super.setBrightness(brightness);
        return this;
    }

    @Override
    public void store(ByteBuffer buffer) {
        this.matrix.identity().rotation(this.orientation).translate(this.position).getFloats(buffer.position(), buffer);
        buffer.position(buffer.position() + Float.BYTES * 16);

        buffer.putFloat(this.color.red() * this.brightness);
        buffer.putFloat(this.color.green() * this.brightness);
        buffer.putFloat(this.color.blue() * this.brightness);

        this.size.get(buffer.position(), buffer);
        buffer.position(buffer.position() + Float.BYTES * 2);

        buffer.putShort((short) Mth.clamp((int) (this.angle * MAX_ANGLE_SIZE), 0, 65535));
        buffer.putFloat(this.distance);
        buffer.putFloat(this.occlusionEnabled ? 1.0F : 0.0F);
        buffer.putFloat(this.inscattering);
    }

    @Override
    public boolean isVisible(CullFrustum frustum) {
        float radius = Math.max(this.size.x, this.size.y) + this.distance;
        return frustum.testAab(
                this.position.x - radius,
                this.position.y - radius,
                this.position.z - radius,
                this.position.x + radius,
                this.position.y + radius,
                this.position.z + radius);
    }

    @Override
    public LightData setTo(Camera camera) {
        Vec3 pos = camera.getPosition();
        this.position.set(pos.x, pos.y, pos.z);
        this.orientation.identity().lookAlong(camera.getLookVector().mul(-1), camera.getUpVector());
        this.markDirty();
        return this;
    }

    @Override
    public void renderImGuiAttributes() {
        Vector3f orientationAngles = this.orientation.normalize().getEulerAnglesXYZ(new Vector3f());

        float[] editSize = new float[]{this.size.x(), this.size.y()};

        double[] editX = new double[]{this.position.x()};
        double[] editY = new double[]{this.position.y()};
        double[] editZ = new double[]{this.position.z()};

        float[] editXRot = new float[]{orientationAngles.x()};
        float[] editYRot = new float[]{orientationAngles.y()};
        float[] editZRot = new float[]{orientationAngles.z()};

        float[] editAngle = new float[]{this.angle};
        float[] editDistance = new float[]{this.distance};

        float[] editInscattering = new float[]{this.inscattering};

        if (ImGui.dragFloat2("size", editSize, 0.02F, 0.0001F)) {
            this.setSize(editSize[0], editSize[1]);
        }

        float totalWidth = ImGui.calcItemWidth();
        ImGui.pushItemWidth(totalWidth / 3.0F - (ImGui.getStyle().getItemInnerSpacingX() * 0.58F));
        if (ImGui.dragScalar("##x", editX, 0.02F)) {
            this.position.x = editX[0];
            this.markDirty();
        }
        ImGui.sameLine(0, ImGui.getStyle().getItemInnerSpacingX());
        if (ImGui.dragScalar("##y", editY, 0.02F)) {
            this.position.y = editY[0];
            this.markDirty();
        }
        ImGui.sameLine(0, ImGui.getStyle().getItemInnerSpacingX());
        if (ImGui.dragScalar("##z", editZ, 0.02F)) {
            this.position.z = editZ[0];
            this.markDirty();
        }

        ImGui.popItemWidth();
        ImGui.sameLine(0, ImGui.getStyle().getItemInnerSpacingX());
        ImGui.text("position");

        ImGui.pushItemWidth(totalWidth / 3.0F - (ImGui.getStyle().getItemInnerSpacingX() * 0.58F));
        if (ImGui.sliderAngle("##xrot", editXRot)) {
            this.orientation.identity().rotationXYZ(editXRot[0], orientationAngles.y(), orientationAngles.z());
            this.markDirty();
        }
        ImGui.sameLine(0, ImGui.getStyle().getItemInnerSpacingX());
        if (ImGui.sliderAngle("##yrot", editYRot)) {
            this.orientation.identity().rotationXYZ(orientationAngles.x(), editYRot[0], orientationAngles.z());
            this.markDirty();
        }
        ImGui.sameLine(0, ImGui.getStyle().getItemInnerSpacingX());
        if (ImGui.sliderAngle("##zrot", editZRot)) {
            this.orientation.identity().rotationXYZ(orientationAngles.x(), orientationAngles.y(), editZRot[0]);
            this.markDirty();
        }

        ImGui.popItemWidth();
        ImGui.sameLine(0, ImGui.getStyle().getItemInnerSpacingX());
        ImGui.text("orientation");

        if (ImGui.sliderAngle("##angle", editAngle, 0.1F, 180.0F, "%.1f")) {
            this.setAngle(editAngle[0]);
        }
        ImGui.sameLine(0, ImGui.getStyle().getItemInnerSpacingX());
        ImGui.text("angle");

        if (ImGui.dragScalar("distance", editDistance, 0.02F, 0.0F)) {
            this.setDistance(editDistance[0]);
        }

        if (ImGui.checkbox("Occluded", this.occlusionEnabled)) {
            this.setOcclusionEnabled(!this.occlusionEnabled);
        }

        if (ImGui.dragScalar("In-scattering", editInscattering, 0.01F, 0.0F)) {
            this.setInscatteringStrength(editInscattering[0]);
        }
    }

    @Override
    public void renderLightGuide(MatrixStack stack, VertexConsumer consumer) {
        stack.matrixPush();

        stack.translate(this.position.x, this.position.y, this.position.z);
        Vector3f rot = this.orientation.getEulerAnglesXYZ(new Vector3f()).mul(-1);
        stack.rotate(new Quaternionf().rotateX(rot.x).rotateLocalY(rot.y).rotateLocalZ(rot.z));

        Matrix4f pose = stack.position();

        consumer.addVertex(pose, -this.size.x, -this.size.y, 0).setColor(this.color.red(), this.color.green(), this.color.blue(), this.color.alpha());
        consumer.addVertex(pose, -this.size.x,  this.size.y, 0).setColor(this.color.red(), this.color.green(), this.color.blue(), this.color.alpha());
        consumer.addVertex(pose,  this.size.x,  this.size.y, 0).setColor(this.color.red(), this.color.green(), this.color.blue(), this.color.alpha());
        consumer.addVertex(pose,  this.size.x, -this.size.y, 0).setColor(this.color.red(), this.color.green(), this.color.blue(), this.color.alpha());
        consumer.addVertex(pose, -this.size.x, -this.size.y, 0).setColor(this.color.red(), this.color.green(), this.color.blue(), this.color.alpha());

        Vector3f endSize = new Vector3f(this.size.x + (this.distance * (float)Math.tan(this.angle * 0.5)), this.size.y + (this.distance * (float)Math.tan(this.angle * 0.5)), this.distance);

        consumer.addVertex(pose, -endSize.x, -endSize.y, this.distance).setColor(this.color.red(), this.color.green(), this.color.blue(), this.color.alpha());
        consumer.addVertex(pose, -endSize.x, endSize.y, this.distance).setColor(this.color.red(), this.color.green(), this.color.blue(), this.color.alpha());
        consumer.addVertex(pose, -this.size.x,  this.size.y, 0).setColor(this.color.red(), this.color.green(), this.color.blue(), this.color.alpha());
        consumer.addVertex(pose, -endSize.x, endSize.y, this.distance).setColor(this.color.red(), this.color.green(), this.color.blue(), this.color.alpha());
        consumer.addVertex(pose, endSize.x, endSize.y, this.distance).setColor(this.color.red(), this.color.green(), this.color.blue(), this.color.alpha());
        consumer.addVertex(pose,  this.size.x,  this.size.y, 0).setColor(this.color.red(), this.color.green(), this.color.blue(), this.color.alpha());
        consumer.addVertex(pose, endSize.x, endSize.y, this.distance).setColor(this.color.red(), this.color.green(), this.color.blue(), this.color.alpha());
        consumer.addVertex(pose, endSize.x, -endSize.y, this.distance).setColor(this.color.red(), this.color.green(), this.color.blue(), this.color.alpha());
        consumer.addVertex(pose,  this.size.x, -this.size.y, 0).setColor(this.color.red(), this.color.green(), this.color.blue(), this.color.alpha());
        consumer.addVertex(pose, endSize.x, -endSize.y, this.distance).setColor(this.color.red(), this.color.green(), this.color.blue(), this.color.alpha());
        consumer.addVertex(pose, -endSize.x, -endSize.y, this.distance).setColor(this.color.red(), this.color.green(), this.color.blue(), this.color.alpha());

        stack.matrixPop();
    }
}
