package foundry.veil.api.client.render.light.data;

import foundry.veil.api.client.color.Colorc;
import foundry.veil.api.client.editor.EditorAttributeProvider;
import foundry.veil.api.client.registry.LightTypeRegistry;
import foundry.veil.api.client.render.CullFrustum;
import foundry.veil.api.client.render.light.IndirectLightData;
import imgui.ImGui;
import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3fc;

import java.nio.ByteBuffer;

/**
 * Represents a light where all rays come from a position in space.
 *
 * @since 2.0.0
 */
public class PointLightData extends LightData implements IndirectLightData, EditorAttributeProvider {

    protected final Vector3d position;
    protected float radius;

    public PointLightData() {
        this.position = new Vector3d();
        this.radius = 1.0F;
    }

    @Override
    public float getRadius() {
        return this.radius;
    }

    @Override
    public Vector3dc getPosition() {
        return this.position;
    }

    public PointLightData setPosition(Vector3dc pos) {
        this.position.set(pos);
        return this;
    }

    public PointLightData setPosition(double x, double y, double z) {
        this.position.set(x, y, z);
        return this;
    }

    public PointLightData setRadius(float radius) {
        this.radius = radius;
        return this;
    }

    @Override
    public PointLightData setColor(Vector3fc color) {
        super.setColor(color);
        return this;
    }

    @Override
    public PointLightData setColor(Colorc color) {
        this.setColor(color.red(), color.green(), color.blue());
        return this;
    }

    @Override
    public PointLightData setColor(float red, float green, float blue) {
        super.setColor(red, green, blue);
        return this;
    }

    @Override
    public PointLightData setColor(int color) {
        super.setColor(color);
        return this;
    }

    @Override
    public PointLightData setBrightness(float brightness) {
        super.setBrightness(brightness);
        return this;
    }

    @Override
    public boolean isVisible(CullFrustum frustum) {
        return frustum.testSphere(this.position, this.radius * 1.4F);
    }

    @Override
    public void store(ByteBuffer buffer) {
        this.position.getf(buffer.position(), buffer);
        buffer.position(buffer.position() + Float.BYTES * 3);
        buffer.putFloat(this.color.red() * this.brightness);
        buffer.putFloat(this.color.green() * this.brightness);
        buffer.putFloat(this.color.blue() * this.brightness);
        buffer.putFloat(this.radius);
    }

    @Override
    public PointLightData setTo(Camera camera) {
        Vec3 pos = camera.getPosition();
        this.position.set(pos.x, pos.y, pos.z);
        return this;
    }

    @Override
    public LightTypeRegistry.LightType<?> getType() {
        return LightTypeRegistry.POINT.get();
    }

    @Override
    public void renderImGuiAttributes() {
        double[] editX = new double[]{this.position.x()};
        double[] editY = new double[]{this.position.y()};
        double[] editZ = new double[]{this.position.z()};

        float[] editRadius = new float[]{this.radius};

        float totalWidth = ImGui.calcItemWidth();
        ImGui.pushItemWidth(totalWidth / 3.0F - (ImGui.getStyle().getItemInnerSpacingX() * 0.58F));
        if (ImGui.dragScalar("##x", editX, 0.02F)) {
            this.position.x = editX[0];
        }
        ImGui.sameLine(0, ImGui.getStyle().getItemInnerSpacingX());
        if (ImGui.dragScalar("##y", editY, 0.02F)) {
            this.position.y = editY[0];
        }
        ImGui.sameLine(0, ImGui.getStyle().getItemInnerSpacingX());
        if (ImGui.dragScalar("##z", editZ, 0.02F)) {
            this.position.z = editZ[0];
        }

        ImGui.popItemWidth();
        ImGui.sameLine(0, ImGui.getStyle().getItemInnerSpacingX());
        ImGui.text("position");

        if (ImGui.dragScalar("radius", editRadius, 0.02F, 0.0F)) {
            this.setRadius(editRadius[0]);
        }
    }
}
