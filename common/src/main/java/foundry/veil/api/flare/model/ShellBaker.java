package foundry.veil.api.flare.model;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public interface ShellBaker {
    UnbakedShell getShell(ResourceLocation location);

    @Nullable
    UnbakedShell getTopLevelShell(ModelResourceLocation location);

    BakedShell bake(ResourceLocation location);

    @Nullable
    BakedShell bakeUncached(UnbakedShell unbakedShell);
}
