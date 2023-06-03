package foundry.veil.anim;

import foundry.veil.math.Easings;
import net.minecraft.world.phys.Vec3;

public class Frame {
    Vec3 position;
    Vec3 rotation;
    Vec3 scale;

    public Frame(Vec3 position, Vec3 rotation, Vec3 scale){
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
    }

    public Vec3 getPosition() {
        return position;
    }

    public Vec3 getRotation() {
        return rotation;
    }

    public Vec3 getScale() {
        return scale;
    }

    public Frame copy(){
        return new Frame(position, rotation, scale);
    }

    public Frame interpolate(Frame frame, float progress, Easings.Easing easing) {
        Vec3 position = this.position.add(frame.position.subtract(this.position).scale(easing.ease(progress)));
        Vec3 rotation = this.rotation.add(frame.rotation.subtract(this.rotation).scale(easing.ease(progress)));
        Vec3 scale = this.scale.add(frame.scale.subtract(this.scale).scale(easing.ease(progress)));
        return new Frame(position, rotation, scale);
    }
}
