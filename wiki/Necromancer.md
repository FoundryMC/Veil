Veil includes an animation API, named Necromancer. Necromancer provides a framework for models and animations, with a focus on a procedural workflow.

# Skeleton

[`Skeleton`](https://github.com/FoundryMC/Veil/blob/1.21/common/src/main/java/foundry/veil/api/client/necromancer/Skeleton.java)s are the basis of Necromancer, acting as a container for [`Bone`](https://github.com/FoundryMC/Veil/blob/1.21/common/src/main/java/foundry/veil/api/client/necromancer/Bone.java)s. `Skeleton`s do very little on their own - they require a [`Skin`](https://github.com/FoundryMC/Veil/blob/1.21/common/src/main/java/foundry/veil/api/client/necromancer/render/Skin.java) to be rendered, and an [`Animator`](https://github.com/FoundryMC/Veil/blob/1.21/common/src/main/java/foundry/veil/api/client/necromancer/animation/Animator.java) to be puppeteered.

`Skeleton`s only have their animation data calculated each tick, and their `Bone`s automatically interpolate between these values. Support is planned for variable update times, depending on entity distance and performance.

`Skeleton`s may have their `Bone`s initialized in the constructor, though they may also be added dynamically if needed.
The `Skeleton#buildRoots` method should be called after adding `Bone`s!

An example `Skeleton` class:

### Example
```java
public class ExampleSkeleton extends Skeleton<ExampleEntity> {
    protected final Bone Head, Body, LeftLeg, RightLeg;
    
    public ExampleSkeleton(ExampleEntity parent) {
        super();
        this.Body = new Bone("Body");
        this.Body.setInitialTransform(0F, 16F, 0F, new Quaternionf().rotationZYX(0F, 0F, 0F));
        this.addBone(Body);

        this.Head = new Bone("Head");
        this.Head.setInitialTransform(0F, 8F, 0F, new Quaternionf().rotationZYX(0F, 0F, 0F));
        this.addBone(Head);

        this.LeftLeg = new Bone("LeftLeg");
        this.LeftLeg.setInitialTransform(-8F, -8F, 0F, new Quaternionf().rotationZYX(1.5F, 0F, 0F));
        this.addBone(LeftLeg);

        this.RightLeg = new Bone("RightLeg");
        this.RightLeg.setInitialTransform(8F, 8F, 0F, new Quaternionf().rotationZYX(1.5F, 0F, 0F));
        this.addBone(RightLeg);

        this.Body.addChild(Head);
        this.Body.addChild(RightLeg);
        this.Body.addChild(LeftLeg);

        this.buildRoots();
    }
}
```

# Animator

An `Animator` puppeteers a `Skeleton`, updating the transformations of bones and the like. It does this through the `Animator#animate` method, as well as applying given animations and constraints each tick.

## Animation

An [`Animation`](https://github.com/FoundryMC/Veil/blob/1.21/common/src/main/java/foundry/veil/api/client/necromancer/animation/Animation.java) applies a transformation to the bones of a skeleton, using the `Animator#apply` method. Animations should be _statically created_, and shared between animators which utilize them. An animation can be provided to an animator using the `Animator#addAnimation` method or the `Animator#addTimedAnimation` method. An animation is designed to work procedurally, with custom animations generating their transformations via code.

### KeyframedAnimation
[`KeyframedAnimation`](https://github.com/FoundryMC/Veil/blob/1.21/common/src/main/java/foundry/veil/api/client/necromancer/animation/keyframe/KeyframedAnimation.java) is a subclass of `Animation`, which transforms a `Skeleton` based off keyframes. These are currently a work-in-progress!

### AnimationEntry

[`AnimationEntry`](https://github.com/FoundryMC/Veil/blob/1.21/common/src/main/java/foundry/veil/api/client/necromancer/animation/Animator.java#L63) is used by the Animator to control attributes of an Animation before applying it to the Skeleton, such as an animation's time or blend factor. Returned by `Animator#addAnimation`. The entry's time is _not_ automatically updated, and must be set by the Animator. For entries with a given length and automatically updated time, see [`TimedAnimationEntry`](https://github.com/FoundryMC/Veil/blob/1.21/common/src/main/java/foundry/veil/api/client/necromancer/animation/Animator.java#L100).

### TimedAnimationEntry

A subclass of `AnimationEntry`, these are used to execute one-shot animations with a known length, such as attacks. Returned by `Animator#addTimedAnimation`. A `TimedAnimationEntry`'s time variable is automatically updated. 

## Constraints

`Constraint`s are applied by an `Animator` after any `Animation`s, and adjust the pose in order to satisfy them. Currently, these are a work-in-progress, and no constraints are implemented by Veil.

### Example
```java
public class ExampleAnimator extends Animator<ExampleEntity, ExampleSkeleton> {
    final AnimationEntry<ExampleEntity, ExampleSkeleton> walk;

    public ExampleAnimator(ExampleEntity entity, ExampleSkeleton skeleton) {
        this.walk = this.addAnimation(WalkAnimation.INSTANCE, 0);
    }

    public void animate(ExampleEntity entity) {
        super.animate(entity);
        // walk
        WalkAnimation walkState = entity.walkAnimation;
        this.walk.setTime(walkState.position());
        this.walk.setMixFactor(walkState.speed());

        // idle
        skeleton.Body.y += Mth.sin(entity.tickCount * 0.05F) * 2;
    }

    static class WalkAnimation extends Animation<ExampleEntity, ExampleSkeleton> {
        static final INSTANCE = new WalkAnimation();
        public void apply(ExampleEntity entity, ExampleSkeleton skeleton, float blendFactor, float time) {
            skeleton.LeftLeg .rotateDeg(45 *  Mth.sin(time) * blendFactor, Direction.Axis.Z);
            skeleton.RightLeg.rotateDeg(45 * -Mth.cos(time) * blendFactor, Direction.Axis.Z);
        }
    }
}
```

# Skin

[`Skin`](https://github.com/FoundryMC/Veil/blob/1.21/common/src/main/java/foundry/veil/api/client/necromancer/render/Skin.java)s are laid atop `Skeleton`s, and are what is actually drawn to the screen. Currently, they contain a list of meshes and the identifier of their associated `Bone`, which they inherit the transform of.


# Attachment

## SkeletonParent

[`SkeletonParent`](https://github.com/FoundryMC/Veil/blob/1.21/common/src/main/java/foundry/veil/api/client/necromancer/SkeletonParent.java) is an interface, providing functionality for attaching a skeleton to an Entity (support for block entities is also planned.) When implemented by an entity, Necromancer will _automatically_ attach a `Skeleton` and `Animator`, generated using the factories specified in the entity's `NecromancerEntityRenderer`.

## NecromancerEntityRenderer

An extension of vanilla's `EntityRenderer`, built for entities implementing `SkeletonParent`. This automatically handles attaching `Skeleton`s and `Animator`s to entities, drawing the `Skeleton` through a provided `Skin`, and entity render layers.

### NecromancerEntityRenderLayer

An equivalent of vanilla's `EntityRenderLayer`, for entities implementing `SkeletonParent`. Should be added to a `NecromancerEntityRenderer` in its constructor, using the `NecromancerEntityRenderer#addLayer` method.