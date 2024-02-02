package foundry.veil.quasar.client.particle;

import foundry.veil.quasar.emitters.modules.ParticleModule;
import foundry.veil.quasar.emitters.modules.particle.init.InitParticleModule;
import foundry.veil.quasar.emitters.modules.particle.render.RenderParticleModule;
import foundry.veil.quasar.emitters.modules.particle.update.UpdateParticleModule;

import java.util.HashSet;
import java.util.Set;

public class ParticleModuleSet {

    private final ParticleModule[] modules;
    private final InitParticleModule[] initModules;
    private final UpdateParticleModule[] updateModules;
    private final RenderParticleModule[] renderModules;

    private ParticleModuleSet(ParticleModule[] modules, InitParticleModule[] initModules, UpdateParticleModule[] updateModules, RenderParticleModule[] renderModules) {
        this.modules = modules;
        this.initModules = initModules;
        this.updateModules = updateModules;
        this.renderModules = renderModules;
    }

    public ParticleModule[] getAllModules() {
        return this.modules;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final Set<ParticleModule> modules;
        private final Set<InitParticleModule> initModules;
        private final Set<UpdateParticleModule> updateModules;
        private final Set<RenderParticleModule> renderModules;

        public Builder() {
            this.modules = new HashSet<>();
            this.initModules = new HashSet<>();
            this.updateModules = new HashSet<>();
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
            if (module instanceof RenderParticleModule renderModule) {
                this.renderModules.add(renderModule);
            }
        }

        public ParticleModuleSet build() {
            ParticleModule[] modules = this.modules.toArray(ParticleModule[]::new);
            InitParticleModule[] initModules = this.initModules.toArray(InitParticleModule[]::new);
            UpdateParticleModule[] updateModules = this.updateModules.toArray(UpdateParticleModule[]::new);
            RenderParticleModule[] renderModules = this.renderModules.toArray(RenderParticleModule[]::new);
            return new ParticleModuleSet(modules, initModules, updateModules, renderModules);
        }
    }
}
