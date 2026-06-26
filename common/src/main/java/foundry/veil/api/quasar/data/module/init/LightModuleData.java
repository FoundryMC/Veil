package foundry.veil.api.quasar.data.module.init;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.api.client.editor.EditorAttributeProvider;
import foundry.veil.api.molang.MolangExpressionCodec;
import foundry.veil.api.molang.VeilMolang;
import foundry.veil.api.quasar.data.ParticleModuleTypeRegistry;
import foundry.veil.api.quasar.data.module.ModuleType;
import foundry.veil.api.quasar.data.module.ParticleModuleData;
import foundry.veil.api.quasar.emitters.module.render.DynamicLightModule;
import foundry.veil.api.quasar.emitters.module.render.StaticLightModule;
import foundry.veil.api.quasar.particle.ParticleModuleSet;
import foundry.veil.impl.quasar.ColorGradient;
import gg.moonflower.molangcompiler.api.MolangExpression;
import imgui.ImGui;
import imgui.type.ImString;

public final class LightModuleData implements ParticleModuleData, EditorAttributeProvider {

    public static final MapCodec<LightModuleData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ColorGradient.CODEC.fieldOf("gradient").forGetter(LightModuleData::color),
            MolangExpressionCodec.CODEC.fieldOf("brightness").forGetter(LightModuleData::brightness),
            MolangExpressionCodec.CODEC.fieldOf("radius").forGetter(LightModuleData::radius)
    ).apply(instance, LightModuleData::new));
    private final ColorGradient color;
    private MolangExpression brightness;
    private MolangExpression radius;

    public LightModuleData(ColorGradient color,
                           MolangExpression brightness,
                           MolangExpression radius) {
        this.color = color;
        this.brightness = brightness;
        this.radius = radius;
    }

    @Override
    public void addModules(ParticleModuleSet.Builder builder) {
        if (this.color.isConstant() && this.brightness.isConstant() && this.radius.isConstant()) {
            StaticLightModule module = new StaticLightModule(this);
            if (module.isVisible()) {
                builder.addModule(module);
            }
        } else {
            builder.addModule(new DynamicLightModule(this));
        }
    }

    @Override
    public ModuleType<?> getType() {
        return ParticleModuleTypeRegistry.LIGHT;
    }

    @Override
    public void renderImGuiAttributes() {
        color.renderImGuiAttributes();

        ImGui.separator();

        ImString brightnessInput = new ImString();
        ImString radiusInput = new ImString();

        String brightnessString = this.brightness.toString();
        if (brightnessString.startsWith("return (")) {
            brightnessInput.set(brightnessString.substring(8, brightnessString.length() - 1));
        } else {
            brightnessInput.set(brightnessString);
        }

        String radiusString = this.radius.toString();
        if (radiusString.startsWith("return (")) {
            radiusInput.set(radiusString.substring(8, radiusString.length() - 1));
        } else {
            radiusInput.set(radiusString);
        }

        if (ImGui.inputText("brightness", brightnessInput)) {
            try {
                this.brightness = VeilMolang.get().compile(brightnessInput.get());
            } catch (Exception ignored) {
            }
        }

        if (ImGui.inputText("radius", radiusInput)) {
            try {
                this.radius = VeilMolang.get().compile(radiusInput.get());
            } catch (Exception ignored) {
            }
        }
    }

    public ColorGradient color() {
        return color;
    }

    public MolangExpression brightness() {
        return brightness;
    }

    public MolangExpression radius() {
        return radius;
    }
}
