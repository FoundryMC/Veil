package foundry.veil.test;

import com.mojang.math.Vector3f;
import foundry.veil.color.Color;
import foundry.veil.postprocessing.DynamicEffectInstance;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class BloomFx extends DynamicEffectInstance {
    Supplier<Float> intensity;
    Supplier<Float> blur;
    public BloomFx(){
    }

    public BloomFx(Supplier<Float> intensity, Supplier<Float> blur) {
        this();
        this.intensity = intensity;
        this.blur = blur;
    }

    @Override
    public void writeDataToBuffer(BiConsumer<Integer, Float> writer) {
        writer.accept(0, intensity.get());
        writer.accept(1, blur.get());
    }
}
