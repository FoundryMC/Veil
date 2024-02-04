//package foundry.veil.quasar.emitters.modules.particle.render.color;
//
//import foundry.veil.quasar.client.particle.QuasarVanillaParticle;
//import foundry.veil.quasar.data.module.ModuleType;
//import foundry.veil.quasar.client.particle.RenderData;
//import foundry.veil.quasar.util.ColorGradient;
//import com.mojang.serialization.Codec;
//import com.mojang.serialization.codecs.RecordCodecBuilder;
//import org.jetbrains.annotations.NotNull;
//import org.joml.Vector4f;
//
//public class ColorOverVelocityParticleModule extends ColorParticleModule {
//    public static final Codec<ColorOverVelocityParticleModule> CODEC = RecordCodecBuilder.create(instance ->
//            instance.group(
//                    ColorGradient.CODEC.fieldOf("gradient").forGetter(ColorOverVelocityParticleModule::getGradient)
//            ).apply(instance, ColorOverVelocityParticleModule::new));
//    ColorGradient gradient;
//
//    public ColorOverVelocityParticleModule(ColorGradient gradient) {
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
//        double vel = particle.getDeltaMovement().length();
//        vel = Math.min(vel, 1);
//        Vector4f col = gradient.getColor((float) vel);
//        data.setColor(col.x(), col.y(), col.z(), col.w());
//    }
//
//    @Override
//    public @NotNull ModuleType<?> getType() {
//        return ModuleType.COLOR_OVER_VELOCITY;
//    }
//}
