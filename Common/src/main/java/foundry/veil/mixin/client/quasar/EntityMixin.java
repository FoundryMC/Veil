package foundry.veil.mixin;

import foundry.veil.quasar.emitters.ParticleEmitter;
import foundry.veil.quasar.util.EntityExtension;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityExtension {

    @Shadow
    public abstract Vec3 position();

    @Unique
    private final List<ParticleEmitter> quasar$emitters = new ArrayList<>();

    @Override
    public List<ParticleEmitter> getEmitters() {
        return this.quasar$emitters;
    }

    @Override
    public void addEmitter(ParticleEmitter emitter) {
        this.quasar$emitters.add(emitter);
    }

    @Inject(method = "onClientRemoval", at = @At("TAIL"))
    public void remove(CallbackInfo ci) {
        this.quasar$emitters.forEach(ParticleEmitter::remove);
        this.quasar$emitters.clear();
    }
}
