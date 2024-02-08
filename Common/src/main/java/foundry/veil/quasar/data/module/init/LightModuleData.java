package foundry.veil.quasar.data.module.init;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.quasar.client.particle.ParticleModuleSet;
import foundry.veil.quasar.data.module.ModuleType;
import foundry.veil.quasar.data.module.ParticleModuleData;
import foundry.veil.quasar.emitters.modules.particle.render.DynamicColorLightModule;
import foundry.veil.quasar.emitters.modules.particle.render.StaticColorLightModule;
import foundry.veil.quasar.util.ColorGradient;

public record LightModuleData(ColorGradient color,
                              float brightness,
                              float falloff,
                              float radius) implements ParticleModuleData {

    public static final Codec<LightModuleData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ColorGradient.CODEC.fieldOf("gradient").forGetter(LightModuleData::color),
            Codec.FLOAT.fieldOf("brightness").forGetter(LightModuleData::brightness),
            Codec.FLOAT.fieldOf("falloff").forGetter(LightModuleData::falloff),
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
