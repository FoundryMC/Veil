package foundry.veil.api.flare;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.flare.data.effect.FlareEffectTemplate;
import foundry.veil.api.flare.data.effect.FlareModule;
import foundry.veil.api.flare.model.BakedShell;
import foundry.veil.api.flare.modifier.ControllerManager;
import foundry.veil.impl.flare.FlareManager;
import foundry.veil.impl.flare.ShellManager;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

/**
 * @since 2.5.0
 */
public final class FlareEffectManager {

    private final ShellManager shellManager;
    private final ControllerManager controllerManager;

    @ApiStatus.Internal
    public FlareEffectManager() {
        this.shellManager = new ShellManager();
        this.controllerManager = new ControllerManager();
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

    @ApiStatus.Internal
    public ShellManager getShellManager() {
        return this.shellManager;
    }

    public BakedShell getBakedShell(ResourceLocation modelLocation) {
        return this.shellManager.getBakedShell(modelLocation);
    }

    public ControllerManager getControllerManager() {
        return this.controllerManager;
    }
}
