package foundry.veil.render.shader.compiler;

import foundry.veil.render.shader.ShaderProgram;

import java.util.Map;
import java.util.Set;

/**
 * A shader instance that has additional pre-compiled data.
 * {@link #apply(ShaderProgram)} should be called after this shader is attached to a program.
 *
 * @param id                     The OpenGL id of the shader. The shader is automatically deleted later
 * @param uniformBindings        The bindings set by the shader
 * @param definitionDependencies The shader pre-definitions this shader is dependent on
 * @author Ocelot
 */
public record CompiledShader(int id, Map<String, Integer> uniformBindings, Set<String> definitionDependencies) {

    /**
     * Applies the additional attributes of this shader to the specified program.
     */
    public void apply(ShaderProgram program) {
        this.uniformBindings.forEach(program::setInt);
    }
}
