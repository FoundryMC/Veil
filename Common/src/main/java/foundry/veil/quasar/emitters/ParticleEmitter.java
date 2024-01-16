package foundry.veil.quasar.emitters;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.quasar.client.particle.data.QuasarParticleData;
import foundry.veil.quasar.client.particle.data.QuasarParticleDataRegistry;
import foundry.veil.quasar.emitters.modules.emitter.EmitterModule;
import foundry.veil.quasar.emitters.modules.emitter.settings.EmitterSettingsModule;
import foundry.veil.quasar.emitters.modules.emitter.settings.EmitterSettingsRegistry;
import foundry.veil.quasar.emitters.modules.particle.init.forces.InitialVelocityForce;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/*
 *  TODO:
 *   ✅ REQUIRED EmitterModule that controls lifetime, loop, rate, count, position
 *   ✅ OPTIONAL EmitterSettingsModule that controls randomization, emission area, emission shape, emission direction, emission speed, onEdge, linked entity, live position, etc
 *   OPTIONAL EmitterEntityModule that controls entity-specific settings
 *   OPTIONAL EmitterParticleModule that controls particle-specific settings
 *   OPTIONAL EmitterBlockEntityModule that controls block entity-specific settings
 *   OPTIONAL SubEmitterModule that controls sub-emitters
 *   OPTIONAL OnSpawnAction that can be set to run when a particle is spawned
 *   OPTIONAL OnDeathAction that can be set to run when a particle dies
 *   OPTIONAL OnUpdateAction that can be set to run when a particle is updated
 *   OPTIONAL OnRenderAction that can be set to run when a particle is rendered
 */
public class ParticleEmitter {
    public static final Codec<ParticleEmitter> CODEC = RecordCodecBuilder.create(i ->
            i.group(
                    EmitterModule.CODEC.fieldOf("emitter_module").forGetter(ParticleEmitter::getEmitterModule),
                    ResourceLocation.CODEC.fieldOf("emitter_settings").xmap(
                            EmitterSettingsRegistry::getSettings,
                            EmitterSettingsModule::getRegistryId
                    ).forGetter(ParticleEmitter::getEmitterSettingsModule),
                    ResourceLocation.CODEC.fieldOf("particle_data").xmap(
                            QuasarParticleDataRegistry::getData,
                            QuasarParticleData::getRegistryId
                    ).forGetter(ParticleEmitter::getParticleData)
            ).apply(i, ParticleEmitter::new)
            );
    public ResourceLocation registryName;
    public boolean isComplete = false;
    private boolean active = false;
    EmitterModule emitterModule;
    EmitterSettingsModule emitterSettingsModule;
    float maxYOffset = 0.0f;
    private Level level;
//    private Entity linkedEntity;
    QuasarParticleData data;
    public int particleCount = 0;

    public ParticleEmitter(Level level, EmitterModule emitterModule, EmitterSettingsModule emitterSettingsModule) {
        this.level = level;
        this.emitterModule = emitterModule;
        this.emitterSettingsModule = emitterSettingsModule;
        this.data = new QuasarParticleData(emitterSettingsModule.getEmissionParticleSettings(), true, true);
        this.data.parentEmitter = this;
    }

    public ParticleEmitter(EmitterModule emitterModule, EmitterSettingsModule emitterSettingsModule, QuasarParticleData data) {
        this(null, emitterModule, emitterSettingsModule);
        this.data = data;
        data.setParticleSettings(emitterSettingsModule.getEmissionParticleSettings());
        this.data.parentEmitter = this;
    }

    public ParticleEmitter(Level level, EmitterModule emitterModule, EmitterSettingsModule emitterSettingsModule, QuasarParticleData quasarParticleData) {
        this(level, emitterModule, emitterSettingsModule);
        this.data = quasarParticleData;
        data.setParticleSettings(emitterSettingsModule.getEmissionParticleSettings());
        this.data.parentEmitter = this;
    }

    public ParticleEmitter instance(){
        ParticleEmitter em = new ParticleEmitter(this.level, this.emitterModule.instance(), this.emitterSettingsModule.instance(), this.data.instance());
        em.registryName = this.registryName;
        return em;
    }

    public boolean isActive() {
        return active;
    }

    public void setLevel(Level level) {
        this.level = level;
        this.emitterSettingsModule.getEmissionShapeSettings().setRandomSource(level.random);
    }

    public void setPosition(Vec3 position){
        this.emitterModule.setPosition(position);
        this.emitterSettingsModule.getEmissionShapeSettings().setPosition(position);
    }
    public QuasarParticleData getParticleData() {
        return data;
    }

    public EmitterModule getEmitterModule() {
        return emitterModule;
    }

    public EmitterSettingsModule getEmitterSettingsModule() {
        return emitterSettingsModule;
    }

    public void run() {
        // apply spread

        if (this.emitterModule.getCurrentLifetime() == 0) {
            this.active = true;
        }
        if (level.isClientSide) {
            Vec3 particlePos = emitterSettingsModule.getEmissionShapeSettings().getPos();
            Vec3 particleDirection = emitterSettingsModule.getEmissionParticleSettings().getInitialDirection().scale(emitterSettingsModule.getEmissionParticleSettings().getParticleSpeed());
            QuasarParticleData d2 = data.instance();
            d2.getInitModules().stream().filter(force -> force instanceof InitialVelocityForce).forEach(f -> {
                InitialVelocityForce force = (InitialVelocityForce) f;
                if(force.takesParentRotation()){
                    force.velocityDirection = force.velocityDirection
                            .xRot((float) -Math.toRadians(emitterSettingsModule.getEmissionShapeSettings().getRotation().x))
                            .yRot((float) -Math.toRadians(emitterSettingsModule.getEmissionShapeSettings().getRotation().y))
                            .zRot((float) -Math.toRadians(emitterSettingsModule.getEmissionShapeSettings().getRotation().z));
                    float e = 0;
                }
            });
            level.addParticle(d2, true, particlePos.x(), particlePos.y(), particlePos.z(), particleDirection.x(), particleDirection.y(), particleDirection.z());
        }
    }

    public void tick() {
        emitterModule.tick(() -> {
            if(emitterModule.getCurrentLifetime() % emitterModule.getRate() == 0 || emitterModule.getCurrentLifetime() == 1){
                int i = 0;
                while (i < emitterModule.getCount()){
                    this.run();
                    i++;
                }
            }
        });
        if(emitterModule.isComplete()){
            this.isComplete = true;
        }
    }
}
