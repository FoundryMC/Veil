package foundry.veil.api.flare.model;

import foundry.veil.api.flare.data.model.ShellElement;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @since 2.5.0
 */
public interface UnbakedShell {

    @Contract("->new")
    @Nullable
    BakedShell bake();

    @Contract("->new")
    List<ShellElement> elements();
}
