package foundry.veil.api.quasar.emitters.module.render;

import foundry.veil.api.client.color.Color;
import foundry.veil.api.client.color.Colorc;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.PointLightData;
import foundry.veil.api.client.render.light.renderer.LightRenderHandle;
import foundry.veil.api.quasar.emitters.module.RenderParticleModule;
import foundry.veil.api.quasar.emitters.module.UpdateParticleModule;
import foundry.veil.api.quasar.particle.QuasarParticle;

public class CustomLightModule implements UpdateParticleModule, RenderParticleModule {

    private final Color lastColor;
    private final Color color;
    private final Color renderColor;
    private final PointLightData light;
    private float brightness;
    private LightRenderHandle<PointLightData> lightHandle;

    public CustomLightModule() {
        this.lastColor = new Color(Color.WHITE);
        this.color = new Color(Color.WHITE);
        this.renderColor = new Color(Color.WHITE);
        this.light = new PointLightData();
        this.lightHandle = null;
    }

    @Override
    public void update(QuasarParticle particle) {
        this.lastColor.set(this.color);
        float brightness = this.brightness * this.color.alpha();

        if (this.color.luminance() < 0.1 && brightness < 0.1) {
            this.onRemove();
        } else {
            this.light.setColor(this.color).setBrightness(brightness);
            if (this.lightHandle == null) {
                this.lightHandle = VeilRenderSystem.renderer().getLightRenderer().addLight(this.light);
            }
        }
    }

    @Override
    public void render(QuasarParticle particle, float partialTicks) {
        if (this.lightHandle == null) {
            return;
        }

        this.light.setPosition(particle.getRenderData().getRenderPosition());
        this.lastColor.lerp(this.color, partialTicks, this.renderColor);
        this.light.setColor(this.renderColor);
        this.light.setBrightness(this.brightness * this.renderColor.alpha());
    }

    @Override
    public void onRemove() {
        if (this.lightHandle != null) {
            this.lightHandle.free();
            this.lightHandle = null;
        }
    }

    public Colorc getColor() {
        return this.color;
    }

    public float getBrightness() {
        return this.brightness;
    }

    public float getRadius() {
        return this.light.getRadius();
    }

    public void setBrightness(float brightness) {
        this.brightness = brightness;
    }

    public void setRadius(float radius) {
        this.light.setRadius(radius);
    }
}
