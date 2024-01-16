package foundry.veil.quasar.emitters.modules.particle.init;

import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.emitters.modules.ModuleType;
import foundry.veil.quasar.util.CodecUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector4f;

import java.util.Arrays;
import java.util.List;

public class InitRandomColorModule implements InitModule {
    public static final Codec<InitRandomColorModule> CODEC = RecordCodecBuilder.create(i ->
            i.group(
                    CodecUtil.VECTOR4F_CODEC.listOf().fieldOf("color").forGetter(InitRandomColorModule::getColor)
            ).apply(i, InitRandomColorModule::new
           )
    );
    Vector4f[] color;

    public InitRandomColorModule(Vector4f... color) {
        this.color = color;
    }
    public Vector4f[] getColors() {
        return color;
    }

    public List<Vector4f> getColor() {
        return Arrays.asList(color);
    }

    public InitRandomColorModule(Vector4f color) {
        this.color = new Vector4f[]{color};
    }

    public InitRandomColorModule(List<Vector4f> color) {
        this.color = color.toArray(new Vector4f[0]);
    }

    @Override
    public void run(QuasarParticle particle) {
        if(particle.getAge() == 0) {
            int index = (int) (Math.random() * color.length);
            Vector4f color = this.color[index];
            particle.setColor(color);
        }
    }

    @NotNull
    @Override
    public ModuleType<?> getType() {
        return ModuleType.INIT_RANDOM_COLOR;
    }

}
