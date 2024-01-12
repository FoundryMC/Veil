package foundry.veil.fabric;

import foundry.veil.Veil;
import foundry.veil.VeilClient;
import foundry.veil.render.VeilVanillaShaders;
import foundry.veil.render.deferred.VeilDeferredRenderer;
import foundry.veil.render.ui.VeilUITooltipRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class VeilFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        VeilClient.init();
        HudRenderCallback.EVENT.register((matrices, tickDelta) -> {
            VeilUITooltipRenderer.OVERLAY.render(Minecraft.getInstance().gui, matrices, tickDelta, Minecraft.getInstance().getWindow().getGuiScaledWidth(), Minecraft.getInstance().getWindow().getGuiScaledHeight());
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> VeilClient.tickClient(client.getFrameTime()));

        KeyBindingHelper.registerKeyBinding(VeilClient.EDITOR_KEY);

        // Register test resource pack
        FabricLoader loader = FabricLoader.getInstance();
        if (Veil.DEBUG && loader.isDevelopmentEnvironment()) {
            ResourceManagerHelper.registerBuiltinResourcePack(Veil.veilPath("test_shaders"), loader.getModContainer(Veil.MODID).orElseThrow(), ResourcePackActivationType.NORMAL);
        }
        ResourceManagerHelper.registerBuiltinResourcePack(VeilDeferredRenderer.PACK_ID, loader.getModContainer(Veil.MODID).orElseThrow(), ResourcePackActivationType.DEFAULT_ENABLED);

        CoreShaderRegistrationCallback.EVENT.register(context -> VeilVanillaShaders.registerShaders(context::register));
    }
}
