package foundry.veil.neoforge.platform;

import foundry.veil.api.client.render.post.PostPipeline;
import foundry.veil.neoforge.event.NeoForgeVeilPostProcessingEvent;
import foundry.veil.platform.services.VeilClientPlatform;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class NeoForgeVeilClientPlatform implements VeilClientPlatform {

    @Override
    public void preVeilPostProcessing(ResourceLocation name, PostPipeline pipeline, PostPipeline.Context context) {
        NeoForge.EVENT_BUS.post(new NeoForgeVeilPostProcessingEvent.Pre(name, pipeline, context));
    }

    @Override
    public void postVeilPostProcessing(ResourceLocation name, PostPipeline pipeline, PostPipeline.Context context) {
        NeoForge.EVENT_BUS.post(new NeoForgeVeilPostProcessingEvent.Post(name, pipeline, context));
    }
}
