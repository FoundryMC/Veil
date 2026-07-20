package foundry.veil.api.client.render.light.data;

import com.mojang.blaze3d.vertex.VertexConsumer;
import foundry.veil.api.client.color.Colorc;
import foundry.veil.api.client.editor.EditorAttributeProvider;
import foundry.veil.api.client.registry.LightTypeRegistry;
import foundry.veil.api.client.render.CullFrustum;
import foundry.veil.api.client.render.MatrixStack;
import foundry.veil.api.client.render.light.DDALightData;
import foundry.veil.api.client.render.light.IndirectLightData;
import foundry.veil.api.client.render.light.LightGuideProvider;
import imgui.ImGui;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3fc;

import java.nio.ByteBuffer;

/**
 * Represents a light where all rays come from a position in space.
 *
 * @since 2.0.0
 */
public class PointLightData extends LightData implements IndirectLightData, DDALightData, EditorAttributeProvider, LightGuideProvider {

    protected final Vector3d position;
    protected float radius;
    protected boolean occlusionEnabled;
    protected float inscattering;

    public PointLightData() {
        this.position = new Vector3d();
        this.radius = 1.0F;
        this.occlusionEnabled = false;
        this.inscattering = 0.0F;
    }

    @Override
    public float getRadius() {
        return this.radius;
    }

    @Override
    public Vector3dc getPosition() {
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

    public PointLightData setPosition(Vector3dc pos) {
        this.position.set(pos);
        this.markDirty();
        return this;
    }

    public PointLightData setPosition(double x, double y, double z) {
        this.position.set(x, y, z);
        this.markDirty();
        return this;
    }

    public PointLightData setRadius(float radius) {
        this.radius = radius;
        this.markDirty();
        return this;
    }

    public PointLightData setOcclusionEnabled(boolean occlusionEnabled) {
        this.occlusionEnabled = occlusionEnabled;
        this.markDirty();
        return this;
    }

    public PointLightData setInscatteringStrength(float inscattering) {
        this.inscattering = inscattering;
        this.markDirty();
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
        buffer.putFloat(this.occlusionEnabled ? 1.0F : 0.0F);
        buffer.putFloat(this.inscattering);
    }

    @Override
    public PointLightData setTo(Camera camera) {
        Vec3 pos = camera.getPosition();
        this.position.set(pos.x, pos.y, pos.z);
        this.markDirty();
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

        float[] editInscattering = new float[]{this.inscattering};

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

        if (ImGui.dragScalar("radius", editRadius, 0.02F, 0.0F)) {
            this.setRadius(editRadius[0]);
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

        Matrix4f pose = stack.position();

        for (int i = -8; i < 25; i++) {
            consumer.addVertex(pose, (float)Math.sin(i / 32.0f * Mth.TWO_PI) * this.radius, 0, (float)Math.cos(i / 32.0f * Mth.TWO_PI) * this.radius).setColor(this.color.red(), this.color.green(), this.color.blue(), this.color.alpha());
        }

        for (int i = -8; i < 25; i++) {
            consumer.addVertex(pose, (float)Math.sin(i / 32.0f * Mth.TWO_PI) * this.radius, (float)Math.cos(i / 32.0f * Mth.TWO_PI) * this.radius, 0).setColor(this.color.red(), this.color.green(), this.color.blue(), this.color.alpha());
        }

        for (int i = 25; i >= 16; i--) {
            consumer.addVertex(pose, (float)Math.sin(i / 32.0f * Mth.TWO_PI) * this.radius, (float)Math.cos(i / 32.0f * Mth.TWO_PI) * this.radius, 0).setColor(this.color.red(), this.color.green(), this.color.blue(), this.color.alpha());
        }

        for (int i = -8; i < 25; i++) {
            consumer.addVertex(pose, 0, (float)Math.sin(i / 32.0f * Mth.TWO_PI) * this.radius, (float)Math.cos(i / 32.0f * Mth.TWO_PI) * this.radius).setColor(this.color.red(), this.color.green(), this.color.blue(), this.color.alpha());
        }

        stack.matrixPop();
    }
}
