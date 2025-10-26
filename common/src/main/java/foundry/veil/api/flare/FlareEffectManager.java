package foundry.veil.api.flare;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.flare.data.effect.FlareEffectTemplate;
import foundry.veil.api.flare.data.effect.FlareModel;
import foundry.veil.api.flare.data.effect.FlareModule;
import net.minecraft.resources.ResourceLocation;
import foundry.veil.api.flare.modifier.ControllerManager;
import foundry.veil.api.flare.model.BakedShell;
import foundry.veil.api.flare.model.UnbakedShell;
import foundry.veil.impl.flare.FlareManager;
import foundry.veil.impl.flare.ShellManager;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.system.NativeResource;

public final class FlareEffectManager implements NativeResource {

    private final ShellManager shellManager;
    private final ControllerManager controllerManager;

    @ApiStatus.Internal
    public FlareEffectManager() {
        shellManager = new ShellManager();
        controllerManager = new ControllerManager();
    }

    public static FlareEffectManager getInstance() {
        return VeilRenderSystem.renderer().getEffectManager();
    }

    public static FlareEffectTemplate getTemplate(ResourceLocation resourceLocation) {
        return FlareManager.registryAccess().registry(FlareManager.EFFECT_TEMPLATES).orElseThrow().get(resourceLocation);
    }

    public static FlareModule getModule(ResourceLocation resourceLocation) {
        return FlareManager.registryAccess().registry(FlareManager.EFFECT_MODULES).orElseThrow().get(resourceLocation);
    }

    public ShellManager getShellManager() {
        return shellManager;
    }

    public BakedShell getBakedShell(ResourceLocation modelLocation) {
        return shellManager.getBakedShell(modelLocation);
    }

    public UnbakedShell getUnbakedShell(ResourceLocation modelLocation) {
        return shellManager.getUnbakedShell(modelLocation);
    }

    public ControllerManager getControllerManager() {
        return controllerManager;
    }

    @Override
    public void free() {
        FlareModel.VAO.get().getVertexArray().free();
    }
}
