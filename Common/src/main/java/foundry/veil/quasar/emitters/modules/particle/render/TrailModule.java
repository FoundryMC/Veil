package foundry.veil.quasar.emitters.modules.particle.render;

import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.emitters.modules.ModuleType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import imgui.ImGui;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A module that adds trails to a particle.
 * @see TrailSettings
 * @see RenderModule
 * @see foundry.veil.quasar.emitters.modules.particle.render.RenderData
 * WARNING: Trails add a lot of time to the rendering process, so use them sparingly.
 */
public class TrailModule implements RenderModule {
    public static final Codec<TrailModule> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    TrailSettings.CODEC.listOf().fieldOf("settings").forGetter(module -> module.settings)
            ).apply(instance, TrailModule::new));
    List<TrailSettings> settings;

    public TrailModule(TrailSettings... settings) {
        this.settings = List.of(settings);
    }

    public TrailModule(List<TrailSettings> settings) {
        this.settings = settings;
    }
    @Override
    public void apply(QuasarParticle particle, float partialTicks, RenderData data) {
        List<TrailSettings> settings = data.getTrails();
        settings.addAll(this.settings);
        data.addTrails(settings);
    }

    @NotNull
    @Override
    public ModuleType<?> getType() {
        return ModuleType.TRAIL;
    }

    @Override
    public void renderImGuiSettings() {
        if(ImGui.collapsingHeader("Trail Settings #" + this.hashCode())){
            ImGui.treePush("Trail Settings" + this.hashCode());
            for (TrailSettings settings : this.settings) {
                if(ImGui.treeNode("Trail Settings " + this.settings.indexOf(settings) + "#" + settings.hashCode())){
                    settings.renderImGuiSettings();
                    ImGui.treePop();
                }
            }
            ImGui.treePop();
        }
    }
}
