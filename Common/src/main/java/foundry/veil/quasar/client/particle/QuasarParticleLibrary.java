package foundry.veil.quasar.client.particle;

import gg.moonflower.molangcompiler.api.MolangExpression;
import gg.moonflower.molangcompiler.api.object.MolangLibrary;

import java.util.function.BiConsumer;

public class QuasarParticleLibrary extends MolangLibrary {

    private final QuasarParticle particle;

    public QuasarParticleLibrary(QuasarParticle particle) {
        this.particle = particle;
    }

    @Override
    protected void populate(BiConsumer<String, MolangExpression> consumer) {
        consumer.accept("setPosition", MolangExpression.function(3, context -> {
            this.particle.getPosition().set(context.get(0), context.get(1), context.get(2));
            return 0.0F;
        }));
        consumer.accept("setRotation", MolangExpression.function(3, context -> {
            this.particle.getRotation().set(context.get(0), context.get(1), context.get(2));
            return 0.0F;
        }));
        consumer.accept("setVelocity", MolangExpression.function(3, context -> {
            this.particle.getVelocity().set(context.get(0), context.get(1), context.get(2));
            return 0.0F;
        }));
    }

    @Override
    protected String getName() {
        return "quasar";
    }
}
