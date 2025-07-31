package foundry.veil.api.client.registry;

import foundry.veil.Veil;
import foundry.veil.api.client.render.light.data.AreaLightData;
import foundry.veil.api.client.render.light.data.DirectionalLightData;
import foundry.veil.api.client.render.light.data.LightData;
import foundry.veil.api.client.render.light.data.PointLightData;
import foundry.veil.api.client.render.light.renderer.LightTypeRenderer;
import foundry.veil.impl.client.editor.LightInspector;
import foundry.veil.impl.client.render.light.AreaLightRenderer;
import foundry.veil.impl.client.render.light.DirectionalLightRenderer;
import foundry.veil.impl.client.render.light.InstancedPointLightRenderer;
import foundry.veil.platform.registry.RegistrationProvider;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Registry for all light types.
 */
public final class LightTypeRegistry {

    public static final ResourceKey<Registry<LightType<?>>> REGISTRY_KEY = ResourceKey.createRegistryKey(Veil.veilPath("light_type"));
    private static final RegistrationProvider<LightType<?>> PROVIDER = RegistrationProvider.get(REGISTRY_KEY, Veil.MODID);
    public static final Registry<LightType<?>> REGISTRY = PROVIDER.asVanillaRegistry();

    public static final Supplier<LightType<DirectionalLightData>> DIRECTIONAL = register("directional", DirectionalLightRenderer::new, (level, camera) -> new DirectionalLightData().setTo(camera).setDirection(0, -1, 0));
    public static final Supplier<LightType<PointLightData>> POINT = register("point", () -> {
//        boolean supported = VeilRenderSystem.multiDrawIndirectSupported();
//        if (supported) {
//            Veil.LOGGER.info("Using Indirect Point Light Renderer");
//            return new IndirectPointLightRenderer();
//        } else {
//            Veil.LOGGER.info("Using Instanced Point Light Renderer");
//            return new InstancedPointLightRenderer();
//        }
        return new InstancedPointLightRenderer();
    }, (level, camera) -> new PointLightData().setTo(camera).setRadius(15.0F));
    public static final Supplier<LightType<AreaLightData>> AREA = register("area", AreaLightRenderer::new, (level, camera) -> new AreaLightData().setDistance(15.0F).setTo(camera));

    private LightTypeRegistry() {
    }

    @ApiStatus.Internal
    public static void bootstrap() {
    }

    private static <T extends LightData> Supplier<LightType<T>> register(String name, RendererFactory<T> factory, @Nullable DebugLightFactory debugFactory) {
        return PROVIDER.register(name, () -> new LightType<>(factory, debugFactory));
    }

    public record LightType<T extends LightData>(RendererFactory<T> rendererFactory,
                                                 @Nullable DebugLightFactory debugLightFactory) {
    }

    /**
     * Creates the renderer for lights when requested.
     *
     * @param <T> The type of light the renderer needs to draw
     */
    @FunctionalInterface
    public interface RendererFactory<T extends LightData> {

        /**
         * @return A new renderer for lights
         */
        LightTypeRenderer<T> createRenderer();
    }

    /**
     * Creates debug lights for the {@link LightInspector}.
     */
    @FunctionalInterface
    public interface DebugLightFactory {

        /**
         * Creates a new light in the level.
         *
         * @param level  The level the light is in
         * @param camera The camera the light is being spawned at
         * @return The new light created
         */
        LightData createDebugLight(ClientLevel level, Camera camera);
    }
}
