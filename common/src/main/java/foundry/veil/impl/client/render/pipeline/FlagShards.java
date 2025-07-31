package foundry.veil.impl.client.render.pipeline;

import foundry.veil.Veil;
import net.minecraft.client.renderer.RenderStateShard;
import org.jetbrains.annotations.ApiStatus;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.GL_MULTISAMPLE;
import static org.lwjgl.opengl.GL32C.GL_DEPTH_CLAMP;
import static org.lwjgl.opengl.GL32C.GL_TEXTURE_CUBE_MAP_SEAMLESS;

@ApiStatus.Internal
public class FlagShards extends RenderStateShard {

    public static final FlagShards DEPTH_CLAMP = new FlagShards("depth_clamp", GL_DEPTH_CLAMP);
    public static final FlagShards DITHER = new FlagShards("dither", GL_DITHER);
    public static final FlagShards LINE_SMOOTH = new FlagShards("line_smooth", GL_LINE_SMOOTH);
    public static final FlagShards MULTISAMPLE = new FlagShards("multisample", GL_MULTISAMPLE);
    public static final FlagShards TEXTURE_CUBE_MAP_SEAMLESS = new FlagShards("cube_map_seamless", GL_TEXTURE_CUBE_MAP_SEAMLESS);

    private FlagShards(String name, int flag) {
        super(Veil.MODID + ":" + name, () -> glEnable(flag), () -> glDisable(flag));
    }
}
