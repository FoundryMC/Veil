package foundry.veil.api.quasar.data.module.render;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.api.client.editor.EditorAttributeProvider;
import foundry.veil.api.molang.MolangExpressionCodec;
import foundry.veil.api.molang.VeilMolang;
import foundry.veil.api.quasar.data.ParticleModuleTypeRegistry;
import foundry.veil.api.quasar.data.module.ModuleType;
import foundry.veil.api.quasar.data.module.ParticleModuleData;
import foundry.veil.api.quasar.emitters.module.InitParticleModule;
import foundry.veil.api.quasar.emitters.module.render.ColorRenderModule;
import foundry.veil.api.quasar.particle.ParticleModuleSet;
import foundry.veil.impl.quasar.ColorGradient;
import gg.moonflower.molangcompiler.api.MolangExpression;
import imgui.ImGui;
import imgui.type.ImString;

public final class ColorParticleModuleData implements ParticleModuleData, EditorAttributeProvider {

    public static final MapCodec<ColorParticleModuleData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ColorGradient.CODEC.fieldOf("gradient").forGetter(ColorParticleModuleData::gradient),
            MolangExpressionCodec.CODEC.fieldOf("interpolant").forGetter(ColorParticleModuleData::interpolant)
    ).apply(instance, ColorParticleModuleData::new));
    private final ColorGradient gradient;
    private MolangExpression interpolant;

    public ColorParticleModuleData(ColorGradient gradient,
                                   MolangExpression interpolant) {
        this.gradient = gradient;
        this.interpolant = interpolant;
    }

    @Override
    public void addModules(ParticleModuleSet.Builder builder) {
        if (this.gradient.isConstant() || this.interpolant.isConstant()) {
            builder.addModule((InitParticleModule) particle -> particle.getRenderData().setColor(this.gradient.getColor(particle.getEnvironment().safeResolve(this.interpolant))));
        } else {
            builder.addModule(new ColorRenderModule(this.gradient, this.interpolant));
        }
    }

    @Override
    public ModuleType<?> getType() {
        return ParticleModuleTypeRegistry.COLOR;
    }

    @Override
    public void renderImGuiAttributes() {
        gradient.renderImGuiAttributes();

        ImGui.separator();

        ImString interpolantInput = new ImString();
        String interpolantString = this.interpolant.toString();
        if (interpolantString.startsWith("return (")) {
            interpolantInput.set(interpolantString.substring(8, interpolantString.length() - 1));
        } else {
            interpolantInput.set(interpolantString);
        }
        if (ImGui.inputText("interpolant", interpolantInput)) {
            try {
                this.interpolant = VeilMolang.get().compile(interpolantInput.get());
            } catch (Exception ignored) {
            }
        }
    }

    public ColorGradient gradient() {
        return gradient;
    }

    public MolangExpression interpolant() {
        return interpolant;
    }
}
