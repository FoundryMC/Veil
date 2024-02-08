package foundry.veil.quasar.emitters.modules.particle.render;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.deferred.VeilDeferredRenderer;
import foundry.veil.api.client.render.deferred.light.PointLight;
import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.data.module.init.LightModuleData;
import foundry.veil.quasar.emitters.modules.particle.RenderParticleModule;
import foundry.veil.quasar.emitters.modules.particle.UpdateParticleModule;
import org.joml.Vector4f;

public class DynamicColorLightModule implements UpdateParticleModule, RenderParticleModule {

    private final LightModuleData data;
    private final Vector4f lastColor;
    private final Vector4f color;
    private final Vector4f renderColor;
    private PointLight light;

    public DynamicColorLightModule(LightModuleData data) {
        this.data = data;
        this.lastColor = new Vector4f(1.0F);
        this.color = new Vector4f(1.0F);
        this.renderColor = new Vector4f(1.0F);
        this.light = null;
    }

    @Override
    public void update(QuasarParticle particle) {
        VeilDeferredRenderer deferredRenderer = VeilRenderSystem.renderer().getDeferredRenderer();
        if (!deferredRenderer.isEnabled()) {
            this.onRemove();
            return;
        }

        this.lastColor.set(this.color);
        this.color.set(this.data.color().getColor((float) particle.getAge() / (float) particle.getLifetime()));
        float brightness = this.data.brightness() * this.color.w;

        if (this.color.lengthSquared() < 0.1 && brightness < 0.1) {
            this.onRemove();
        } else {
            if (this.light == null) {
                this.light = new PointLight()
                        .setRadius(this.data.radius())
                        .setFalloff(this.data.falloff());
                deferredRenderer.getLightRenderer().addLight(this.light);
            }
            this.light.setColor(this.color.x, this.color.y, this.color.z);
            this.light.setBrightness(this.data.brightness() * this.color.w);
        }
    }

    @Override
    public void render(QuasarParticle particle, float partialTicks) {
        if (this.light == null) {
            return;
        }

        this.light.setPosition(particle.getRenderData().getRenderPosition());
        this.lastColor.lerp(this.color, partialTicks, this.renderColor);
        this.light.setColor(this.renderColor.x, this.renderColor.y, this.renderColor.z);
        this.light.setBrightness(this.data.brightness() * this.renderColor.w);
    }

    @Override
    public void onRemove() {
        if (this.light != null) {
            VeilDeferredRenderer deferredRenderer = VeilRenderSystem.renderer().getDeferredRenderer();
            if (deferredRenderer.isEnabled()) {
                deferredRenderer.getLightRenderer().removeLight(this.light);
            }
            this.light = null;
        }
    }
}
