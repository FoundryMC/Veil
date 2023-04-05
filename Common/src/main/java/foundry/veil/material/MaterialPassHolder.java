package foundry.veil.material;

import com.mojang.blaze3d.shaders.Uniform;
import net.minecraft.client.renderer.PostPass;

public class MaterialPassHolder {
    private PostPass pass;
    private IMaterialField[] fields;

    // TODO: this holds all of the uniforms for the pass, this is where we edit and update them
    public MaterialPassHolder(PostPass pass, IMaterialField[] fields) {
        this.pass = pass;
        this.fields = fields;
    }

    public Uniform getUniform(String name) {
        return pass.getEffect().getUniform(name);
    }
}
