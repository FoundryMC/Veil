package foundry.veil.quasar.emitters.modules.particle.render;

import foundry.veil.quasar.client.particle.QuasarVanillaParticle;
import foundry.veil.quasar.emitters.modules.ModuleType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A module that adds trails to a particle.
 * @see TrailSettings
 * @see RenderParticleModule
 * @see foundry.veil.quasar.emitters.modules.particle.render.RenderData
 * WARNING: Trails add a lot of time to the rendering process, so use them sparingly.
 */
public class TrailParticleModule implements RenderParticleModule {
    public static final Codec<TrailParticleModule> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    TrailSettings.CODEC.listOf().fieldOf("settings").forGetter(module -> module.settings)
            ).apply(instance, TrailParticleModule::new));
    List<TrailSettings> settings;

    public TrailParticleModule(TrailSettings... settings) {
        this.settings = List.of(settings);
    }

    public TrailParticleModule(List<TrailSettings> settings) {
        this.settings = settings;
    }
    @Override
    public void apply(QuasarVanillaParticle particle, float partialTicks, RenderData data) {
        List<TrailSettings> settings = data.getTrails();
        settings.addAll(this.settings);
        data.addTrails(settings);
    }

    @NotNull
    @Override
    public ModuleType<?> getType() {
        return ModuleType.TRAIL;
    }

}
