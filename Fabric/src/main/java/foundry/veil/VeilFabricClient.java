package foundry.veil;

import foundry.veil.render.ui.VeilUITooltipRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

public class VeilFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        VeilClient.init();
        HudRenderCallback.EVENT.register((matrices, tickDelta) -> {
            VeilUITooltipRenderer.OVERLAY.render(Minecraft.getInstance().gui, matrices, tickDelta, Minecraft.getInstance().getWindow().getGuiScaledWidth(), Minecraft.getInstance().getWindow().getGuiScaledHeight());
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null)
                return;
            VeilClient.tickClient(client.player.tickCount, client.getFrameTime());
        });

        // Register test resource pack
        FabricLoader loader = FabricLoader.getInstance();
        if (loader.isDevelopmentEnvironment()) {
            ResourceManagerHelper.registerBuiltinResourcePack(new ResourceLocation("veil", "test_shaders"), loader.getModContainer("veil").orElseThrow(), ResourcePackActivationType.DEFAULT_ENABLED);
        }
    }
}
