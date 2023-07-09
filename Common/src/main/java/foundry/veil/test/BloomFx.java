//package foundry.veil.test;
//
//import com.mojang.math.Vector3f;
//import foundry.veil.color.Color;
//import foundry.veil.postprocessing.DynamicEffectInstance;
//
//import java.util.List;
//import java.util.function.BiConsumer;
//import java.util.function.Supplier;
//
//public class BloomFx extends DynamicEffectInstance {
//    List<Supplier<Float>> data;
//    public BloomFx(){
//    }
//
//    public BloomFx(List<Supplier<Float>> data) {
//        this();
//        this.data = data;
//    }
//
//    @Override
//    public void writeDataToBuffer(BiConsumer<Integer, Float> writer) {
//        for (int i = 0; i < data.size(); i++) {
//            writer.accept(i, data.get(i).get());
//        }
//    }
//}
