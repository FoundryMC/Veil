package foundry.veil.ext;

import net.minecraft.client.renderer.RenderStateShard;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface CompositeStateExtension {

    void veil$addShard(RenderStateShard shard);
}
