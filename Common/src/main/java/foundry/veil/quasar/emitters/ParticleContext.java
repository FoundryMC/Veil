package foundry.veil.quasar.emitters;

import foundry.veil.quasar.client.particle.QuasarVanillaParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ParticleContext {

    private final Vec3 position;
    private final Vec3 velocity;
    private final QuasarVanillaParticle particle;

    public ParticleContext(Vec3 position, Vec3 velocity, QuasarVanillaParticle particle) {
        this.position = position;
        this.velocity = velocity;
        this.particle = particle;
    }

    public Vec3 getPosition() {
        if (this.particle != null) {
            return ((ParticleAccessorExtension) this.particle).getPosition();
        } else {
            return this.position;
        }
    }

    public BlockState getBlockstateInOrUnder() {
        if (this.particle != null) {
            Level level = this.getLevel();
            BlockState in = level.getBlockState(BlockPos.containing(((ParticleAccessorExtension) this.particle).getPosition().add(0, 0.5, 0)));
            BlockState under = level.getBlockState(BlockPos.containing(((ParticleAccessorExtension) this.particle).getPosition().add(0, -0.5, 0)));
            if (in.isAir()) {
                return under;
            } else {
                return in;
            }
        } else {
            return null;
        }
    }

    public Vec3 getVelocity() {
        if (this.particle != null) {
            return ((ParticleAccessorExtension) this.particle).getVelocity();
        } else {
            return this.velocity;
        }
    }

    public Level getLevel() {
        return this.particle.getLevel();
    }
}
