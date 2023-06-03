package foundry.veil.anim;

import foundry.veil.math.Easings;
import net.minecraft.world.phys.Vec3;

public class Keyframe extends Frame {
    int duration;
    Easings.Easing easing;
    public Keyframe(Vec3 position, Vec3 rotation, Vec3 scale, int duration, Easings.Easing easing) {
        super(position, rotation, scale);
    }

    public Keyframe copy(){
        return new Keyframe(position, rotation, scale, duration, easing);
    }

    public int getDuration() {
        return duration;
    }

    public Easings.Easing getEasing() {
        return easing;
    }
}
