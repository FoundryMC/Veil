package foundry.veil.mixin.client.deferred;

import com.mojang.blaze3d.vertex.BufferBuilder;
import foundry.veil.ext.BufferSourceExtension;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(MultiBufferSource.BufferSource.class)
public class BufferSourceMixin implements BufferSourceExtension {

    @Shadow
    @Final
    protected Map<RenderType, BufferBuilder> fixedBuffers;

    @Override
    public void veil$addFixedBuffer(RenderType renderType) {
        this.fixedBuffers.put(renderType, new BufferBuilder(renderType.bufferSize()));
    }
}
