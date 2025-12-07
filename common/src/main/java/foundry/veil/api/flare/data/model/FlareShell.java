package foundry.veil.api.flare.data.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.veil.api.flare.model.BakedShell;
import foundry.veil.api.flare.model.ShellBakery;
import foundry.veil.api.flare.model.UnbakedShell;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Textureless model, may contain texture coordinate data.
 *
 * @author GuyApooye
 * @since 2.5.0
 */
public final class FlareShell implements UnbakedShell {
    
    public static final Codec<FlareShell> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ShellElement.CODEC.listOf().fieldOf("elements").forGetter(FlareShell::elements)
    ).apply(instance, FlareShell::new));
    private final List<ShellElement> elements;
    
    public FlareShell(List<ShellElement> elements) {
        this.elements = List.copyOf(elements);
    }
    
    @Override
    public @Nullable BakedShell bake() {
        SimpleBakedShell.Builder builder = new SimpleBakedShell.Builder();
        for (ShellElement element : this.elements) {
            for (Map.Entry<Direction, ShellElementFace> entry : element.faces().entrySet()) {
                builder.addFace(ShellBakery.bakeQuad(element, entry.getValue(), entry.getKey()));
            }
        }
        
        return builder.build();
    }
    
    @Override
    public List<ShellElement> elements() {
        return new ArrayList<>(this.elements);
    }
    
}
