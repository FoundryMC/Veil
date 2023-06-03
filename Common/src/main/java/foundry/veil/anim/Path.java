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
    }

    private void populateFrames() {
        List<Frame> newFrames = new ArrayList<>();
        for(int i = 0; i < frames.size(); i++){
            Frame frame = frames.get(i);
            newFrames.add(frame);
            if(frame instanceof Keyframe) {
                for(int j = 0; j < ((Keyframe) frame).getDuration(); j++){
                    int frameIndex = i+1 >= frames.size() ? loop ? 0 : i : i +1;
                    newFrames.add(frame.interpolate(frames.get(frameIndex), j/((float)((Keyframe) frame).duration), ((Keyframe) frame).getEasing()));
                }
            } else {
                newFrames.add(frame);
            }
        }
        frames = newFrames;
    }

    public int duration() {
        int duration = 0;
        for(Frame frame : frames){
            if(frame instanceof Keyframe) {
                duration += ((Keyframe) frame).getDuration();
            } else {
                duration++;
            }
        }
        return duration;
    }

    void reverse() {
        List<Frame> newFrames = new ArrayList<>();
        for(int i = frames.size() - 1; i >= 0; i--){
            Frame frame = frames.get(i);
            if(frame instanceof Keyframe) {
                newFrames.add(frame);
                for(int j = ((Keyframe) frame).getDuration() - 1; j >= 0; j--){
                    int frameIndex = i-1 < 0 ? loop ? frames.size() - 1 : i : i -1;
                    newFrames.add(frame.interpolate(frames.get(frameIndex), j/((float)((Keyframe) frame).duration), ((Keyframe) frame).getEasing()));
                }
            } else {
                newFrames.add(frame);
            }
        }
        frames = newFrames;
    }

    public void next() {
        if(pingPong) {
            if(frames.indexOf(currentFrame) == frames.size() - 1) {
                reverse();
            } else if(frames.indexOf(currentFrame) == 0) {
                reverse();
            }
        }
        currentFrame = frames.get(frames.indexOf(currentFrame) + 1 >= frames.size() ? loop ? 0 : frames.size() - 1 : frames.indexOf(currentFrame) + 1);
    }

    public void previous() {
        if(pingPong) {
            if(frames.indexOf(currentFrame) == frames.size() - 1) {
                reverse();
            } else if(frames.indexOf(currentFrame) == 0) {
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
