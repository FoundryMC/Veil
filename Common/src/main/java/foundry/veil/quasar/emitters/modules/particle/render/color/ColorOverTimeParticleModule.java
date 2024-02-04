//package foundry.veil.quasar.emitters.modules.particle.render.color;
//
//import com.mojang.serialization.Codec;
//import com.mojang.serialization.codecs.RecordCodecBuilder;
//import foundry.veil.quasar.client.particle.QuasarVanillaParticle;
//import foundry.veil.quasar.data.module.ModuleType;
//import foundry.veil.quasar.client.particle.RenderData;
//import foundry.veil.quasar.util.ColorGradient;
//import org.jetbrains.annotations.NotNull;
//import org.joml.Vector4f;
//
//public class ColorOverTimeParticleModule extends ColorParticleModule {
//
//    public static final Codec<ColorOverTimeParticleModule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
//            ColorGradient.CODEC.fieldOf("gradient").forGetter(ColorOverTimeParticleModule::getGradient)
//    ).apply(instance, ColorOverTimeParticleModule::new));
//
//    ColorGradient gradient;
//
//    public ColorOverTimeParticleModule(ColorGradient gradient) {
//        super(gradient.getColor(0));
//        this.gradient = gradient;
//    }
//
//    public ColorGradient getGradient() {
//        return gradient;
//    }
//
//    @Override
//    public void render(QuasarVanillaParticle particle, float partialTicks, RenderData data) {
//        float life = Math.min((particle.getAge() + partialTicks) / particle.getLifetime(), 1.0F);
//        Vector4f col = this.gradient.getColor(life);
//        data.setColor(col.x(), col.y(), col.z(), col.w());
//    }
//
//    @Override
//    public @NotNull ModuleType<?> getType() {
//        return ModuleType.COLOR_OVER_LIFETIME;
//    }
//}
