package foundry.veil.api.flare.model;

import foundry.veil.api.flare.data.model.ShellElement;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface UnbakedShell {
    @Nullable
    BakedShell bake();

    void setLocation(ResourceLocation location);

    @Nullable
    ResourceLocation getLocation();

    List<ShellElement> getElements();
}
