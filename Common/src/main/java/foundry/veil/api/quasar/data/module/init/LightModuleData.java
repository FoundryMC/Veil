package foundry.veil.api.quasar.data.module.init;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.api.quasar.data.module.ModuleType;
import foundry.veil.api.quasar.data.module.ParticleModuleData;
import foundry.veil.api.quasar.emitters.module.render.DynamicColorLightModule;
import foundry.veil.api.quasar.emitters.module.render.StaticColorLightModule;
import foundry.veil.api.quasar.particle.ParticleModuleSet;
import foundry.veil.impl.quasar.ColorGradient;

public record LightModuleData(ColorGradient color,
                              float brightness,
                              float radius) implements ParticleModuleData {

    public static final Codec<LightModuleData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ColorGradient.CODEC.fieldOf("gradient").forGetter(LightModuleData::color),
            Codec.FLOAT.fieldOf("brightness").forGetter(LightModuleData::brightness),
            Codec.FLOAT.fieldOf("radius").forGetter(LightModuleData::radius)
    ).apply(instance, LightModuleData::new));

    @Override
    public void addModules(ParticleModuleSet.Builder builder) {
        if (this.color.isConstant()) {
            StaticColorLightModule module = new StaticColorLightModule(this);
            if (module.isVisible()) {
                builder.addModule(module);
            }
        } else {
            builder.addModule(new DynamicColorLightModule(this));
        }
    }

    @Override
    public ModuleType<?> getType() {
        return ModuleType.LIGHT;
    }
}
