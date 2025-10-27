package foundry.veil.mixin.pipeline.client;

import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.ext.AutoStorageIndexBufferExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RenderSystem.AutoStorageIndexBuffer.class)
public abstract class PipelineAutoStorageIndexBufferMixin implements AutoStorageIndexBufferExtension {

    @Shadow
    private int name;

    @Shadow
    public abstract boolean hasStorage(int index);

    @Shadow
    public abstract void bind(int index);

    @Override
    public void veil$ensureStorage(int neededIndexCount) {
        if (this.name == 0 || !this.hasStorage(neededIndexCount)) {
            this.bind(neededIndexCount);
        }
    }

    @Override
    public int veil$getBuffer() {
        return this.name;
    }
}
