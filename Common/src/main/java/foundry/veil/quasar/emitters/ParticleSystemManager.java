package foundry.veil.quasar.emitters;

import foundry.veil.quasar.data.QuasarParticles;
import foundry.veil.quasar.data.ParticleEmitterData;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

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

    public @Nullable ParticleEmitter createEmitter(Level level, ResourceLocation name) {
        if (!(level instanceof ClientLevel clientLevel)) {
            return null;
        }

        ParticleEmitterData data = QuasarParticles.registryAccess().registryOrThrow(QuasarParticles.EMITTER).get(name);
        return data != null ? new ParticleEmitter(clientLevel, data) : null;
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

        PARTICLE_COUNT = this.particleEmitters.stream().mapToInt(ParticleEmitter::getParticleCount).sum();
        // FIXME
    }

    public float getSpawnScale() {
        return 1 - (float) (PARTICLE_COUNT - 1000) / 3000 * 0.66f;
    }
}
