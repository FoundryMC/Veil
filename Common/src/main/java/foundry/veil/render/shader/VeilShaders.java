package foundry.veil.render.shader;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Default shader names.
 */
public final class VeilShaders {

    private VeilShaders() {
    }

    public static final ResourceLocation POSITION = core("position");
    public static final ResourceLocation POSITION_COLOR = core("position_color");
    public static final ResourceLocation POSITION_COLOR_TEX = core("position_color_tex");
    public static final ResourceLocation POSITION_TEX = core("position_tex");
    public static final ResourceLocation POSITION_TEX_COLOR = core("position_tex_color");

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
//
//    public static final ResourceLocation LIGHT_AMBIENT = light("ambient");
//    public static final ResourceLocation LIGHT_DIRECTIONAL = light("directional");
//    public static final ResourceLocation LIGHT_SKY = light("sky");
//
//    public static final ResourceLocation UI_MAP_FOG = ui("map_fog");

    private static @NotNull ResourceLocation core(@NotNull String name) {
        return new ResourceLocation("starfall", "core/" + name);
    }

    private static @NotNull ResourceLocation renderType(@NotNull String name) {
        return new ResourceLocation("starfall", "rendertype/" + name);
    }

    private static @NotNull ResourceLocation block(@NotNull String name) {
        return new ResourceLocation("starfall", "block/" + name);
    }

    private static @NotNull ResourceLocation model(@NotNull String name) {
        return new ResourceLocation("starfall", "model/" + name);
    }

    private static @NotNull ResourceLocation particle(@NotNull String name) {
        return new ResourceLocation("starfall", "particle/" + name);
    }

    private static @NotNull ResourceLocation light(@NotNull String name) {
        return new ResourceLocation("starfall", "light/" + name);
    }

    private static @NotNull ResourceLocation ui(@NotNull String name) {
        return new ResourceLocation("starfall", "ui/" + name);
    }
}
