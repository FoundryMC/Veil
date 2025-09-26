package foundry.veil.fabric.platform;

import foundry.veil.api.client.render.dynamicbuffer.DynamicBuffersChange;
import foundry.veil.api.client.render.post.PostPipeline;
import foundry.veil.api.client.render.shader.ShaderManager;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import foundry.veil.api.event.VeilAddShaderPreProcessorsEvent;
import foundry.veil.api.event.VeilRegisterGlobalControllersEvent;
import foundry.veil.fabric.event.*;
import foundry.veil.platform.VeilClientPlatform;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;

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

    @Override
    public void onRegisterShaderPreProcessors(ResourceProvider resourceProvider, VeilAddShaderPreProcessorsEvent.Registry registry) {
        FabricVeilAddShaderPreProcessorsEvent.EVENT.invoker().onRegisterShaderPreProcessors(resourceProvider, registry);
    }

    @Override
    public void onVeilCompileShaders(ShaderManager shaderManager, Map<ResourceLocation, ShaderProgram> updatedPrograms) {
        FabricVeilShaderCompileEvent.EVENT.invoker().onVeilCompileShaders(shaderManager, updatedPrograms);
    }

    @Override
    public void onVeilDynamicBuffersChanged(DynamicBuffersChange change) {
        FabricVeilDynamicBuffersChangedEvent.EVENT.invoker().onVeilDynamicBuffersChanged(change);
    }

    @Override
    public void onRegisterGlobalControllers(VeilRegisterGlobalControllersEvent.Registry registry) {
        FabricVeilRegisterGlobalControllersEvent.EVENT.invoker().onRegisterGlobalControllers(registry);
    }
}
