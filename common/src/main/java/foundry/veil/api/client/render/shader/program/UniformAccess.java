package foundry.veil.api.client.render.shader.program;

import foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL31C;

import static org.lwjgl.opengl.GL31C.GL_INVALID_INDEX;

/**
 * Provides read and write access to all uniform variables in a shader program.
 *
 * @author Ocelot
 */
public interface UniformAccess {

    /**
     * Retrieves the location of a uniform.
     *
     * @param name The name of the uniform to get
     * @return The location of that uniform or <code>-1</code> if not found
     */
    int getUniformLocation(CharSequence name);

    /**
     * Checks if the specified uniform exists in the shader.
     *
     * @param name The name of the uniform to check
     * @return Whether that uniform can be set
     */
    default boolean hasUniform(CharSequence name) {
        return this.getUniformLocation(name) != -1;
    }

    /**
     * Retrieves a uniform by name.
     *
     * @param name The name of the uniform to get
     * @return The uniform with that name or <code>null</code> if the uniform does not exist
     */
    @Nullable ShaderUniformAccess getUniform(CharSequence name);

    /**
     * Retrieves a uniform by name.
     *
     * @param name The name of the uniform to get
     * @return The uniform with that name
     */
    ShaderUniformAccess getUniformSafe(CharSequence name);

    /**
     * Retrieves a uniform by name or creates a reference to one that may exist in the future.
     *
     * @param name The name of the uniform to get
     * @return The uniform instance
     * @deprecated Use {@link #getUniformSafe(CharSequence)} instead
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "3.0.0")
    ShaderUniformAccess getOrCreateUniform(CharSequence name);

    /**
     * Retrieves the location of a uniform block.
     *
     * @param name The name of the uniform block to get
     * @return The location of that uniform block or {@value GL31C#GL_INVALID_INDEX} if not found
     */
    int getUniformBlock(CharSequence name);

    /**
     * Checks if the specified uniform block exists in the shader.
     *
     * @param name The name of the uniform block to check
     * @return Whether that uniform block can be set
     */
    default boolean hasUniformBlock(CharSequence name) {
        return this.getUniformBlock(name) != GL_INVALID_INDEX;
    }

    /**
     * Retrieves the location of a storage block.
     *
     * @param name The name of the storage block to get
     * @return The location of that storage block or {@value GL31C#GL_INVALID_INDEX} if not found
     */
    int getStorageBlock(CharSequence name);

    /**
     * Checks if the specified storage block exists in the shader.
     *
     * @param name The name of the storage block to check
     * @return Whether that storage block can be set
     */
    default boolean hasStorageBlock(CharSequence name) {
        return this.getStorageBlock(name) != GL_INVALID_INDEX;
    }
}
