package foundry.veil.quasar.emitters.modules.particle.render;

import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.emitters.modules.ModuleType;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector4f;

public class RandomColorParticleModule implements RenderParticleModule {
    Vector4f[] colors;

    public RandomColorParticleModule(Vector4f... colors) {
        this.colors = colors;
    }
    @Override
    public void apply(QuasarParticle particle, float partialTicks, RenderData data) {
        int index = (int) (Math.random() * colors.length);
        Vector4f color = colors[index];
        data.setRGBA(color.x(), color.y(), color.z(), color.w());
    }

    @NotNull
    @Override
    public ModuleType<?> getType() {
        return null;
    }

}
