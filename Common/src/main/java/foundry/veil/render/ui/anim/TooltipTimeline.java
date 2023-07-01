package foundry.veil.render.ui.anim;

public class TooltipTimeline {
    private TooltipKeyframe[] keyframes;
    private int currentKeyframe;
    private int nextKeyframe;
    private float animationProgress;
    private float animationSpeed;
    private boolean isPlaying;

    public TooltipTimeline(TooltipKeyframe[] keyframes, float animationSpeed) {
        this.keyframes = keyframes;
        this.animationSpeed = animationSpeed;
    }

    public void update() {
        if (animationProgress < 1) {
            animationProgress += animationSpeed;
        } else {
            currentKeyframe++;
            nextKeyframe++;
            animationProgress = 0;
        }
    }

    public TooltipKeyframe getCurrentKeyframe() {
        return keyframes[currentKeyframe];
    }

    public TooltipKeyframe getNextKeyframe() {
        return keyframes[nextKeyframe];
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void addFrameForTime(TooltipKeyframe keyframe, float duration){
        // add a keyframe that lasts for a certain amount of time
        for (int i = 0; i < duration; i++) {
            addFrame(keyframe);
        }
    }

    private void addFrame(TooltipKeyframe keyframe) {
        keyframes[keyframes.length-1] = keyframe;
    }

    public float getAnimationProgress() {
        return animationProgress;
    }

    public boolean isFinished() {
        return currentKeyframe == keyframes.length - 1;
    }

    public void reset() {
        currentKeyframe = 0;
        nextKeyframe = 1;
        animationProgress = 0;
    }

    public void setAnimationSpeed(float animationSpeed) {
        this.animationSpeed = animationSpeed;
    }

    public float getAnimationSpeed() {
        return animationSpeed;
    }

    public void setKeyframes(TooltipKeyframe[] keyframes) {
        this.keyframes = keyframes;
    }

    public TooltipKeyframe[] getKeyframes() {
        return keyframes;
    }
}
