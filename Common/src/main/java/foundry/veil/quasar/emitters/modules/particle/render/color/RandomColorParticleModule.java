//package foundry.veil.quasar.emitters.modules.particle.render.color;
//
//import foundry.veil.quasar.client.particle.QuasarVanillaParticle;
//import foundry.veil.quasar.data.module.ModuleType;
//import foundry.veil.quasar.emitters.modules.particle.RenderParticleModule;
//import foundry.veil.quasar.emitters.modules.particle.render.RenderData;
//import org.jetbrains.annotations.NotNull;
//import org.joml.Vector4f;
//
//public class RandomColorParticleModule implements RenderParticleModule {
//
//    private final Vector4f[] colors;
//
//    public RandomColorParticleModule(Vector4f... colors) {
//        this.colors = colors;
//    }
//
//    @Override
//    public void render(QuasarVanillaParticle particle, float partialTicks, RenderData data) {
//        int index = (int) (Math.random() * this.colors.length);
//        Vector4f color = this.colors[index];
//        data.setColor(color.x(), color.y(), color.z(), color.w());
//    }
//
//    @NotNull
//    @Override
//    public ModuleType<?> getType() {
//        return null;
//    }
//
//}
