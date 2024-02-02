package foundry.veil.quasar.client.particle;

import foundry.veil.quasar.client.particle.data.QuasarParticleData;
import foundry.veil.quasar.emitters.modules.particle.render.RenderData;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class QuasarParticle {

    private final QuasarParticleData data;
    private final ParticleModuleSet modules;
    private final Vector3d position;
    private final Vector3f rotation;
    private float scale;

    private final RenderData renderData;

    public QuasarParticle(QuasarParticleData data) {
        this.data = data;
        this.modules = ParticleModuleSet.builder().build();
        this.position = new Vector3d();
        this.rotation = new Vector3f();
        this.scale = 1.0F;

        this.renderData = new RenderData();
    }

    public static ParticleModuleSet createModuleSet(QuasarParticleData data) {
        ParticleModuleSet.Builder builder = ParticleModuleSet.builder();
        return builder.build();
    }

    public void tick() {
        this.renderData.tick();
    }

    public void render(float partialTicks) {
        this.renderData.render(this.position, this.rotation, this.scale, partialTicks);
    }

    public void setPitch(float pitch) {
        this.rotation.x = pitch;
    }

    public void setYaw(float yaw) {
        this.rotation.y = yaw;
    }

    public void setRoll(float roll) {
        this.rotation.z = roll;
    }

    public void setRotation(float pitch, float yaw, float roll) {
        this.rotation.set(pitch, yaw, roll);
    }

    public void setRotation(Vector3fc rotation) {
        this.rotation.set(rotation);
    }

    public void setRotation(Vector3dc rotation) {
        this.rotation.set(rotation);
    }

    public void vectorToRotation(Vector3dc vector) {
        this.rotation.set((float) Math.asin(vector.y()), (float) Math.atan2(vector.x(), vector.z()), 0);
    }
}
