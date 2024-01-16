package foundry.veil.quasar.emitters.modules.particle.init;

import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.emitters.modules.ModuleType;
import foundry.veil.quasar.util.CodecUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import imgui.ImGui;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector4f;

public class InitColorModule implements InitModule {
    public static final Codec<InitColorModule> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    CodecUtil.VECTOR4F_CODEC.fieldOf("color").forGetter(InitColorModule::getColor)
            ).apply(instance, InitColorModule::new));
    Vector4f color;

    public Vector4f getColor() {
        return color;
    }

    public InitColorModule(Vector4f color) {
        this.color = color;
    }

    @Override
    public void run(QuasarParticle particle) {
        particle.setColor(color);
    }

    @NotNull
    @Override
    public ModuleType<?> getType() {
        return ModuleType.INIT_COLOR;
    }

}
