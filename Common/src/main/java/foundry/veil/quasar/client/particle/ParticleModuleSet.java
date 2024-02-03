package foundry.veil.quasar.client.particle;

import foundry.veil.quasar.emitters.modules.ParticleModule;
import foundry.veil.quasar.emitters.modules.particle.*;

import java.util.HashSet;
import java.util.Set;

public class ParticleModuleSet {

    private final ParticleModule[] modules;
    private final InitParticleModule[] initModules;
    private final UpdateParticleModule[] updateModules;
    private final ForceParticleModule[] forceModules;
    private final CollisionParticleModule[] collisionModules;
    private final RenderParticleModule[] renderModules;

    private ParticleModuleSet(ParticleModule[] modules, InitParticleModule[] initModules, UpdateParticleModule[] updateModules, ForceParticleModule[] forceModules, CollisionParticleModule[] collisionModules, RenderParticleModule[] renderModules) {
        this.modules = modules;
        this.initModules = initModules;
        this.updateModules = updateModules;
        this.forceModules = forceModules;
        this.collisionModules = collisionModules;
        this.renderModules = renderModules;
    }

    public void collide(QuasarParticle particle) {
        for (CollisionParticleModule collisionParticle : this.collisionModules) {
            collisionParticle.collide(particle);
        }
    }

    public ParticleModule[] getAllModules() {
        return this.modules;
    }

    public InitParticleModule[] getInitModules() {
        return this.initModules;
    }

    public UpdateParticleModule[] getUpdateModules() {
        return this.updateModules;
    }

    public ForceParticleModule[] getForceModules() {
        return this.forceModules;
    }

    public CollisionParticleModule[] getCollisionModules() {
        return this.collisionModules;
    }

    public RenderParticleModule[] getRenderModules() {
        return this.renderModules;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final Set<ParticleModule> modules;
        private final Set<InitParticleModule> initModules;
        private final Set<UpdateParticleModule> updateModules;
        private final Set<ForceParticleModule> forceModules;
        private final Set<CollisionParticleModule> collisionModules;
        private final Set<RenderParticleModule> renderModules;

        public Builder() {
            this.modules = new HashSet<>();
            this.initModules = new HashSet<>();
            this.updateModules = new HashSet<>();
            this.forceModules = new HashSet<>();
            this.collisionModules = new HashSet<>();
            this.renderModules = new HashSet<>();
        }

        public void addModule(ParticleModule module) {
            if (!this.modules.add(module)) {
                throw new IllegalArgumentException("Duplicate module: " + module.getClass());
            }
            if (module instanceof InitParticleModule initModule) {
                this.initModules.add(initModule);
            }
            if (module instanceof UpdateParticleModule updateModule) {
                this.updateModules.add(updateModule);
            }
            if (module instanceof ForceParticleModule forceModule) {
                this.forceModules.add(forceModule);
            }
            if (module instanceof CollisionParticleModule collisionModule) {
                this.collisionModules.add(collisionModule);
            }
            if (module instanceof RenderParticleModule renderModule) {
                this.renderModules.add(renderModule);
            }
        }

        public ParticleModuleSet build() {
            ParticleModule[] modules = this.modules.toArray(ParticleModule[]::new);
            InitParticleModule[] initModules = this.initModules.toArray(InitParticleModule[]::new);
            UpdateParticleModule[] updateModules = this.updateModules.toArray(UpdateParticleModule[]::new);
            ForceParticleModule[] forceModules = this.forceModules.toArray(ForceParticleModule[]::new);
            CollisionParticleModule[] collisionModules = this.collisionModules.toArray(CollisionParticleModule[]::new);
            RenderParticleModule[] renderModules = this.renderModules.toArray(RenderParticleModule[]::new);
            return new ParticleModuleSet(modules, initModules, updateModules, forceModules, collisionModules, renderModules);
        }
    }
}
