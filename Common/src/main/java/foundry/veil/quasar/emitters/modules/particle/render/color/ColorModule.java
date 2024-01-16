package foundry.veil.quasar.emitters.modules.particle.render.color;

import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.emitters.modules.ModuleType;
import foundry.veil.quasar.emitters.modules.particle.render.RenderData;
import foundry.veil.quasar.emitters.modules.particle.render.RenderModule;
import foundry.veil.quasar.util.CodecUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import imgui.ImGui;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector4f;

public class ColorModule implements RenderModule {
    public static final Codec<ColorModule> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    CodecUtil.VECTOR4F_CODEC.fieldOf("color").forGetter(ColorModule::getColor)
            ).apply(instance, ColorModule::new));
    Vector4f color;
    public Vector4f getColor() {
        return color;
    }

    public ColorModule(Vector4f color) {
        this.color = color;
    }
    @Override
    public void apply(QuasarParticle particle, float partialTicks, RenderData data) {
        data.setRGBA(color.x(), color.y(), color.z(), color.w());
    }

    @NotNull
    @Override
    public ModuleType<?> getType() {
        return ModuleType.COLOR;
    }

}
