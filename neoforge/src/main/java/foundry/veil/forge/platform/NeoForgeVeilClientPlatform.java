package foundry.veil.forge.platform;

import foundry.veil.api.client.render.dynamicbuffer.DynamicBuffersChange;
import foundry.veil.api.client.render.post.PostPipeline;
import foundry.veil.api.client.render.shader.ShaderManager;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import foundry.veil.api.event.VeilAddShaderPreProcessorsEvent;
import foundry.veil.api.event.VeilRegisterGlobalControllersEvent;
import foundry.veil.forge.event.*;
import foundry.veil.platform.VeilClientPlatform;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;

@ApiStatus.Internal
public class NeoForgeVeilClientPlatform implements VeilClientPlatform {

    @Override
    public void preVeilPostProcessing(ResourceLocation name, PostPipeline pipeline, PostPipeline.Context context) {
        NeoForge.EVENT_BUS.post(new ForgeVeilPostProcessingEvent.Pre(name, pipeline, context));
    }

    @Override
    public void postVeilPostProcessing(ResourceLocation name, PostPipeline pipeline, PostPipeline.Context context) {
        NeoForge.EVENT_BUS.post(new ForgeVeilPostProcessingEvent.Post(name, pipeline, context));
    }

    @Override
    public void onRegisterShaderPreProcessors(ResourceProvider resourceProvider, VeilAddShaderPreProcessorsEvent.Registry registry) {
        ModLoader.postEvent(new ForgeVeilAddShaderProcessorsEvent(resourceProvider, registry));
    }

    @Override
    public void onVeilCompileShaders(ShaderManager shaderManager, Map<ResourceLocation, ShaderProgram> updatedPrograms) {
        ModLoader.postEvent(new ForgeVeilShaderCompileEvent(shaderManager, updatedPrograms));
    }

    @Override
    public void onVeilDynamicBuffersChanged(DynamicBuffersChange change) {
        ModLoader.postEvent(new ForgeVeilDynamicBuffersChangedEvent(change));
    }

    @Override
    public void onRegisterGlobalControllers(VeilRegisterGlobalControllersEvent.Registry registry) {
        ModLoader.postEvent(new ForgeVeilRegisterGlobalControllersEvent(registry));
    }
}
