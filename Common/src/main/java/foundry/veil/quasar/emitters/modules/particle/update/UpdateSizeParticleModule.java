package foundry.veil.quasar.emitters.modules.particle.update;

import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.emitters.modules.particle.UpdateParticleModule;
import gg.moonflower.molangcompiler.api.MolangExpression;
import gg.moonflower.molangcompiler.api.exception.MolangRuntimeException;

public class UpdateSizeParticleModule implements UpdateParticleModule {

    private final MolangExpression size;

    public UpdateSizeParticleModule(MolangExpression size) {
        this.size = size;
    }

    @Override
    public void update(QuasarParticle particle) {
        try {
            particle.setScale(particle.getEnvironment().resolve(this.size));
        } catch (MolangRuntimeException e) {
            e.printStackTrace();
            particle.setScale(1.0F);
        }
    }
}
