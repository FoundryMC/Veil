package foundry.veil.api.client.property;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Specifies that the property <b>may not</b> be applied!
 *
 * @author GuyApooye
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface InapplicableProperty {
}
