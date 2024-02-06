package foundry.veil.mixin.client.quasar;

import foundry.veil.quasar.ParticleEmitter;
import foundry.veil.quasar.util.EntityExtension;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityExtension {

    @Unique
    private final List<ParticleEmitter> quasar$emitters = new ArrayList<>();

    @Override
    public List<ParticleEmitter> veil$getEmitters() {
        return this.quasar$emitters;
    }

    @Override
    public void veil$addEmitter(ParticleEmitter emitter) {
        this.quasar$emitters.add(emitter);
    }

    @Inject(method = "onClientRemoval", at = @At("TAIL"))
    public void remove(CallbackInfo ci) {
        this.quasar$emitters.forEach(ParticleEmitter::remove);
        this.quasar$emitters.clear();
    }
}
