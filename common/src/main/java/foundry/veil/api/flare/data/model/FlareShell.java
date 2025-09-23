package foundry.veil.api.flare.data.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.api.flare.model.BakedShell;
import foundry.veil.api.flare.model.ShellBakery;
import foundry.veil.api.flare.model.UnbakedShell;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FlareShell implements UnbakedShell {

    public static final Codec<FlareShell> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ShellElement.CODEC.listOf().fieldOf("elements").forGetter(FlareShell::getElements)
    ).apply(instance, FlareShell::new));

    @Nullable
    private ResourceLocation location = null;
    private final List<ShellElement> elements;

    public FlareShell(List<ShellElement> elements) {
        this.elements = elements;
    }

    @Override
    public @Nullable BakedShell bake() {
        return ShellBakery.FaceBakery.bake(this);
    }

    @Override
    public void setLocation(@Nullable ResourceLocation location) {
        this.location = location;
    }

    @Override
    public @Nullable ResourceLocation getLocation() {
        return location;
    }

    @Override
    public List<ShellElement> getElements() {
        return new ArrayList<>(elements);
    }

    @Override
    public String toString() {
        return location == null ? "" : location.toString();
    }
}
