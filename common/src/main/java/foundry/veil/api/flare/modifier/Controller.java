package foundry.veil.api.flare.modifier;

import foundry.veil.api.client.property.Property;
import foundry.veil.api.flare.data.FloatCurve;

/**
 * Controllers collect values from sources to be used to evaluate {@link FloatCurve FloatCurves} and modify {@link Property Properties}.
 *
 * @author GuyApooye
 * @since 2.5.2
 */
public sealed interface Controller permits GlobalController, HostBoundController {
    default void initialize() {}
    void update(float partialTick);
    float getUpdatedValue();
    float getValue();
    String getName();
}
