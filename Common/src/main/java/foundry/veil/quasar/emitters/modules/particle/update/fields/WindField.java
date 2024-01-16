package foundry.veil.quasar.emitters.modules.particle.update.fields;

import foundry.veil.util.FastNoiseLite;
import net.minecraft.world.phys.Vec3;

/**
 * A global wind field. Applies a uniform velocity change to particles.
 * Ticking is also possible, allowing the wind to shift and change over time.
 */
public class WindField {
    Vec3 windDirection;
    float strength;
    float falloff;
    FastNoiseLite noise;

    public WindField(Vec3 windDirection, float strength, float falloff, FastNoiseLite noise) {
        this.windDirection = windDirection;
        this.strength = strength;
        this.falloff = falloff;
        this.noise = noise;
    }

    public Vec3 getWindDirection() {
        return windDirection;
    }

    public void tickWind() {
        float x = (float) (windDirection.x() + noise.GetNoise(0, 0, 0) * 0.01);
        float y = (float) (windDirection.y() + noise.GetNoise(0, 0, 1) * 0.01);
        float z = (float) (windDirection.z() + noise.GetNoise(0, 0, 2) * 0.01);
        windDirection = new Vec3(x, y, z);
    }
}
