package foundry.veil.render.shader.processor;

import foundry.veil.render.shader.program.ProgramDefinition;
import foundry.veil.render.shader.definition.ShaderPreDefinitions;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Modifies the source code of a shader before compilation.
 *
 * @author Ocelot
 */
public interface ShaderPreProcessor {

    Pattern UNIFORM_PATTERN = Pattern.compile(".*uniform\\s+(?<type>\\w+)\\W(?<name>\\w*)");

    /**
     * Called once when a shader is first run through the pre-processor.
     */
    default void prepare() {
    }

    /**
     * Modifies the specified shader source input.
     *
     * @param context Context for modifying shaders
     * @return The modified source or the input if nothing changed
     * @throws IOException If any error occurs while editing the source
     */
    String modify(Context context) throws IOException;

    /**
     * Context for modifying source code and shader behavior.
     */
    interface Context {

        /**
         * Runs the specified source through the entire processing list.
         *
         * @return The modified source
         * @throws IOException If any error occurs while editing the source
         */
        String modify(String source) throws IOException;

        /**
         * Sets the uniform binding for a shader.
         *
         * @param name    The name of the uniform
         * @param binding The binding to set it to
         */
        void addUniformBinding(String name, int binding);

        /**
         * Marks this shader as dependent on the specified pre-definition.
         * When definitions change, only shaders marked as dependent on that definition will be recompiled.
         *
         * @param name The name of the definition to depend on
         */
        void addDefinitionDependency(String name);

        /**
         * @return The input source code. This is GLSL
         */
        String getInput();

        /**
         * @return The definition of the program this is being compiled for or <code>null</code> if the shader is standalone
         */
        @Nullable ProgramDefinition getDefinition();

        /**
         * @return The set of pre-definitions for shaders
         */
        ShaderPreDefinitions getPreDefinitions();
    }
}
