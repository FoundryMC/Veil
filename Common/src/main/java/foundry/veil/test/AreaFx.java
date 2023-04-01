package foundry.veil.test;

import com.mojang.math.Vector3f;
import foundry.veil.Veil;
import foundry.veil.postprocessing.DynamicEffectInstance;
import foundry.veil.postprocessing.PostProcessor;

import java.util.function.BiConsumer;

public class AreaFx extends DynamicEffectInstance {
    public Vector3f position;

    public AreaFx(Vector3f position) {
        this.position = position;
        PostProcessor.TEXTURE_UNIFORMS.get(Veil.veilPath("area")).stream().toList().forEach(s->s.getSecond().initialize());
    }
    @Override
    public void writeDataToBuffer(BiConsumer<Integer, Float> writer) {
        writer.accept(0, position.x());
        writer.accept(1, position.y());
        writer.accept(2, position.z());
    }
}
