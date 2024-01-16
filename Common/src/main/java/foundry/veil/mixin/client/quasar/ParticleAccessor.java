package foundry.veil.mixin.client.quasar;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Particle.class)
public interface ParticleAccessor {
    @Accessor("x")
    double getX();

    @Accessor("y")
    double getY();

    @Accessor("z")
    double getZ();

    @Accessor("xd")
    double getXd();

    @Accessor("yd")
    double getYd();

    @Accessor("zd")
    double getZd();

    @Accessor("level")
    ClientLevel getLevel();

}
