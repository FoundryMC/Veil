Veil provides a very basic Vec3 animation keyframing system. By creating a list of Keyframes and creating a Path object
from those, you can then call `Path#frameAtProgress(float)` to get the Frame at the current time. Veil automatically
populates the frames between keyframes using the easing methods defined when creating the keyframes.

## Keyframes

Keyframes are the core to Veil's animation system, even though they are extremely simple. Keyframes require a Position (
Vec3), Rotation (Vec3), Scale (Vec3), Duration (in ticks) and an Easing.

## Paths

Paths are Veil's approach at an `Animation Timeline`. Paths store a list of keyframes, populate the filler frames, and
provide methods to get needed information from the current/next/past frames.

To create a Path, you must supply with a list of Frames (usually Keyframes), a boolean for whether or not to loop the
animation, and optionally a boolean on whether or not to use Bézier interpolation when creating the filler frames.

An example usecase:

```java
Path arcPath=new Path(List.of(
        new Keyframe(new Vec3(0.5,0.5,-0.5),Vec3.ZERO,Vec3.ZERO,20,Easings.Easing.linear),
        new Keyframe(new Vec3(0.5,0.5,-0.5),Vec3.ZERO,Vec3.ZERO,5,Easings.Easing.easeInQuad),
        new Keyframe(new Vec3(0.5,1.15,-0.5),Vec3.ZERO,Vec3.ZERO,10,Easings.Easing.easeInBounce),
        new Keyframe(new Vec3(0.5,1.2,-0.25),Vec3.ZERO,Vec3.ZERO,5,Easings.Easing.easeInBounce),
        new Keyframe(new Vec3(0.5,1.2,0.25),Vec3.ZERO,Vec3.ZERO,5,Easings.Easing.easeInBounce),
        new Keyframe(new Vec3(0.5,1.2,0.75),Vec3.ZERO,Vec3.ZERO,10,Easings.Easing.easeInBounce),
// Loop, then Bézier
        ),false,true)

// block entity render method
public void render(MyBlockEntity blockEntity,...){
        //...
        int processingTicks=blockEntity.getProcessingTime();
        Vec3 renderPos=arcPath.frameAtProgress(processingTicks/30f).position()
        poseStack.translate(renderPos.x,renderPos.y,renderPos.z);
        //...
        }
```

Bézier interpolation uses a cubic Bézier curve and disregards the Easing method
chosen. [See the math here](https://github.com/FoundryMC/Veil/blob/main/Common/src/main/java/foundry/veil/anim/Frame.java#L40)
