package foundry.veil.api.client.render.profiler;

import static org.lwjgl.opengl.ARBPipelineStatisticsQuery.*;

/**
 * The types of statistics that can be captured by the profiler.
 *
 * @since 2.0.0
 */
public enum RenderProfilerCounter {
    VERTICES_SUBMITTED(GL_VERTICES_SUBMITTED_ARB),
    PRIMITIVES_SUBMITTED(GL_PRIMITIVES_SUBMITTED_ARB),
    VERTEX_SHADER_INVOCATIONS(GL_VERTEX_SHADER_INVOCATIONS_ARB),
    TESS_CONTROL_SHADER_PATCHES(GL_TESS_CONTROL_SHADER_PATCHES_ARB),
    TESS_EVALUATION_SHADER_INVOCATIONS(GL_TESS_EVALUATION_SHADER_INVOCATIONS_ARB),
    GEOMETRY_SHADER_INVOCATIONS(GL_GEOMETRY_SHADER_INVOCATIONS),
    GEOMETRY_SHADER_PRIMITIVES_EMITTED(GL_GEOMETRY_SHADER_PRIMITIVES_EMITTED_ARB),
    FRAGMENT_SHADER_INVOCATIONS(GL_FRAGMENT_SHADER_INVOCATIONS_ARB),
    COMPUTE_SHADER_INVOCATIONS(GL_COMPUTE_SHADER_INVOCATIONS_ARB),
    CLIPPING_INPUT_PRIMITIVES(GL_CLIPPING_INPUT_PRIMITIVES_ARB),
    CLIPPING_OUTPUT_PRIMITIVES(GL_CLIPPING_OUTPUT_PRIMITIVES_ARB);

    /**
     * All statistic types.
     */
    public static final RenderProfilerCounter[] ALL = RenderProfilerCounter.values();

    /**
     * All normal statistic types for rendering.
     */
    public static final RenderProfilerCounter[] STANDARD_GEOMETRY = {
            RenderProfilerCounter.VERTICES_SUBMITTED,
            RenderProfilerCounter.PRIMITIVES_SUBMITTED,
            RenderProfilerCounter.VERTEX_SHADER_INVOCATIONS,
            RenderProfilerCounter.FRAGMENT_SHADER_INVOCATIONS,
            RenderProfilerCounter.CLIPPING_INPUT_PRIMITIVES,
            RenderProfilerCounter.CLIPPING_OUTPUT_PRIMITIVES
    };

    private final int id;

    RenderProfilerCounter(int id) {
        this.id = id;
    }

    /**
     * @return The OpenGL id of this statistic
     */
    public int getId() {
        return this.id;
    }
}
