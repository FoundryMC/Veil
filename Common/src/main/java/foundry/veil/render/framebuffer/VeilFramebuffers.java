package foundry.veil.render.framebuffer;

import foundry.veil.Veil;
import net.minecraft.resources.ResourceLocation;

/**
 * Default framebuffer names for use with {@link FramebufferManager#getFramebuffer(ResourceLocation)}.
 *
 * @author Ocelot
 */
public class VeilFramebuffers {

    private VeilFramebuffers() {
    }

    public static final ResourceLocation MAIN = new ResourceLocation("main");
    public static final ResourceLocation FIRST_PERSON = buffer("first_person");
    public static final ResourceLocation TRANSPARENT = buffer("transparent");
    public static final ResourceLocation LIGHT = buffer("light");
    public static final ResourceLocation DEFERRED = buffer("deferred");
    public static final ResourceLocation POST = buffer("post");

    private static ResourceLocation buffer(String name) {
        return new ResourceLocation(Veil.MODID, name);
    }
}
