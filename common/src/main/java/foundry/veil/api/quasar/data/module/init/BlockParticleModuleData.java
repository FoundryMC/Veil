package foundry.veil.api.quasar.data.module.init;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import foundry.veil.api.client.editor.EditorAttributeProvider;
import foundry.veil.api.quasar.data.ParticleModuleTypeRegistry;
import foundry.veil.api.quasar.data.module.ModuleType;
import foundry.veil.api.quasar.data.module.ParticleModuleData;
import foundry.veil.api.quasar.emitters.module.InitParticleModule;
import foundry.veil.api.quasar.emitters.module.UpdateParticleModule;
import foundry.veil.api.quasar.particle.ParticleModuleSet;
import imgui.ImGui;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockParticleModuleData implements ParticleModuleData, EditorAttributeProvider {

    public static final MapCodec<BlockParticleModuleData> CODEC = Codec.BOOL.optionalFieldOf("dynamic", true).xmap(BlockParticleModuleData::new, BlockParticleModuleData::dynamic);
    private boolean dynamic;

    public BlockParticleModuleData(boolean dynamic) {
        this.dynamic = dynamic;
    }

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
        return ParticleModuleTypeRegistry.BLOCK_PARTICLE;
    }

    @Override
    public void renderImGuiAttributes() {
        if (ImGui.checkbox("dynamic", dynamic)) {
            this.dynamic = !this.dynamic;
        }
    }

    public boolean dynamic() {
        return dynamic;
    }

}
