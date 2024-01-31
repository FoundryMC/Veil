package foundry.veil.quasar.emitters.modules.particle.init;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.deferred.light.Light;
import foundry.veil.api.client.render.deferred.light.PointLight;
import foundry.veil.quasar.client.particle.QuasarVanillaParticle;
import foundry.veil.quasar.emitters.modules.ModuleType;
import foundry.veil.quasar.util.ColorGradient;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class LightModule implements InitParticleModule {
    public static final Codec<LightModule> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ColorGradient.CODEC.fieldOf("gradient").forGetter(LightModule::getColor),
                    Codec.FLOAT.fieldOf("brightness").forGetter(LightModule::getBrightness),
                    Codec.FLOAT.fieldOf("falloff").forGetter(LightModule::getFallOff),
                    Codec.FLOAT.fieldOf("radius").forGetter(LightModule::getRadius)
            ).apply(instance, LightModule::new));
    ColorGradient gradient;
    float brightness;
    float fallOff;
    float radius;

    public LightModule(ColorGradient color, float brightness, float fallOff, float radius) {
        this.gradient = color;
        this.brightness = brightness;
        this.fallOff = fallOff;
        this.radius = radius;
    }

    @NotNull
    @Override
    public ModuleType<?> getType() {
        return ModuleType.LIGHT_MODULE;
    }

    public ColorGradient getColor() {
        return gradient;
    }

    public float getBrightness() {
        return brightness;
    }

    public float getFallOff() {
        return fallOff;
    }

    public float getRadius() {
        return radius;
    }

    @Override
    public void run(QuasarVanillaParticle particle) {
        if(VeilRenderSystem.renderer().getDeferredRenderer().isEnabled()){
            if(VeilRenderSystem.renderer().getDeferredRenderer().getLightRenderer().getLights(Light.Type.POINT).stream().filter(light ->
                            light.isVisible(VeilRenderSystem.renderer().getCullingFrustum()) && light.getColor().x() + light.getColor().y() + light.getColor().z() > 0.35f
                    ).toList().size() > 500) return;
            particle.light = new PointLight();
            particle.lightGradient = gradient;
            particle.light.setPosition(particle.getPos().x, particle.getPos().y, particle.getPos().z);
            particle.light.setColor(toLightColor(gradient.getColor(0.0f)).mul(getBrightness()));
            particle.light.setRadius(radius);
            particle.light.setFalloff(fallOff);
            VeilRenderSystem.renderer().getDeferredRenderer().getLightRenderer().addLight(particle.light);
        }
    }

    public static Vector3f toLightColor(Vector4f gradientColor) {
        return new Vector3f(gradientColor.x() * gradientColor.w(), gradientColor.y() * gradientColor.w(), gradientColor.z() * gradientColor.w());
    }
    @Override
    public InitParticleModule copy() {
        return new LightModule(gradient, brightness, fallOff, radius);
    }
}
