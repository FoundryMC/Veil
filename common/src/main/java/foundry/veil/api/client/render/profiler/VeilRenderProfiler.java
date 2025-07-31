package foundry.veil.api.client.render.profiler;

import foundry.veil.impl.client.editor.PipelineStatisticsViewer;
import foundry.veil.impl.client.render.profiler.VeilRenderProfilerImpl;

import java.util.function.Supplier;

/**
 * Collects render information for the {@link PipelineStatisticsViewer}.
 *
 * @since 2.0.0
 */
public interface VeilRenderProfiler {

    /**
     * @return The currently active profiler instance
     */
    static VeilRenderProfiler get() {
        return VeilRenderProfilerImpl.get();
    }

    /**
     * Pushes a region onto the profile stack. This will only collect information if being viewed in the profiler window.
     *
     * @param name       The name of the region to push
     * @param statistics The statistics to capture or empty to capture all available
     */
    void push(String name, RenderProfilerCounter... statistics);

    /**
     * Pushes a region onto the profile stack. This will only collect information if being viewed in the profiler window.
     *
     * @param name       The name of the region to push
     * @param statistics The statistics to capture or empty to capture all available
     */
    void push(Supplier<String> name, RenderProfilerCounter... statistics);

    /**
     * Pops the current region.
     */
    void pop();

    /**
     * Pops, then pushes a region onto the profile stack. This will only collect information if being viewed in the profiler window.
     *
     * @param name       The name of the region to push
     * @param statistics The statistics to capture or empty to capture all available
     */
    void popPush(String name, RenderProfilerCounter... statistics);

    /**
     * Pops, then pushes a region onto the profile stack. This will only collect information if being viewed in the profiler window.
     *
     * @param name       The name of the region to push
     * @param statistics The statistics to capture or empty to capture all available
     */
    void popPush(Supplier<String> name, RenderProfilerCounter... statistics);
}
