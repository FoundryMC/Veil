package foundry.veil.api.quasar.emitters.module.render;

import foundry.veil.Veil;
import foundry.veil.api.client.color.Color;
import foundry.veil.api.quasar.emitters.module.RenderParticleModule;
import foundry.veil.api.quasar.particle.QuasarParticle;
import foundry.veil.impl.quasar.ColorGradient;
import gg.moonflower.molangcompiler.api.MolangExpression;
import gg.moonflower.molangcompiler.api.exception.MolangRuntimeException;

public class ColorRenderModule implements RenderParticleModule {

    private static final ThreadLocal<Color> COLOR = ThreadLocal.withInitial(Color::new);

    private final ColorGradient gradient;
    private final MolangExpression interpolant;
    private boolean error;

    public ColorRenderModule(ColorGradient gradient, MolangExpression interpolant) {
        this.gradient = gradient;
        this.interpolant = interpolant;
    }

    @Override
    public void render(QuasarParticle particle, float partialTicks) {
        float percentage;
        try {
            percentage = particle.getEnvironment().resolve(this.interpolant);
        } catch (MolangRuntimeException e) {
            percentage = 0;
            if (!this.error) {
                this.error = true;
                Veil.LOGGER.error("Error evaluating color for particle: {}", particle.getData().getRegistryId(), e);
            }
        }
        particle.getRenderData().setColor(this.gradient.getColor(percentage, COLOR.get()));
    }
}
