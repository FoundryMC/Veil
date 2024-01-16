package foundry.veil.quasar.emitters;

import foundry.veil.quasar.emitters.modules.particle.update.forces.AbstractParticleForce;
import net.minecraft.world.phys.Vec3;


import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ParticleSystemManager {
    private static ParticleSystemManager instance;
    private Queue<ParticleEmitter> particleEmitters = new ConcurrentLinkedQueue<>();
    private Queue<ParticleEmitter> particleSystemsToRemove = new ConcurrentLinkedQueue<>();
    private Queue<ParticleEmitter> particleSystemsToAdd = new ConcurrentLinkedQueue<>();
    public static int PARTICLE_COUNT = 0;
    public static ParticleSystemManager getInstance() {
        if (instance == null) {
            instance = new ParticleSystemManager();
        }
        return instance;
    }

    public void applyForceToParticles(Vec3 center, float radius, AbstractParticleForce... forces){
        for (ParticleEmitter particleEmitter : particleEmitters) {
                if(particleEmitter.emitterModule.getPosition().distanceTo(center) < radius){
                    particleEmitter.getParticleData().addForces(forces);
                }
        }
    }

    public void removeForcesFromParticles(Vec3 center, float radius, AbstractParticleForce... forces){
        for (ParticleEmitter particleEmitter : particleEmitters) {
            if(particleEmitter.emitterModule.getPosition().distanceTo(center) < radius){
                particleEmitter.getParticleData().removeForces(forces);
            }
        }
    }

    public void addParticleSystem(ParticleEmitter particleEmitter) {
        ParticleEmitter emitter = particleEmitter;
//        EmitterInstantiationEvent event = new EmitterInstantiationEvent(emitter);
//        MinecraftForge.EVENT_BUS.post(event);
//        emitter = event.getEmitter();
        particleEmitters.add(emitter);
    }

    public void addDelayedParticleSystem(ParticleEmitter particleEmitter) {
        ParticleEmitter emitter = particleEmitter;
//        EmitterInstantiationEvent event = new EmitterInstantiationEvent(emitter);
//        MinecraftForge.EVENT_BUS.post(event);
//        emitter = event.getEmitter();
        particleSystemsToAdd.add(emitter);
    }

    public void removeDelayedParticleSystem(ParticleEmitter particleEmitter) {
        particleSystemsToRemove.add(particleEmitter);
    }

    public void clear() {
        particleEmitters.clear();
    }

    public void tick() {
        particleEmitters.addAll(particleSystemsToAdd);
        particleSystemsToAdd.clear();
        particleEmitters.forEach(ParticleEmitter::tick);
        particleEmitters.removeIf(emitter -> emitter.isComplete);
        particleEmitters.removeAll(particleSystemsToRemove);
        particleSystemsToRemove.clear();
        tickLimiter();
    }

    public void tickLimiter() {
        PARTICLE_COUNT = particleEmitters.stream().mapToInt(emitter -> emitter.particleCount).sum();
        float delta = (float) (PARTICLE_COUNT - 1000) / 3000;
//        float fpsDelta = (float) Minecraft.getInstance().getFps() / OptimizationUtil.getStableFps();
//        delta = Math.min(delta, fpsDelta);
        float rate = 1 - delta * 0.66f;
        particleEmitters.forEach(emitter -> {
            emitter.emitterModule.setRate((int) Math.max(1, emitter.emitterModule.baseRate * rate));
            emitter.emitterModule.setCount((int) Math.max(1, emitter.emitterModule.baseCount * rate));
        });
    }
}
