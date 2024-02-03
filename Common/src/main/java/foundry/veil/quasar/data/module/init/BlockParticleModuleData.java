package foundry.veil.quasar.data.module.init;

import com.mojang.serialization.Codec;
import foundry.veil.quasar.client.particle.ParticleModuleSet;
import foundry.veil.quasar.data.module.ModuleType;
import foundry.veil.quasar.data.module.ParticleModuleData;
import foundry.veil.quasar.emitters.modules.particle.InitParticleModule;
import foundry.veil.quasar.emitters.modules.particle.UpdateParticleModule;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.state.BlockState;

public record BlockParticleModuleData(boolean dynamic) implements ParticleModuleData {

    public static final Codec<BlockParticleModuleData> CODEC = Codec.BOOL.optionalFieldOf("dynamic", true).xmap(BlockParticleModuleData::new, BlockParticleModuleData::dynamic).codec();

    @Override
    public void addModules(ParticleModuleSet.Builder builder) {
        builder.addModule((InitParticleModule) particle -> {
            BlockState state = particle.getBlockStateInOrUnder();
            if (!state.isAir()) {
                particle.getRenderData().setAtlasSprite(Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(state));
            }
        });
        if (this.dynamic) {
            builder.addModule((UpdateParticleModule) particle -> {
                BlockState state = particle.getBlockStateInOrUnder();
                if (!state.isAir()) {
                    particle.getRenderData().setAtlasSprite(Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(state));
                }
            });
        }
    }

    @Override
    public ModuleType<?> getType() {
        return ModuleType.BLOCK_PARTICLE;
    }
}
