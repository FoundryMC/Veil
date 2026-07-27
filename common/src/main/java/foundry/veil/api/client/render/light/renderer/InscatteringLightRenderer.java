package foundry.veil.api.client.render.light.renderer;

import foundry.veil.api.client.render.light.data.LightData;

/**
 * Renders in-scattering for deferred lights.
 *
 * @author Neddslayer
 * @since 4.4.0
 */
public interface InscatteringLightRenderer<T extends LightData> extends LightTypeRenderer<T> {

    /**
     * Renders inscattering of all prepared lights with this renderer.
     * <br>
     * Shaders, custom uniforms, and the way lights are rendered is up to the individual renderer.
     * This function is not called for the first-person perspective.
     *
     * @param lightRenderer The light renderer instance
     */
    void renderLightInscattering(LightRenderer lightRenderer);
}
