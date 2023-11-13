package foundry.veil.quasar.emitters.modules.particle.update;

import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.emitters.ParticleContext;
import foundry.veil.quasar.emitters.ParticleEmitter;
import foundry.veil.quasar.emitters.ParticleEmitterRegistry;
import foundry.veil.quasar.emitters.ParticleSystemManager;
import foundry.veil.quasar.emitters.modules.ModuleType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import imgui.ImGui;
import imgui.type.ImInt;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class TickSubEmitter implements UpdateModule {
    public static final Codec<TickSubEmitter> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("subemitter").forGetter(TickSubEmitter::getSubEmitter),
                    Codec.INT.fieldOf("frequency").forGetter(TickSubEmitter::getFrequency)
            ).apply(instance, TickSubEmitter::new));
    ResourceLocation subEmitter;
    int frequency;

    public TickSubEmitter(ResourceLocation subEmitter, int frequency) {
        this.subEmitter = subEmitter;
        this.frequency = frequency;
    }

    public ResourceLocation getSubEmitter() {
        return subEmitter;
    }

    public int getFrequency() {
        return frequency;
    }
    @Override
    public void run(QuasarParticle particle) {
        if(particle.getAge() % frequency != 0) return;
        ParticleContext context = particle.getContext();
        ParticleEmitter emitter = ParticleEmitterRegistry.getEmitter(subEmitter).instance();
        if(emitter == null) return;
        emitter.setPosition(context.particle.getPos());
        emitter.setLevel(context.particle.getLevel());
        emitter.getEmitterSettingsModule().getEmissionShapeSettings().setRandomSource(context.particle.getLevel().random);
        emitter.getEmitterSettingsModule().getEmissionShapeSettings().setPosition(context.particle.getPos());
        ParticleSystemManager.getInstance().addDelayedParticleSystem(emitter);
    }

    @NotNull
    @Override
    public ModuleType<?> getType() {
        return ModuleType.TICK_SUB_EMITTER;
    }

    @Override
    public void renderImGuiSettings() {
        if(ImGui.beginCombo("SubEmitter", subEmitter.toString())){
            for(ResourceLocation location : ParticleEmitterRegistry.getEmitterNames()){
                if(ImGui.selectable(location.toString(), location.equals(subEmitter))){
                    subEmitter = location;
                }
            }
            ImGui.endCombo();
        }
        ImInt frequency = new ImInt(this.frequency);
        ImGui.inputInt("Frequency" + this.hashCode(), frequency);
    }
}
