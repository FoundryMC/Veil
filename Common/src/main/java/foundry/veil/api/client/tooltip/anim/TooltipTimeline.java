package foundry.veil.api.client.tooltip.anim;

public class TooltipTimeline {

    private final TooltipKeyframe[] keyframes;
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
        if (this.animationProgress < 1) {
            this.animationProgress += this.animationSpeed;
        } else {
            this.currentKeyframe++;
            this.nextKeyframe++;
            this.animationProgress = 0;
        }
    }

    public TooltipKeyframe getCurrentKeyframe() {
        return this.keyframes[this.currentKeyframe];
    }

    public TooltipKeyframe getNextKeyframe() {
        return this.keyframes[this.nextKeyframe];
    }

    public void setPlaying(boolean playing) {
        this.isPlaying = playing;
    }

    public boolean isPlaying() {
        return this.isPlaying;
    }

    public void addFrameForTime(TooltipKeyframe keyframe, float duration) {
        // add a keyframe that lasts for a certain amount of time
        for (int i = 0; i < duration; i++) {
            this.addFrame(keyframe);
        }
    }

    private void addFrame(TooltipKeyframe keyframe) {
        this.keyframes[this.keyframes.length - 1] = keyframe;
    }

    public float getAnimationProgress() {
        return this.animationProgress;
    }

    public boolean isFinished() {
        return this.currentKeyframe == this.keyframes.length - 1;
    }

    public void reset() {
        this.currentKeyframe = 0;
        this.nextKeyframe = 1;
        this.animationProgress = 0;
    }

    public void setAnimationSpeed(float animationSpeed) {
        this.animationSpeed = animationSpeed;
    }

    public float getAnimationSpeed() {
        return this.animationSpeed;
    }

    public TooltipKeyframe[] getKeyframes() {
        return this.keyframes;
    }
}
