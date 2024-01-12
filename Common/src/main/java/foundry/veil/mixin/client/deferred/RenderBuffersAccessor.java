package foundry.veil.mixin.client.deferred;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.SortedMap;

@Mixin(RenderBuffers.class)
public interface RenderBuffersAccessor {

    @Accessor
    SortedMap<RenderType, BufferBuilder> getFixedBuffers();
}
