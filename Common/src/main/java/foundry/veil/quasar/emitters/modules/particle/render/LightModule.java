package foundry.veil.quasar.emitters.modules.particle.render;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.deferred.VeilDeferredRenderer;
import foundry.veil.api.client.render.deferred.light.PointLight;
import foundry.veil.quasar.client.particle.QuasarVanillaParticle;
import foundry.veil.quasar.data.module.init.LightModuleData;
import org.joml.Vector4f;

public class LightModule implements RenderParticleModule {

    private final LightModuleData data;
    private PointLight light;

    public LightModule(LightModuleData data) {
        this.data = data;
        this.light = null;
    }

    @Override
    public void render(QuasarVanillaParticle particle, float partialTicks, RenderData data) {
        VeilDeferredRenderer deferredRenderer = VeilRenderSystem.renderer().getDeferredRenderer();
        if (!deferredRenderer.isEnabled()) {
            return;
        }

        Vector4f color = this.data.color().getColor((float) particle.getAge() / (float) particle.getLifetime());
        float brightness = this.data.brightness() * color.w;
        if (color.lengthSquared() < 0.1 || brightness < 0.1) {
            this.onRemove();
            return;
        }

        if (this.light == null) {
            this.light = new PointLight()
                    .setRadius(this.data.radius())
                    .setFalloff(this.data.falloff());
            deferredRenderer.getLightRenderer().addLight(this.light);
        }
        this.light.setPosition(data.getRenderPosition());
        this.light.setColor(color.x, color.y, color.z);
        this.light.setBrightness(brightness);
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
