package foundry.veil.quasar.emitters.modules.particle.init;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.deferred.light.Light;
import foundry.veil.api.client.render.deferred.light.PointLight;
import foundry.veil.mixin.debug.MinecraftMixin;
import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.emitters.modules.ModuleType;
import foundry.veil.quasar.util.ColorGradient;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Vector4f;

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
    public void run(QuasarParticle particle) {
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
