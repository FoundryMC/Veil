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
    private String shader;
    private String modid;
    private ResourceLocation id;

    private HashMap<String, MaterialFieldType> fields;

    HashMap<String, MaterialFieldType> getFields() {
        return fields;
    }

    public String getShader() {
        return shader;
    }

    public String getModid() {
        return modid;
    }

    public ResourceLocation getId() {
        return id;
    }

    public Material(String shader, String modid, ResourceLocation id) {
        this.shader = shader;
        this.modid = modid;
        this.id = id;
        this.fields = new HashMap<>();
    }

    public Material(String shader, String modid, String id) {
        this(shader, modid, new ResourceLocation(modid, id));
    }

    public Material(String shader, String modid, String id, HashMap<String, MaterialFieldType> fields) {
        this(shader, modid, id);
        this.fields = fields;
    }

    public Material(String shader, String modid, ResourceLocation id, HashMap<String, MaterialFieldType> fields) {
        this(shader, modid, id);
        this.fields = fields;
    }

    public Material(String shader, String modid, String id, MaterialFieldType... fields) {
        this(shader, modid, id);
        for (MaterialFieldType field : fields) {
            this.fields.put(field.name(), field);
        }
    }

    public Material(String shader, String modid, ResourceLocation id, MaterialFieldType... fields) {
        this(shader, modid, id);
        for (MaterialFieldType field : fields) {
            this.fields.put(field.name(), field);
        }
    }
}
