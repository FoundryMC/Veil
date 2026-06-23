package foundry.veil.api.quasar.emitters.module.render;

import foundry.veil.api.client.color.Color;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.PointLightData;
import foundry.veil.api.client.render.light.renderer.LightRenderHandle;
import foundry.veil.api.quasar.data.module.init.LightModuleData;
import foundry.veil.api.quasar.emitters.module.RenderParticleModule;
import foundry.veil.api.quasar.emitters.module.UpdateParticleModule;
import foundry.veil.api.quasar.particle.QuasarParticle;
import net.minecraft.util.Mth;

public class DynamicLightModule implements UpdateParticleModule, RenderParticleModule {

    private final LightModuleData data;
    private float brightness;
    private float lastRadius;
    private float radius;

    private final Color lastColor;
    private final Color color;
    private final Color renderColor;
    private float lastBrightness;
    private final PointLightData light;
    private LightRenderHandle<PointLightData> lightHandle;

    private final boolean constantColor;
    private final boolean constantBrightness;
    private final boolean constantRadius;

    public DynamicLightModule(LightModuleData data) {
        this.data = data;

        this.constantColor = data.color().isConstant();
        this.constantBrightness = this.constantColor && data.brightness().isConstant();
        this.constantRadius = data.radius().isConstant();

        this.lastColor = new Color(Color.WHITE);
        this.color = new Color(Color.WHITE);
        this.renderColor = new Color(Color.WHITE);
        this.light = new PointLightData();

        if (this.constantColor) {
            data.color().getColor(0.0F, this.color);
            this.lastColor.set(this.color);
            this.renderColor.set(this.color);
        }

        if (this.constantBrightness) {
            this.brightness = data.brightness().getConstant();
            this.lastBrightness = this.brightness;
        }

        if (this.constantRadius) {
            this.radius = data.radius().getConstant();
            this.lastRadius = this.radius;
        }
        
        this.light.setBrightness(this.brightness * this.renderColor.alpha());
        this.light.setRadius(this.radius);
        this.light.setColor(this.color);
    }

    @Override
    public void update(QuasarParticle particle) {
        if (!this.constantColor) {
            this.lastColor.set(this.color);
            this.data.color().getColor((float) particle.getAge() / (float) particle.getLifetime(), this.color);
        }
        if (!this.constantBrightness) {
            this.lastBrightness = this.brightness;
            this.brightness = particle.getEnvironment().safeResolve(this.data.brightness());
        }
        if (!this.constantRadius) {
            this.lastRadius = this.radius;
            this.radius = particle.getEnvironment().safeResolve(this.data.radius());
        }

        float brightness = this.brightness * this.color.alpha();
        if (this.color.luminance() < 0.1 && brightness < 0.1) {
            this.onRemove();
            return;
        }

        if (this.lightHandle == null) {
            this.lightHandle = VeilRenderSystem.renderer().getLightRenderer().addLight(this.light);
        }
    }

    @Override
    public void render(QuasarParticle particle, float partialTicks) {
        if (this.light == null) {
            return;
        }

        this.light.setPosition(particle.getRenderData().getRenderPosition());

        if (!this.constantColor) {
            this.lastColor.lerp(this.color, partialTicks, this.renderColor);
            this.light.setColor(this.renderColor);
        }
        if (!this.constantBrightness) {
            this.light.setBrightness(Mth.lerp(partialTicks, this.lastBrightness, this.brightness) * this.renderColor.alpha());
        }
        if (!this.constantRadius) {
            this.light.setRadius(Mth.lerp(partialTicks, this.lastRadius, this.radius));
        }
    }

    @Override
    public void onRemove() {
        if (this.lightHandle != null) {
            this.lightHandle.free();
            this.lightHandle = null;
        }
    }
}
