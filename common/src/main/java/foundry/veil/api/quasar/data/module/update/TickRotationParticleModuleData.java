package foundry.veil.api.quasar.data.module.update;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.api.client.editor.EditorAttributeProvider;
import foundry.veil.api.molang.MolangExpressionCodec;
import foundry.veil.api.molang.VeilMolang;
import foundry.veil.api.quasar.data.ParticleModuleTypeRegistry;
import foundry.veil.api.quasar.data.module.ModuleType;
import foundry.veil.api.quasar.data.module.ParticleModuleData;
import foundry.veil.api.quasar.emitters.module.UpdateParticleModule;
import foundry.veil.api.quasar.particle.ParticleModuleSet;
import gg.moonflower.molangcompiler.api.MolangExpression;
import imgui.ImGui;
import imgui.type.ImString;
import net.minecraft.util.Mth;

/**
 * @since 4.5.0
 */
public final class TickRotationParticleModuleData implements ParticleModuleData, EditorAttributeProvider {
    public static final MapCodec<TickRotationParticleModuleData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            MolangExpressionCodec.CODEC.fieldOf("x").forGetter(TickRotationParticleModuleData::rotationX),
            MolangExpressionCodec.CODEC.fieldOf("y").forGetter(TickRotationParticleModuleData::rotationY),
            MolangExpressionCodec.CODEC.fieldOf("z").forGetter(TickRotationParticleModuleData::rotationZ)
    ).apply(instance, TickRotationParticleModuleData::new));
    private MolangExpression rotationX, rotationY, rotationZ;

    public TickRotationParticleModuleData(MolangExpression rotationX,
                                          MolangExpression rotationY,
                                          MolangExpression rotationZ) {
        this.rotationX = rotationX;
        this.rotationY = rotationY;
        this.rotationZ = rotationZ;
    }

    @Override
    public void renderImGuiAttributes() {
        ImString rotationXInput = new ImString();
        String rotationXString = this.rotationX.toString();
        if (rotationXString.startsWith("return (")) {
            rotationXInput.set(rotationXString.substring(8, rotationXString.length() - 1));
        } else {
            rotationXInput.set(rotationXString);
        }

        if (ImGui.inputText("x", rotationXInput)) {
            try {
                this.rotationX = VeilMolang.get().compile(rotationXInput.get());
            } catch (Exception ignored) {
            }
        }

        ImString rotationYInput = new ImString();
        String rotationYString = this.rotationY.toString();
        if (rotationYString.startsWith("return (")) {
            rotationYInput.set(rotationYString.substring(8, rotationYString.length() - 1));
        } else {
            rotationYInput.set(rotationYString);
        }

        if (ImGui.inputText("y", rotationYInput)) {
            try {
                this.rotationY = VeilMolang.get().compile(rotationYInput.get());
            } catch (Exception ignored) {
            }
        }

        ImString rotationZInput = new ImString();
        String rotationZString = this.rotationZ.toString();
        if (rotationZString.startsWith("return (")) {
            rotationZInput.set(rotationZString.substring(8, rotationZString.length() - 1));
        } else {
            rotationZInput.set(rotationZString);
        }

        if (ImGui.inputText("z", rotationZInput)) {
            try {
                this.rotationZ = VeilMolang.get().compile(rotationZInput.get());
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void addModules(ParticleModuleSet.Builder builder) {
        builder.addModule((UpdateParticleModule) particle -> {
            try {
                particle.setRotation(particle.getEnvironment().resolve(this.rotationX) * Mth.DEG_TO_RAD, particle.getEnvironment().resolve(this.rotationY) * Mth.DEG_TO_RAD, particle.getEnvironment().resolve(this.rotationZ) * Mth.DEG_TO_RAD);
            } catch (Exception ignored) {

            }
        });
    }

    @Override
    public ModuleType<?> getType() {
        return ParticleModuleTypeRegistry.TICK_ROTATION;
    }

    public MolangExpression rotationX() {
        return rotationX;
    }

    public MolangExpression rotationY() {
        return rotationY;
    }

    public MolangExpression rotationZ() {
        return rotationZ;
    }
}
