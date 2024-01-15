package foundry.veil.api.client.render.shader;

import org.jetbrains.annotations.NotNull;

/**
 * An exception used to indicate shader compilation and linking issues.
 *
 * @author Ocelot
 */
public class ShaderException extends Exception {

    private final String glError;

    public ShaderException(@NotNull String error, @NotNull String glError) {
        super(error);
        this.glError = glError;
    }

    /**
     * @return The OpenGL shader error.
     */
    public @NotNull String getGlError() {
        return this.glError;
    }
}
