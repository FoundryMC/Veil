package foundry.veil.api.quasar.data.module.update;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.api.client.editor.EditorAttributeProvider;
import foundry.veil.api.quasar.data.ParticleModuleTypeRegistry;
import foundry.veil.api.quasar.data.QuasarParticles;
import foundry.veil.api.quasar.data.module.ModuleType;
import foundry.veil.api.quasar.data.module.ParticleModuleData;
import foundry.veil.api.quasar.emitters.module.update.TickSubEmitterModule;
import foundry.veil.api.quasar.particle.ParticleModuleSet;
import imgui.ImGui;
import imgui.type.ImString;
import net.minecraft.resources.ResourceLocation;

public final class TickSubEmitterModuleData implements ParticleModuleData, EditorAttributeProvider {

    public static final MapCodec<TickSubEmitterModuleData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("subemitter").forGetter(TickSubEmitterModuleData::subEmitter),
            Codec.INT.fieldOf("frequency").forGetter(TickSubEmitterModuleData::frequency)
    ).apply(instance, TickSubEmitterModuleData::new));
    private ResourceLocation subEmitter;
    private int frequency;

    public TickSubEmitterModuleData(ResourceLocation subEmitter, int frequency) {
        this.subEmitter = subEmitter;
        this.frequency = frequency;
    }

    @Override
    public void addModules(ParticleModuleSet.Builder builder) {
        builder.addModule(new TickSubEmitterModule(this));
    }

    @Override
    public ModuleType<?> getType() {
        return ParticleModuleTypeRegistry.TICK_SUB_EMITTER;
    }

    @Override
    public void renderImGuiAttributes() {
        ImString textInput = new ImString(subEmitter.toString(), 256);

        if (ImGui.inputTextWithHint("subemitter", "namespace:path", textInput)) {
            try {
                ResourceLocation location = ResourceLocation.parse(textInput.get());
                QuasarParticles.registryAccess().registry(QuasarParticles.EMITTER).map(registry -> registry.get(location))
                        .ifPresent(data -> this.subEmitter = location);
            } catch (Exception ignored) {}
        }

        int[] editFrequency = new int[]{frequency};
        if (ImGui.dragInt("frequency", editFrequency, 0.01F, 1, Integer.MAX_VALUE)) frequency = Math.max(editFrequency[0], 1);
    }

    public ResourceLocation subEmitter() {
        return subEmitter;
    }

    public int frequency() {
        return frequency;
    }
}
