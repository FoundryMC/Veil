package foundry.veil.quasar.emitters.modules.particle.init;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.quasar.client.particle.QuasarVanillaParticle;
import foundry.veil.quasar.emitters.modules.ModuleType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class BlockParticleModule implements InitParticleModule {
    public static final Codec<BlockParticleModule> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.BOOL.optionalFieldOf("dynamic", true).forGetter(module -> module.dynamic)
            ).apply(instance, BlockParticleModule::new));

    boolean dynamic = true;

    public BlockParticleModule(boolean dynamic) {
        this.dynamic = dynamic;
    }

    @NotNull
    @Override
    public ModuleType<?> getType() {
        return ModuleType.BLOCK_PARTICLE;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    @Override
    public void run(QuasarVanillaParticle particle) {
        BlockState state = particle.getContext().getBlockstateInOrUnder();
        if(state != null && !state.isAir()) {
            particle.sprite = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(state);
        }
    }
    @Override
    public InitParticleModule copy() {
        return new BlockParticleModule(dynamic);
    }
}
