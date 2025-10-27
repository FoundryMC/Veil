package foundry.veil.api.client.property;

import foundry.veil.api.flare.data.effect.FlareMaterial;
import foundry.veil.api.flare.data.effect.FlareModel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Specifies that the property is modified before, and reset after the {@link FlareModel} and its {@link FlareMaterial}s have been rendered.
 *
 * @author GuyApooye
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ModelProperty {
}
