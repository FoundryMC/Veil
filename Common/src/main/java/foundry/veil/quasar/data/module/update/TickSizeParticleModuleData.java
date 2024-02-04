package foundry.veil.quasar.data.module.update;

import com.mojang.serialization.Codec;
import foundry.veil.api.molang.MolangExpressionCodec;
import foundry.veil.quasar.client.particle.ParticleModuleSet;
import foundry.veil.quasar.data.module.ModuleType;
import foundry.veil.quasar.data.module.ParticleModuleData;
import foundry.veil.quasar.emitters.modules.particle.InitParticleModule;
import foundry.veil.quasar.emitters.modules.particle.UpdateParticleModule;
import gg.moonflower.molangcompiler.api.MolangExpression;
import gg.moonflower.molangcompiler.api.exception.MolangRuntimeException;

public record TickSizeParticleModuleData(MolangExpression size) implements ParticleModuleData {

    public static final Codec<TickSizeParticleModuleData> CODEC = MolangExpressionCodec.CODEC.fieldOf("size").xmap(TickSizeParticleModuleData::new, TickSizeParticleModuleData::size).codec();

    @Override
    public void addModules(ParticleModuleSet.Builder builder) {
        builder.addModule((UpdateParticleModule) particle -> {
            try {
                particle.setScale(particle.getEnvironment().resolve(this.size));
            } catch (MolangRuntimeException e) {
                e.printStackTrace();
                particle.setScale(1.0F);
            }
        });
    }

    @Override
    public ModuleType<?> getType() {
        return ModuleType.TICK_SIZE;
    }
}
