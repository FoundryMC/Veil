package foundry.veil.api.flare;

import foundry.veil.api.client.render.MatrixStack;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.event.VeilRenderLevelStageEvent;
import foundry.veil.api.flare.data.effect.FlareEffectTemplate;
import foundry.veil.api.flare.data.effect.FlareModel;
import foundry.veil.api.flare.data.effect.FlareModule;
import foundry.veil.platform.VeilEventPlatform;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.resources.ResourceLocation;
import foundry.veil.api.flare.modifier.ControllerManager;
import foundry.veil.api.flare.model.BakedShell;
import foundry.veil.api.flare.model.UnbakedShell;
import foundry.veil.api.flare.modifier.RandomnessController;
import foundry.veil.impl.flare.FlareManager;
import foundry.veil.impl.flare.ShellManager;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix4fc;
import org.lwjgl.system.NativeResource;

public final class FlareEffectManager implements NativeResource {

    private final ShellManager shellManager;
    private final ControllerManager controllerManager;
    private final FlareManager flareManager;

    @ApiStatus.Internal
    public FlareEffectManager() {
        shellManager = new ShellManager();
        flareManager = new FlareManager();
        controllerManager = new ControllerManager();
        controllerManager.addController(RandomnessController.INSTANCE);
    }

    public ShellManager getShellManager() {
        return shellManager;
    }

    public static FlareEffectManager getInstance() {
        return VeilRenderSystem.renderer().getEffectManager();
    }

    public FlareEffectTemplate getTemplate(ResourceLocation resourceLocation) {
        return flareManager.getTemplate(resourceLocation);
    }

    public FlareModule getModule(ResourceLocation resourceLocation) {
        return flareManager.getModule(resourceLocation);
    }

    public BakedShell getBakedShell(ResourceLocation modelLocation) {
        return getInstance().getShellManager().getBakedShell(modelLocation);
    }

    public UnbakedShell getUnbakedShell(ResourceLocation modelLocation) {
        return getInstance().getShellManager().getUnbakedShell(modelLocation);
    }

    public ControllerManager getControllerManager() {
        return controllerManager;
    }

    @Override
    public void free() {
        FlareModel.VAO.get().getVertexArray().free();
    }
}
