package foundry.veil.api.client.necromancer.animation.keyframe;

import foundry.veil.api.client.necromancer.Bone;
import foundry.veil.api.client.necromancer.Skeleton;
import foundry.veil.api.client.necromancer.SkeletonParent;
import foundry.veil.api.client.necromancer.animation.Animation;
import foundry.veil.api.client.util.Easing;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;

import java.util.*;

public class KeyframedAnimation<P extends SkeletonParent<?, ?>, S extends Skeleton> extends Animation<P, S> {
    private final Map<String, KeyframeTimeline> keyframesByBoneName;
    private final boolean additive;
    private final boolean looping;

    private KeyframedAnimation(Map<String, KeyframeTimeline> keyframesByBoneName, boolean additive, boolean looping) {
        this.keyframesByBoneName = keyframesByBoneName;
        this.additive = additive;
        this.looping = looping;
    }

    // todo: make this more static
    // so it works with multithreading.
    // no idea how to do that though without
    // a ton of allocations though!
    private final Quaternionf tempRotationA = new Quaternionf(), tempRotationB = new Quaternionf();
    private final Keyframe[] tempKeyframes = new Keyframe[4];

    @Override
    public void apply(P parent, S skeleton, float mixFactor, float time) {
        for (Map.Entry<String, KeyframeTimeline> timeline : keyframesByBoneName.entrySet()) {
            Bone bone = skeleton.bones.get(timeline.getKey());
            if (bone == null) {
                continue;
            }

            KeyframeTimeline keyframes = timeline.getValue();
            float t = keyframes.getAdjacentKeyframes(time, this.looping, tempKeyframes);
            Keyframe a = tempKeyframes[1];
            Keyframe b = tempKeyframes[2];
            float easing = a.easing().ease(t);

            if (additive) {
                float allowed = Mth.clamp(1F - bone.additiveMixUsed, 0F, 1F);
                float effectiveMix = Math.min(mixFactor, allowed);
                bone.additiveMixUsed += effectiveMix;

                if (effectiveMix == 0) continue;

                if (!(a.transform().px() == 0 && a.transform().py() == 0 && a.transform().pz() == 0 &&
                      b.transform().px() == 0 && b.transform().py() == 0 && b.transform().pz() == 0)) {
                    bone.position.add(
                            Mth.lerp(effectiveMix, 0, Mth.lerp(easing, a.transform().px(), b.transform().px())),
                            Mth.lerp(effectiveMix, 0, Mth.lerp(easing, a.transform().py(), b.transform().py())),
                            Mth.lerp(effectiveMix, 0, Mth.lerp(easing, a.transform().pz(), b.transform().pz()))
                    );
                }
                tempRotationA.set(a.transform().qx(), a.transform().qy(), a.transform().qz(), a.transform().qw());
                tempRotationB.set(b.transform().qx(), b.transform().qy(), b.transform().qz(), b.transform().qw());
                if(!(tempRotationA.equals(0,0,0,1) && tempRotationB.equals(0,0,0,1))){
                    tempRotationA.slerp(tempRotationB, easing);
                    tempRotationA.slerp(tempRotationB.identity(), effectiveMix);
                    bone.rotation.premul(tempRotationA);
                }
                if (!(a.transform().sx() == 1 && a.transform().sy() == 1 && a.transform().sz() == 1 &&
                      b.transform().sx() == 1 && b.transform().sy() == 1 && b.transform().sz() == 1)) {
                    bone.size.mul(
                            Mth.lerp(effectiveMix, 1, Mth.lerp(easing, a.transform().sx(), b.transform().sx())),
                            Mth.lerp(effectiveMix, 1, Mth.lerp(easing, a.transform().sy(), b.transform().sy())),
                            Mth.lerp(effectiveMix, 1, Mth.lerp(easing, a.transform().sz(), b.transform().sz()))
                    );
                }
            } else {
                bone.position.set(
                        Mth.lerp(mixFactor, bone.position.x, Mth.lerp(easing, a.transform().px(), b.transform().px())),
                        Mth.lerp(mixFactor, bone.position.y, Mth.lerp(easing, a.transform().py(), b.transform().py())),
                        Mth.lerp(mixFactor, bone.position.z, Mth.lerp(easing, a.transform().pz(), b.transform().pz()))
                );
                tempRotationA.set(a.transform().qx(), a.transform().qy(), a.transform().qz(), a.transform().qw());
                tempRotationB.set(b.transform().qx(), b.transform().qy(), b.transform().qz(), b.transform().qw());
                bone.rotation.slerp(tempRotationA.slerp(tempRotationB, easing), mixFactor);
                bone.size.set(
                        Mth.lerp(mixFactor, bone.size.x, Mth.lerp(easing, a.transform().sx(), b.transform().sx())),
                        Mth.lerp(mixFactor, bone.size.y, Mth.lerp(easing, a.transform().sy(), b.transform().sy())),
                        Mth.lerp(mixFactor, bone.size.z, Mth.lerp(easing, a.transform().sz(), b.transform().sz()))
                );
            }
        }
    }

    public static class Builder<P extends SkeletonParent<?, ?>, S extends Skeleton> {
        boolean looped = false, additive = false;
        Map<String, List<Keyframe>> timelines = new HashMap<>();

        public Builder<P, S> looped(boolean isLooped) {
            this.looped = isLooped;
            return this;
        }

        public Builder<P, S> additive(boolean isAdditive) {
            this.additive = isAdditive;
            return this;
        }

        // todo: allow for keyframes to only specify one channel (ex. only position, only orientation, etc.)
        // probably only in the builder? and just bake everything to equivalent full keyframes
        // idk!
        public void addKeyframe(String boneId, float time, Easing easing,
                                Vector3fc position, Vector3fc size, Quaternionfc orientation) {
            if (!timelines.containsKey(boneId)) {
                timelines.put(boneId, new ArrayList<>(2));
            }
            timelines.get(boneId).add(new Keyframe(time, easing, new Keyframe.KeyframeTransform(position, size, orientation)));
        }

        public KeyframedAnimation<P, S> build() {
            Map<String, KeyframeTimeline> builtTimelines = new HashMap<>();
            for (Map.Entry<String, List<Keyframe>> timeline : timelines.entrySet()) {
                List<Keyframe> keyframeList = timeline.getValue();
                keyframeList.sort(Comparator.comparingDouble(Keyframe::time));
                KeyframeTimeline builtTimeline = new KeyframeTimeline(keyframeList.toArray(new Keyframe[0]));
                builtTimelines.put(timeline.getKey(), builtTimeline);
            }
            return new KeyframedAnimation<>(builtTimelines, this.additive, this.looped);
        }
    }
}
