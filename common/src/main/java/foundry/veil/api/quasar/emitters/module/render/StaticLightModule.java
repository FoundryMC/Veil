package foundry.veil.api.quasar.emitters.module.render;

import foundry.veil.api.client.color.Colorc;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.PointLightData;
import foundry.veil.api.client.render.light.renderer.LightRenderHandle;
import foundry.veil.api.quasar.data.module.init.LightModuleData;
import foundry.veil.api.quasar.emitters.module.RenderParticleModule;
import foundry.veil.api.quasar.particle.QuasarParticle;

public class StaticLightModule implements RenderParticleModule {

    private final PointLightData light;
    private LightRenderHandle<PointLightData> lightHandle;

    public StaticLightModule(LightModuleData data) {
        this(data.color().getColor(0.0F), data.brightness().getConstant(), data.radius().getConstant());
    }

    public StaticLightModule(Colorc color, float brightness, float radius) {
        this.light = new PointLightData().setColor(color).setBrightness(brightness).setRadius(radius);
    }

    public boolean isVisible() {
        return this.light.getColor().luminance() * this.light.getBrightness() >= 0.1;
    }

    @Override
    public void render(QuasarParticle particle, float partialTicks) {
        if (this.lightHandle == null) {
            this.lightHandle = VeilRenderSystem.renderer().getLightRenderer().addLight(this.light);
        }

        this.light.setPosition(particle.getRenderData().getRenderPosition());
    }

    @Override
    public void onRemove() {
        if (this.lightHandle != null) {
            this.lightHandle.free();
            this.lightHandle = null;
        }
    }
}
