package foundry.veil.quasar.event;

import foundry.veil.quasar.client.particle.QuasarParticle;
import net.minecraftforge.eventbus.api.Event;

public class QuasarParticleTickEvent extends Event {
    private QuasarParticle particle;

    public QuasarParticleTickEvent(QuasarParticle particle) {
        this.particle = particle;
    }

    public QuasarParticle getParticle() {
        return particle;
    }
}
