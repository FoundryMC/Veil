package foundry.veil.test;

import com.mojang.math.Vector3f;
import foundry.veil.color.Color;
import foundry.veil.postprocessing.DynamicEffectInstance;

import java.util.function.BiConsumer;

public class BloomFx extends DynamicEffectInstance {
    public BloomFx(){
    }

    public BloomFx(Vector3f center) {
        this();
    }

    @Override
    public void writeDataToBuffer(BiConsumer<Integer, Float> writer) {
    }
}
