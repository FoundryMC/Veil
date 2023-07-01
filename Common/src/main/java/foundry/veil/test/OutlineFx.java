package foundry.veil.test;

import foundry.veil.postprocessing.DynamicEffectInstance;
import org.joml.Vector3f;

import java.util.function.BiConsumer;

public class OutlineFx extends DynamicEffectInstance {

    public OutlineFx() {
    }

    public OutlineFx(Vector3f center) {
        this();
    }

    @Override
    public void writeDataToBuffer(BiConsumer<Integer, Float> writer) {
    }
}
