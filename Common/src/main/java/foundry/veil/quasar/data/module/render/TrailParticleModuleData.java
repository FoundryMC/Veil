package foundry.veil.quasar.data.module.render;

import com.mojang.serialization.Codec;
import foundry.veil.quasar.client.particle.ParticleModuleSet;
import foundry.veil.quasar.data.module.ModuleType;
import foundry.veil.quasar.data.module.ParticleModuleData;
import foundry.veil.quasar.emitters.modules.particle.RenderParticleModule;
import foundry.veil.quasar.emitters.modules.particle.render.RenderData;
import foundry.veil.quasar.emitters.modules.particle.render.TrailParticleModule;
import foundry.veil.quasar.emitters.modules.particle.render.TrailSettings;

import java.util.List;

/**
 * A module that adds trails to a particle.
 *
 * @see TrailSettings
 * @see RenderParticleModule
 * @see RenderData
 * WARNING: Trails add a lot of time to the rendering process, so use them sparingly.
 */
public record TrailParticleModuleData(List<TrailSettings> settings) implements ParticleModuleData {

    public static final Codec<TrailParticleModuleData> CODEC = TrailSettings.CODEC.listOf()
            .fieldOf("settings")
            .xmap(TrailParticleModuleData::new, TrailParticleModuleData::settings)
            .codec();

    @Override
    public void addModules(ParticleModuleSet.Builder builder) {
        builder.addModule(new TrailParticleModule(this.settings));
    }

    @Override
    public ModuleType<?> getType() {
        return ModuleType.TRAIL;
    }
}
