package foundry.veil.quasar.emitters.modules.particle.init;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.quasar.client.particle.QuasarVanillaParticle;
import foundry.veil.quasar.data.module.ModuleType;
import foundry.veil.quasar.util.CodecUtil;
import org.joml.Vector4fc;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class InitRandomColorParticleModule implements InitParticleModule {

    public static final Codec<InitRandomColorParticleModule> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    CodecUtil.VECTOR4F_CODEC.listOf().optionalFieldOf("color", Collections.emptyList()).forGetter(module -> Arrays.asList(module.getColors()))
            ).apply(instance, InitRandomColorParticleModule::new)
    );

    private final Vector4fc[] color;

    public InitRandomColorParticleModule(Vector4fc... color) {
        this.color = color;
    }

    public InitRandomColorParticleModule(Collection<Vector4fc> color) {
        this.color = color.toArray(new Vector4fc[0]);
    }

    public Vector4fc[] getColors() {
        return this.color;
    }

    @Override
    public void run(QuasarVanillaParticle particle) {
        if (particle.getAge() == 0) {
            int index = (int) (Math.random() * this.color.length);
            Vector4fc color = this.color[index];
            particle.setColor(color);
        }
    }

    @Override
    public ModuleType<?> getType() {
        return ModuleType.INIT_RANDOM_COLOR;
    }

}
