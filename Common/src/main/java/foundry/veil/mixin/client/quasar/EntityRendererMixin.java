package foundry.veil.mixin.client.quasar;

import com.mojang.blaze3d.vertex.PoseStack;
import foundry.veil.quasar.data.ParticleEmitterData;
import foundry.veil.quasar.emitters.ParticleEmitter;
import foundry.veil.quasar.emitters.ParticleEmitterRegistry;
import foundry.veil.quasar.emitters.ParticleSystemManager;
import foundry.veil.quasar.emitters.modules.particle.update.forces.PointForce;
import foundry.veil.quasar.util.EntityExtension;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

    @Inject(method = "render", at = @At("HEAD"))
    public void render(T entity, float pEntityYaw, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, CallbackInfo ci) {
        EntityExtension extension = (EntityExtension) entity;
        if (entity.isOnFire()) {
            if (extension.getEmitters().isEmpty()) {
                ParticleEmitterData emitter = ParticleEmitterRegistry.getEmitter(new ResourceLocation("veil:basic_smoke"));
                if (emitter == null) {
                    return;
                }

                ParticleEmitter instance = new ParticleEmitter(entity.level(), emitter);
                instance.setPosition(entity.position());
//                instance.getEmitterSettingsModule().emissionShapeSettings().setDimensions(
//                        new Vector3f(
//                                pEntity.getBbWidth(),
//                                pEntity.getBbHeight(),
//                                pEntity.getBbWidth()
//                        )
//                );
//                instance.setLoop(true);
//                instance.setMaxLifetime(5);
//                instance.getParticleData().getForces().forEach(force -> {
//                    if (force instanceof PointForce pf) {
//                        pf.setPoint(pEntity::position);
//                    }
//                });
                // FIXME
                extension.addEmitter(instance);
                ParticleSystemManager.getInstance().addParticleSystem(instance);
            } else {
//                ((EntityExtension) pEntity).getEmitters().stream().filter(emitter -> emitter.registryName.toString().equals("veil:basic_smoke")).forEach(emitter -> emitter.getEmitterModule().setMaxLifetime(5));
            }
        } else {
//            ((EntityExtension) pEntity).getEmitters().stream().filter(emitter -> emitter.registryName.toString().equals("veil:basic_smoke")).forEach(p -> p.getEmitterModule().setLoop(false));
            extension.getEmitters().forEach(emitter -> {
                if ("veil:basic_smoke".equals(String.valueOf(emitter.getRegistryName()))) {
                    emitter.remove();
                }
            });
            extension.getEmitters().removeIf(emitter -> "veil:basic_smoke".equals(String.valueOf(emitter.getRegistryName())));
        }
    }
}
