package foundry.veil.ext;

import net.minecraft.client.renderer.RenderStateShard;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;

@ApiStatus.Internal
public interface CompositeStateExtension {

    void veil$addShards(Collection<RenderStateShard> shards);
}
