package foundry.veil.mixin.client.deferred;

import com.google.common.collect.ImmutableList;
import foundry.veil.ext.CompositeStateExtension;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RenderType.CompositeState.class)
public class CompositeStateMixin implements CompositeStateExtension {

    @Shadow
    ImmutableList<RenderStateShard> states;

    @Override
    public void veil$addShard(RenderStateShard shards) {
        ImmutableList.Builder<RenderStateShard> builder = new ImmutableList.Builder<>();
        builder.addAll(this.states);
        builder.add(shards);
        this.states = builder.build();
    }
}
