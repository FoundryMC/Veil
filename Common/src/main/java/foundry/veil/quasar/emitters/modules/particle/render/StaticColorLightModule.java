package foundry.veil.quasar.emitters.modules.particle.render;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.deferred.VeilDeferredRenderer;
import foundry.veil.api.client.render.deferred.light.PointLight;
import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.data.module.init.LightModuleData;
import foundry.veil.quasar.emitters.modules.particle.RenderParticleModule;
import org.joml.Vector4f;

public class StaticColorLightModule implements RenderParticleModule {

    private final LightModuleData data;
    private final Vector4f color;
    private final float brightness;
    private PointLight light;

    public StaticColorLightModule(LightModuleData data) {
        this.data = data;
        this.color = data.color().getColor(0.0F);
        this.brightness = data.brightness() * this.color.w;
        this.light = null;
    }

    public boolean isVisible() {
        return this.color.lengthSquared() < 0.1 && this.brightness < 0.1;
    }

    @Override
    public void render(QuasarParticle particle, float partialTicks) {
        VeilDeferredRenderer deferredRenderer = VeilRenderSystem.renderer().getDeferredRenderer();
        if (!deferredRenderer.isEnabled()) {
            this.onRemove();
            return;
        }

        if (this.light == null) {
            this.light = new PointLight()
                    .setColor(this.color.x, this.color.y, this.color.z)
                    .setBrightness(this.brightness)
                    .setRadius(this.data.radius())
                    .setFalloff(this.data.falloff());
            deferredRenderer.getLightRenderer().addLight(this.light);
        }

        this.light.setPosition(particle.getRenderData().getRenderPosition());
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
