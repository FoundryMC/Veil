package foundry.veil.material;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Vector4f;
import foundry.veil.material.types.*;
import foundry.veil.shader.ExtendedShaderInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

// TODO: implement the bridge between this class and the actual rendering using the material. Needs more research but definitely doable.
public class MaterialInstance {
    private static TextureManager textureManager;
    private static ResourceManager resourceManager;
    private RenderTarget renderTarget;
    private Material material;
    private HashMap<String, IMaterialField<?>> fields;
    //private PostChain postChain;
    private ShaderInstance coreShaderInstance;
    private ResourceLocation mainTexture;
    private ResourceLocation passJson;
    private HashMap<String, ResourceLocation> textures = new HashMap<>();
    private int[] rawData;

    private RenderType renderType;

    private final SimpleRenderTypeBuilder builder = new SimpleRenderTypeBuilder();
    public MaterialInstance(Material material, ResourceLocation passJson, VertexFormat format) throws IOException {
        textureManager = Minecraft.getInstance().getTextureManager();
        resourceManager = Minecraft.getInstance().getResourceManager();
        this.material = material;
        this.fields = new HashMap<>();
        for(String name : material.getFields().keySet()){
            fields.put(name, material.getFields().get(name).createField());
        }
        mainTexture = new ResourceLocation("veil", "textures/vfx/empty.png");
        rawData = new int[0];
        coreShaderInstance = new ExtendedShaderInstance(resourceManager, material.getShader(), format, material.getModid());
//        postChain = new PostChain(textureManager, resourceManager, renderTarget, passJson);
    }

    public void refresh(){
        this.fields = new HashMap<>();
        for(String name : material.getFields().keySet()){
            fields.put(name, material.getFields().get(name).createField());
        }
        mainTexture = new ResourceLocation("veil", "textures/vfx/empty.png");
        rawData = new int[0];
    }

    public RenderType constructRenderType(){
        if(builder.getTextureShard() != null) {
            builder.getTextureShard().texture = Optional.of(mainTexture);
        }
        builder.shader(new RenderStateShard.ShaderStateShard(() -> coreShaderInstance));
        return renderType = builder.build();
    }

    public void bindTextures(){
        RenderSystem.setShaderTexture(0, mainTexture);
        int i = 1;
        for(String name : textures.keySet()){
            // get index of texture
            RenderSystem.setShaderTexture(i, textures.get(name));
            i++;
        }
        for(String name : fields.keySet()){
            IMaterialField<?> field = fields.get(name);
            if(field instanceof MaterialTextureField mtf) {
                RenderSystem.setShaderTexture(i, mtf.getValue());
                i++;
            }
        }
        coreShaderInstance.safeGetUniform("TextureCount").set(i);
    }

    public void setShader(){
        RenderSystem.setShader(() -> coreShaderInstance);
    }

    public void bindUniforms(){
        for(String name : fields.keySet()){
            IMaterialField<?> field = fields.get(name);
            if(coreShaderInstance.getUniform(name) != null) {
                if(field instanceof MaterialColorField mcf) {
                    coreShaderInstance.getUniform(name).set(new Vector4f(mcf.getColor().getRed(), mcf.getColor().getGreen(), mcf.getColor().getBlue(), mcf.getColor().getAlpha()));
                } else if(field instanceof MaterialSliderField msf) {
                    coreShaderInstance.getUniform(name).set((msf).getValue());
                } else if(field instanceof MaterialValueField mvf) {
                    coreShaderInstance.getUniform(name).set((float)mvf.getValue());
                }
            }
        }
    }

    public void apply(){
        setShader();
        bindTextures();
        bindUniforms();
    }

    public SimpleRenderTypeBuilder getRenderTypeBuilder() {
        return builder;
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

    public HashMap<String, IMaterialField<?>> getFields() {
        return fields;
    }

    public Material getMaterial() {
        return material;
    }

    public String getShader() {
        return material.getShader();
    }

    public void setMaterial(Material material) {
        this.material = material;
        refresh();
    }

    public void setCoreShaderInstance(ShaderInstance coreShaderInstance) {
        this.coreShaderInstance = coreShaderInstance;
    }

    public ShaderInstance getCoreShaderInstance() {
        return coreShaderInstance;
    }




}
