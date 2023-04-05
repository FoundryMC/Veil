package foundry.veil.material;

import com.mojang.blaze3d.shaders.Shader;
import foundry.veil.material.types.MaterialFieldType;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;

/**
 * @author foundry
 * A Unity-like material class for Veil.
 * This class is used to store all the information about a material, such as the shader, ID, and field types.
 */
public class Material {
    private Shader shader;
    private ResourceLocation id;

    private MaterialFieldType[] fields;

    MaterialFieldType[] getFields() {
        return fields;
    }

    public Shader getShader() {
        return shader;
    }
}
