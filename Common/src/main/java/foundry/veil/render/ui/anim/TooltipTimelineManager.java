package foundry.veil.render.ui.anim;

import java.util.HashMap;
import java.util.Map;

public class TooltipTimelineManager {
    private static TooltipTimelineManager instance;
    private final Map<String, TooltipTimeline> timelines = new HashMap<>();

    public static TooltipTimelineManager getInstance() {
        if (instance == null) {
            instance = new TooltipTimelineManager();
        }
        return instance;
    }

    public void addTimeline(String id, TooltipTimeline timeline) {
        timelines.put(id, timeline);
    }

    public TooltipTimeline getTimeline(String id) {
        return timelines.get(id);
    }

    public void removeTimeline(String id) {
        timelines.remove(id);
    }

    public void playTimeline(String id) {
        timelines.get(id).setPlaying(true);
    }

    public void stopTimeline(String id) {
        timelines.get(id).setPlaying(false);
    }

    public void resetTimeline(String id) {
        timelines.get(id).setPlaying(false);
        timelines.get(id).reset();
    }

    public void resetAllTimelines() {
        for (TooltipTimeline timeline : timelines.values()) {
            timeline.reset();
        }
    }

    public void stopAllTimelines() {
        for (TooltipTimeline timeline : timelines.values()) {
            timeline.setPlaying(false);
        }
    }

    public void playAllTimelines() {
        for (TooltipTimeline timeline : timelines.values()) {
            timeline.setPlaying(true);
        }
    }



    public void update() {
        for (TooltipTimeline timeline : timelines.values()) {
            if(timeline.isFinished()) {
                timeline.reset();
            }
            if(timeline.isPlaying()) {
                timeline.update();
            }
        }
    }
}
