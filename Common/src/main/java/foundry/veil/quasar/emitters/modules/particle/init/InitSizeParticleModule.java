package foundry.veil.quasar.emitters.modules.particle.init;

import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.emitters.modules.particle.InitParticleModule;
import gg.moonflower.molangcompiler.api.MolangExpression;
import gg.moonflower.molangcompiler.api.exception.MolangRuntimeException;

public class InitSizeParticleModule implements InitParticleModule {

    private final MolangExpression size;

    public InitSizeParticleModule(MolangExpression size) {
        this.size = size;
    }

    @Override
    public void init(QuasarParticle particle) {
        try {
            particle.setScale(particle.getEnvironment().resolve(this.size));
        } catch (MolangRuntimeException e) {
            e.printStackTrace();
            particle.setScale(1.0F);
        }
    }
}
