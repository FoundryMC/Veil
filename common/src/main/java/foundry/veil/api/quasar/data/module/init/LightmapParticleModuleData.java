package foundry.veil.api.quasar.data.module.init;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.api.client.editor.EditorAttributeProvider;
import foundry.veil.api.quasar.data.ParticleModuleTypeRegistry;
import foundry.veil.api.quasar.data.module.ModuleType;
import foundry.veil.api.quasar.data.module.ParticleModuleData;
import foundry.veil.api.quasar.emitters.module.InitParticleModule;
import foundry.veil.api.quasar.particle.ParticleModuleSet;
import imgui.ImGui;
import net.minecraft.client.renderer.LightTexture;

public final class LightmapParticleModuleData implements ParticleModuleData, EditorAttributeProvider {

    public static final MapCodec<LightmapParticleModuleData> CODEC = Codec.mapEither(
            Codec.BOOL.optionalFieldOf("fullbright", false)
                    .xmap(bright -> bright ? LightTexture.FULL_BRIGHT : -1, packedLight -> packedLight == LightTexture.FULL_BRIGHT),
            RecordCodecBuilder.<Integer>mapCodec(instance -> instance.group(
                    Codec.intRange(0, 15)
                            .fieldOf("block")
                            .forGetter(LightTexture::block),
                    Codec.intRange(0, 15)
                            .fieldOf("sky")
                            .forGetter(LightTexture::sky)
            ).apply(instance, LightTexture::pack))
    ).xmap(either -> either.map(LightmapParticleModuleData::new, LightmapParticleModuleData::new), module -> {
        int packedLight = module.packedLight();
        if (packedLight == -1 || packedLight == LightTexture.FULL_BRIGHT) {
            return Either.left(packedLight);
        }
        return Either.right(packedLight);
    });
    private int packedLight;

    public LightmapParticleModuleData(int packedLight) {
        this.packedLight = packedLight;
    }

    @Override
    public void addModules(ParticleModuleSet.Builder builder) {
        if (this.packedLight != -1) {
            builder.addModule((InitParticleModule) particle -> particle.getRenderData().setFixedPackedLight(this.packedLight));
        }
    }

    @Override
    public ModuleType<?> getType() {
        return ParticleModuleTypeRegistry.LIGHTMAP;
    }

    @Override
    public void renderImGuiAttributes() {
        int[] editBlockLight = new int[]{LightTexture.block(packedLight)};
        int[] editSkyLight = new int[]{LightTexture.sky(packedLight)};

        boolean blockLightDirty = ImGui.dragScalar("block", editBlockLight, 0.01f, 0, 15);
        boolean skyLightDirty = ImGui.dragScalar("sky", editSkyLight, 0.01f, 0, 15);

        if (blockLightDirty || skyLightDirty) {
            packedLight = LightTexture.pack(editBlockLight[0], editSkyLight[0]);
        }
    }

    public int packedLight() {
        return packedLight;
    }
}
