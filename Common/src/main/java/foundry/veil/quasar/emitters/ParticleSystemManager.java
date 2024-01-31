package foundry.veil.quasar.emitters;

import foundry.veil.quasar.emitters.modules.particle.update.forces.AbstractParticleForce;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ParticleSystemManager {

    private static ParticleSystemManager instance;
    private final List<ParticleEmitter> particleEmitters = new ArrayList<>();
    private final Queue<ParticleEmitter> particleSystemsToAdd = new ConcurrentLinkedQueue<>();
    public static int PARTICLE_COUNT = 0;

    public static ParticleSystemManager getInstance() {
        if (instance == null) {
            instance = new ParticleSystemManager();
        }
        return instance;
    }

    public void applyForceToParticles(Vec3 center, float radius, AbstractParticleForce... forces) {
//        for (ParticleEmitter particleEmitter : this.particleEmitters) {
//                if(particleEmitter.emitterModule.getPosition().distanceTo(center) < radius){
//                    particleEmitter.getParticleData().addForces(forces);
//                }
//        }
    }

    public void removeForcesFromParticles(Vec3 center, float radius, AbstractParticleForce... forces) {
//        for (ParticleEmitter particleEmitter : this.particleEmitters) {
//            if(particleEmitter.emitterModule.getPosition().distanceTo(center) < radius){
//                particleEmitter.getParticleData().removeForces(forces);
//            }
//        }
    }

    public void addParticleSystem(ParticleEmitter particleEmitter) {
        this.particleSystemsToAdd.add(particleEmitter);
    }

    public void clear() {
        this.particleEmitters.clear();
    }

    public void tick() {
        this.particleEmitters.addAll(this.particleSystemsToAdd);
        this.particleSystemsToAdd.clear();

        Iterator<ParticleEmitter> iterator = this.particleEmitters.iterator();
        while (iterator.hasNext()) {
            ParticleEmitter emitter = iterator.next();
            emitter.tick();
            if (emitter.isRemoved()) {
                iterator.remove();
            }
        }

        PARTICLE_COUNT = this.particleEmitters.stream().mapToInt(emitter -> emitter.getParticleCount()).sum();
        // FIXME
    }

    public float getSpawnScale() {
        return 1 - (float) (PARTICLE_COUNT - 1000) / 3000 * 0.66f;
    }
}
