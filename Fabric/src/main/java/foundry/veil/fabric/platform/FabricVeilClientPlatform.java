package foundry.veil.fabric.platform;

import foundry.veil.api.client.render.post.PostPipeline;
import foundry.veil.fabric.event.FabricVeilPostProcessingEvent;
import foundry.veil.platform.services.VeilClientPlatform;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class FabricVeilClientPlatform implements VeilClientPlatform {

    @Override
    public void preVeilPostProcessing(ResourceLocation name, PostPipeline pipeline, PostPipeline.Context context) {
        FabricVeilPostProcessingEvent.PRE.invoker().preVeilPostProcessing(name, pipeline, context);
    }

    @Override
    public void postVeilPostProcessing(ResourceLocation name, PostPipeline pipeline, PostPipeline.Context context) {
        FabricVeilPostProcessingEvent.POST.invoker().postVeilPostProcessing(name, pipeline, context);
    }
}
