package foundry.veil.quasar;

import com.mojang.blaze3d.vertex.PoseStack;
import foundry.veil.api.client.render.CullFrustum;
import foundry.veil.quasar.data.ParticleEmitterData;
import foundry.veil.quasar.data.QuasarParticles;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ParticleSystemManager {

    private static final int MAX_PARTICLES = 10000;

    private final List<ParticleEmitter> particleEmitters = new ArrayList<>();
    private int particleCount = 0;

    private ClientLevel level;
    private TickTaskSchedulerImpl scheduler;

    public ParticleSystemManager() {
        this.level = null;
    }

    @ApiStatus.Internal
    public void setLevel(@Nullable ClientLevel level) {
        if (this.scheduler != null) {
            this.scheduler.shutdown();
        }

        this.clear();
        this.level = level;
        this.scheduler = new TickTaskSchedulerImpl();
    }

    public @Nullable ParticleEmitter createEmitter(ResourceLocation name) {
        if (this.level == null) {
            return null;
        }
        ParticleEmitterData data = QuasarParticles.registryAccess().registryOrThrow(QuasarParticles.EMITTER).get(name);
        return data != null ? new ParticleEmitter(this, this.level, data) : null;
    }

    public void addParticleSystem(ParticleEmitter particleEmitter) {
        this.scheduler.execute(() -> this.particleEmitters.add(particleEmitter));
    }

    public void clear() {
        for (ParticleEmitter particleEmitter : this.particleEmitters) {
            particleEmitter.onRemoved();
        }
        this.particleEmitters.clear();
    }

    @ApiStatus.Internal
    public void tick() {
        if (this.level == null) {
            return;
        }

        this.scheduler.run();
        Iterator<ParticleEmitter> iterator = this.particleEmitters.iterator();
        while (iterator.hasNext()) {
            ParticleEmitter emitter = iterator.next();
            emitter.tick();
            if (emitter.isRemoved()) {
                emitter.onRemoved();
                iterator.remove();
            }
        }

        this.particleCount = this.particleEmitters.stream().mapToInt(ParticleEmitter::getParticleCount).sum();
        // FIXME
    }

    @ApiStatus.Internal
    public void render(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Camera camera, CullFrustum frustum, float partialTicks) {
        for (ParticleEmitter emitter : this.particleEmitters) {
            emitter.render(poseStack, bufferSource, camera, partialTicks);
        }
        bufferSource.endLastBatch();
    }

    public int getEmitterCount() {
        return this.particleEmitters.size();
    }

    public int getParticleCount() {
        return this.particleCount;
    }

    public float getSpawnScale() {
        return (float) (MAX_PARTICLES - this.particleCount) / (float) MAX_PARTICLES;
    }
}
