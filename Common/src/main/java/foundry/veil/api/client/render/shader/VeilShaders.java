package foundry.veil.api.client.render.shader;

import foundry.veil.Veil;
import net.minecraft.resources.ResourceLocation;

/**
 * Default shader names.
 */
public final class VeilShaders {

    private VeilShaders() {
    }

//    public static final ResourceLocation POSITION = core("position");
//    public static final ResourceLocation POSITION_COLOR = core("position_color");
//    public static final ResourceLocation POSITION_COLOR_TEX = core("position_color_tex");
//    public static final ResourceLocation POSITION_TEX = core("position_tex");
//    public static final ResourceLocation POSITION_TEX_COLOR = core("position_tex_color");
//
//    public static final ResourceLocation RENDER_LINES = renderType("lines");
//    public static final ResourceLocation RENDER_TEXT = renderType("text");
//    public static final ResourceLocation RENDER_NAMEPLATE = renderType("nameplate");
//
//    public static final ResourceLocation BLOCK_SHADOW = block("shadow");
//    public static final ResourceLocation BLOCK_SOLID = block("solid");
//    public static final ResourceLocation BLOCK_CUTOUT_MIPPED = block("cutout_mipped");
//    public static final ResourceLocation BLOCK_CUTOUT = block("cutout");
//    public static final ResourceLocation BLOCK_TRANSLUCENT = block("translucent");
//    public static final ResourceLocation BLOCK_GUI = block("gui");
//
//    public static final ResourceLocation RENDER_SHADOW = renderType("shadow");
//
//    public static final ResourceLocation MODEL_CUTOUT = model("cutout");
//    public static final ResourceLocation MODEL_CUTOUT_CULL = model("cutout_cull");
//    public static final ResourceLocation MODEL_SOLID = model("solid");
//    public static final ResourceLocation MODEL_TRANSLUCENT = model("translucent");
//    public static final ResourceLocation MODEL_TRANSLUCENT_CULL = model("translucent_cull");
//
//    public static final ResourceLocation PARTICLE_SOLID = particle("solid");
//    public static final ResourceLocation PARTICLE_CUTOUT = particle("cutout");
//    public static final ResourceLocation PARTICLE_TRANSLUCENT = particle("translucent");

    public static final ResourceLocation LIGHT_AMBIENT = light("ambient");
    public static final ResourceLocation LIGHT_DIRECTIONAL = light("directional");
    public static final ResourceLocation LIGHT_POINT = light("point");
    public static final ResourceLocation LIGHT_AREA = light("area");
    public static final ResourceLocation LIGHT_VANILLA_LIGHTMAP = light("vanilla_lightmap");
    public static final ResourceLocation LIGHT_SKY = light("sky");
    public static final ResourceLocation LIGHT_INDIRECT_SPHERE = light("indirect_sphere");

    private static ResourceLocation core(String name) {
        return Veil.veilPath("core/" + name);
    }

    private static ResourceLocation renderType(String name) {
        return Veil.veilPath("rendertype/" + name);
    }

    private static ResourceLocation block(String name) {
        return Veil.veilPath("block/" + name);
    }

    private static ResourceLocation model(String name) {
        return Veil.veilPath("model/" + name);
    }

    private static ResourceLocation particle(String name) {
        return Veil.veilPath("particle/" + name);
    }

    private static ResourceLocation light(String name) {
        return Veil.veilPath("light/" + name);
    }
}
