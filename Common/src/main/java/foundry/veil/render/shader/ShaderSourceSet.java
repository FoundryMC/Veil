package foundry.veil.render.shader;

import net.minecraft.resources.FileToIdConverter;

import java.util.Map;

import static org.lwjgl.opengl.GL20C.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20C.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL32C.GL_GEOMETRY_SHADER;
import static org.lwjgl.opengl.GL40C.GL_TESS_CONTROL_SHADER;
import static org.lwjgl.opengl.GL40C.GL_TESS_EVALUATION_SHADER;
import static org.lwjgl.opengl.GL43C.GL_COMPUTE_SHADER;

/**
 * A location to load shader source files from.
 *
 * @author Ocelot
 */
public final class ShaderSourceSet {

    private final String folder;
    private final FileToIdConverter shaderDefinitionLister;
    private final FileToIdConverter glslConverter;
    private final Map<Integer, FileToIdConverter> typeConverters;

    public ShaderSourceSet(String folder) {
        this.folder = folder;
        this.shaderDefinitionLister = FileToIdConverter.json(folder);
        this.glslConverter = new FileToIdConverter(folder, ".glsl");
        this.typeConverters = Map.of(
                GL_VERTEX_SHADER, new FileToIdConverter(folder, ".vsh"),
                GL_TESS_CONTROL_SHADER, new FileToIdConverter(folder, ".tcsh"),
                GL_TESS_EVALUATION_SHADER, new FileToIdConverter(folder, ".tesh"),
                GL_GEOMETRY_SHADER, new FileToIdConverter(folder, ".gsh"),
                GL_FRAGMENT_SHADER, new FileToIdConverter(folder, ".fsh"),
                GL_COMPUTE_SHADER, new FileToIdConverter(folder, ".csh")
        );
    }

    /**
     * @return The location to load shader definitions from
     */
    public String getFolder() {
        return this.folder;
    }

    /**
     * @return The lister for shader definitions
     */
    public FileToIdConverter getShaderDefinitionLister() {
        return this.shaderDefinitionLister;
    }

    /**
     * @return The converter for a regular <code>glsl</code> shader type
     */
    public FileToIdConverter getGlslConverter() {
        return this.glslConverter;
    }

    /**
     * Retrieves the id converter of a shader type.
     *
     * @param type The GL enum for the type
     * @return The file type converter or <code>glsl</code> if the type is unknown
     */
    public FileToIdConverter getTypeConverter(int type) {
        return this.typeConverters.getOrDefault(type, this.glslConverter);
    }
}
