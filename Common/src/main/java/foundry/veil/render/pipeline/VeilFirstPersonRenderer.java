package foundry.veil.render.pipeline;

import com.mojang.logging.LogUtils;
import foundry.veil.Veil;
import foundry.veil.render.framebuffer.AdvancedFbo;
import foundry.veil.render.framebuffer.VeilFramebuffers;
import foundry.veil.render.post.PostPipeline;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

/**
 * Handles drawing to and copying from the first person framebuffer.
 *
 * @author Ocelot
 */
@ApiStatus.Internal
public final class VeilFirstPersonRenderer {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation FIRST_PERSON = new ResourceLocation(Veil.MODID, "first_person");

    private static PostPipeline pipeline;
    private static boolean printedError;

    private VeilFirstPersonRenderer() {
    }

    public static void setup() {
        VeilRenderer renderer = VeilRenderSystem.renderer();
        AdvancedFbo buffer = VeilRenderSystem.renderer().getFramebufferManager().getFramebuffer(VeilFramebuffers.FIRST_PERSON);
        pipeline = renderer.getPostProcessingManager().getPipeline(FIRST_PERSON);

        if (pipeline == null || buffer == null) {
            if (!printedError) {
                LOGGER.warn("Failed to render first person with pipeline: {}, {}", FIRST_PERSON, VeilFramebuffers.FIRST_PERSON);
                printedError = true;
            }
            return;
        }

        buffer.bind(false);
        printedError = false;
    }

    public static void blit() {
        if (pipeline != null) {
            VeilRenderSystem.renderer().getPostProcessingManager().runPipeline(pipeline);
        }

        AdvancedFbo.unbind();
    }
}
