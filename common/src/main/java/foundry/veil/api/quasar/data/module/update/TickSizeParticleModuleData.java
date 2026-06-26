package foundry.veil.api.quasar.data.module.update;

import com.mojang.serialization.MapCodec;
import foundry.veil.api.client.editor.EditorAttributeProvider;
import foundry.veil.api.molang.MolangExpressionCodec;
import foundry.veil.api.molang.VeilMolang;
import foundry.veil.api.quasar.data.ParticleModuleTypeRegistry;
import foundry.veil.api.quasar.data.module.ModuleType;
import foundry.veil.api.quasar.data.module.ParticleModuleData;
import foundry.veil.api.quasar.emitters.module.InitParticleModule;
import foundry.veil.api.quasar.emitters.module.UpdateParticleModule;
import foundry.veil.api.quasar.particle.ParticleModuleSet;
import gg.moonflower.molangcompiler.api.MolangExpression;
import gg.moonflower.molangcompiler.api.exception.MolangRuntimeException;
import imgui.ImGui;
import imgui.type.ImString;

public final class TickSizeParticleModuleData implements ParticleModuleData, EditorAttributeProvider {

    public static final MapCodec<TickSizeParticleModuleData> CODEC = MolangExpressionCodec.CODEC.fieldOf("size").xmap(TickSizeParticleModuleData::new, TickSizeParticleModuleData::size);
    private MolangExpression size;

    public TickSizeParticleModuleData(MolangExpression size) {
        this.size = size;
    }

    @Override
    public void addModules(ParticleModuleSet.Builder builder) {
        builder.addModule((InitParticleModule) particle -> {
            try {
                particle.setRadius(particle.getEnvironment().resolve(this.size));
            } catch (MolangRuntimeException e) {
                e.printStackTrace();
                particle.setRadius(1.0F);
            }
        });
        if (!this.size.isConstant()) {
            builder.addModule((UpdateParticleModule) particle -> {
                try {
                    particle.setRadius(particle.getEnvironment().resolve(this.size));
                } catch (MolangRuntimeException e) {
                    e.printStackTrace();
                    particle.setRadius(1.0F);
                }
            });
        }
    }

    @Override
    public ModuleType<?> getType() {
        return ParticleModuleTypeRegistry.TICK_SIZE;
    }

    @Override
    public void renderImGuiAttributes() {
        ImString textInput = new ImString();
        String sizeText = this.size.toString();
        if (sizeText.startsWith("return (")) {
            textInput.set(size.toString().substring(8, this.size.toString().length() - 1));
        } else {
            textInput.set(size.toString());
        }
        if (ImGui.inputText("size", textInput)) {
            try {
                this.size = VeilMolang.get().compile(textInput.get());
            } catch (Exception ignored) {
            }
        }
    }

    public MolangExpression size() {
        return size;
    }

}
