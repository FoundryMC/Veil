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

    public Frame bezierInterpolate(Frame frame, float progress, Easings.Easing easing) {
        // interpolate from this to frame using a bezier curve
        // https://en.wikipedia.org/wiki/B%C3%A9zier_curve#Cubic_B%C3%A9zier_curves
        Vec3 p0 = this.position;
        Vec3 p1 = this.position.add(this.rotation);
        Vec3 p2 = frame.position.add(frame.rotation);
        Vec3 p3 = frame.position;
        Vec3 position = p0.scale((float) Math.pow(1 - progress, 3))
                .add(p1.scale(3 * progress * (float) Math.pow(1 - progress, 2)))
                .add(p2.scale(3 * (float) Math.pow(progress, 2) * (1 - progress)))
                .add(p3.scale((float) Math.pow(progress, 3)));
        Vec3 r0 = this.rotation;
        Vec3 r1 = this.rotation.add(this.scale);
        Vec3 r2 = frame.rotation.add(frame.scale);
        Vec3 r3 = frame.rotation;
        Vec3 rotation = r0.scale((float) Math.pow(1 - progress, 3))
                .add(r1.scale(3 * progress * (float) Math.pow(1 - progress, 2)))
                .add(r2.scale(3 * (float) Math.pow(progress, 2) * (1 - progress)))
                .add(r3.scale((float) Math.pow(progress, 3)));
        Vec3 s0 = this.scale;
        Vec3 s1 = this.scale.add(this.position);
        Vec3 s2 = frame.scale.add(frame.position);
        Vec3 s3 = frame.scale;
        Vec3 scale = s0.scale((float) Math.pow(1 - progress, 3))
                .add(s1.scale(3 * progress * (float) Math.pow(1 - progress, 2)))
                .add(s2.scale(3 * (float) Math.pow(progress, 2) * (1 - progress)))
                .add(s3.scale((float) Math.pow(progress, 3)));
        return new Frame(position, rotation, scale);
    }
}
