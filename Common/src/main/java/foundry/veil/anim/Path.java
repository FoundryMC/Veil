package foundry.veil.anim;

import java.util.ArrayList;
import java.util.List;

public class Path {
    List<Frame> frames;
    boolean loop;
    boolean pingPong = false;
    Frame currentFrame;

    public Path(List<Frame> frames, boolean loop) {
        this.frames = frames;
        this.loop = loop;
        currentFrame = frames.get(0);
        populateFrames();
    }

    public Path(List<Frame> frames, boolean loop, boolean bezier) {
        this.frames = frames;
        this.loop = loop;
        currentFrame = frames.get(0);
        if (bezier) {
            populateFramesBezier();
        } else {
            populateFrames();
        }
    }

    public Keyframe getCurrentKeyframeAtProgress(float progress) {
        // find the keyframe before the current frame if the current frame is not a keyframe
        int index = frames.indexOf(frameAtProgress(progress));
        if (index == -1) {
            for (int i = 0; i < frames.size(); i++) {
                if (frames.get(i) instanceof Keyframe) {
                    index = i;
                    break;
                }
            }
        }
        Frame frame = frames.get(index);
        if (frame instanceof Keyframe) {
            return (Keyframe) frame;
        } else {
            return null;
        }
    }

    public Keyframe getKeyframeBefore(Frame frame) {
        int index = frames.indexOf(frame);
        if (index == -1) {
            return null;
        }
        for (int i = index; i >= 0; i--) {
            if (frames.get(i) instanceof Keyframe) {
                return (Keyframe) frames.get(i);
            }
        }
        return null;
    }

    private void populateFrames() {
        List<Frame> newFrames = new ArrayList<>();
        for (int i = 0; i < frames.size(); i++) {
            Frame frame = frames.get(i);
            newFrames.add(frame);
            if (frame instanceof Keyframe) {
                for (int j = 0; j < ((Keyframe) frame).getDuration(); j++) {
                    int frameIndex = i + 1 >= frames.size() ? loop ? 0 : i : i + 1;
                    newFrames.add(frame.interpolate(frames.get(frameIndex), j / ((float) ((Keyframe) frame).duration), ((Keyframe) frame).getEasing()));
                }
            } else {
                newFrames.add(frame);
            }
        }
        frames = newFrames;
    }

    private void populateFramesBezier() {
        List<Frame> newFrames = new ArrayList<>();
        for (int i = 0; i < frames.size(); i++) {
            Frame frame = frames.get(i);
            newFrames.add(frame);
            if (frame instanceof Keyframe) {
                for (int j = 0; j < ((Keyframe) frame).getDuration(); j++) {
                    int frameIndex = i + 1 >= frames.size() ? loop ? 0 : i : i + 1;
                    newFrames.add(frame.bezierInterpolate(frames.get(frameIndex), j / ((float) ((Keyframe) frame).duration), ((Keyframe) frame).getEasing()));
                }
            } else {
                newFrames.add(frame);
            }
        }
        frames = newFrames;
    }

    public int duration() {
        int duration = 0;
        for (Frame frame : frames) {
            if (frame instanceof Keyframe) {
                duration += ((Keyframe) frame).getDuration();
            } else {
                duration++;
            }
        }
        return duration;
    }

    void reverse() {
        List<Frame> newFrames = new ArrayList<>();
        for (int i = frames.size() - 1; i >= 0; i--) {
            Frame frame = frames.get(i);
            if (frame instanceof Keyframe) {
                newFrames.add(frame);
                for (int j = ((Keyframe) frame).getDuration() - 1; j >= 0; j--) {
                    int frameIndex = i - 1 < 0 ? loop ? frames.size() - 1 : i : i - 1;
                    newFrames.add(frame.interpolate(frames.get(frameIndex), j / ((float) ((Keyframe) frame).duration), ((Keyframe) frame).getEasing()));
                }
            } else {
                newFrames.add(frame);
            }
        }
        frames = newFrames;
    }

    public void next() {
        if (pingPong) {
            if (frames.indexOf(currentFrame) == frames.size() - 1) {
                reverse();
            } else if (frames.indexOf(currentFrame) == 0) {
                reverse();
            }
        }
        currentFrame = frames.get(frames.indexOf(currentFrame) + 1 >= frames.size() ? loop ? 0 : frames.size() - 1 : frames.indexOf(currentFrame) + 1);
    }

    public void previous() {
        if (pingPong) {
            if (frames.indexOf(currentFrame) == frames.size() - 1) {
                reverse();
            } else if (frames.indexOf(currentFrame) == 0) {
                reverse();
            }
        }
        currentFrame = frames.get(frames.indexOf(currentFrame) - 1 < 0 ? loop ? frames.size() - 1 : 0 : frames.indexOf(currentFrame) - 1);
    }

    public void reset() {
        currentFrame = frames.get(0);
    }

    public Frame frameAtProgress(float progress) {
        return frames.get((int) (frames.size() * progress));
    }

    public Frame getCurrentFrame() {
        return currentFrame;
    }
}
