package foundry.veil.api.client.property;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Specifies that the property cannot be transformed.
 *
 * @author GuyApooye
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ImmutableProperty {
}
