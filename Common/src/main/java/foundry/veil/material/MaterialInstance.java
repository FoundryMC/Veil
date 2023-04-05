package foundry.veil.material;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.shaders.Shader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.HashMap;

// TODO: implement the bridge between this class and the actual rendering using the material. Needs more research but definitely doable.
public class MaterialInstance {
    private static TextureManager textureManager;
    private static ResourceManager resourceManager;
    private RenderTarget renderTarget;
    private Material material;
    private IMaterialField[] fields;
    private PostChain postChain;

    private ResourceLocation mainTexture;
    private ResourceLocation passJson;
    private HashMap<String, ResourceLocation> textures = new HashMap<>();
    private int[] rawData;

    // TODO: This holds all of the passes for the material, this is where we contain them and store them

    public MaterialInstance(Material material, ResourceLocation passJson) throws IOException {
        textureManager = Minecraft.getInstance().getTextureManager();
        resourceManager = Minecraft.getInstance().getResourceManager();
        this.material = material;
        this.fields = new IMaterialField[material.getFields().length];
        for (int i = 0; i < material.getFields().length; i++) {
            fields[i] = material.getFields()[i].createField();
        }
        mainTexture = new ResourceLocation("veil", "textures/vfx/empty.png");
        rawData = new int[0];
        postChain = new PostChain(textureManager, resourceManager, renderTarget, passJson);
    }

    public void setRenderTarget(RenderTarget renderTarget) {
        this.renderTarget = renderTarget;
    }

    public RenderTarget getRenderTarget() {
        return renderTarget;
    }

    public void setMainTexture(ResourceLocation texture) {
        this.mainTexture = texture;
    }

    public ResourceLocation getMainTexture() {
        return mainTexture;
    }

    public ResourceLocation getTexture(String name) {
        return textures.get(name);
    }

    public void setRawData(int[] rawData) {
        this.rawData = rawData;
    }

    public int[] getRawData() {
        return rawData;
    }

    public IMaterialField[] getFields() {
        return fields;
    }

    public Material getMaterial() {
        return material;
    }

    public Shader getShader() {
        return material.getShader();
    }


}
