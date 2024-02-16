package foundry.veil.api.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import foundry.veil.mixin.client.pipeline.BufferBuilderAccessor;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeResource;

import java.util.*;

public class CachedBufferSource implements MultiBufferSource, NativeResource {

    private final Map<RenderType, BufferBuilder> buffers = new Object2ObjectArrayMap<>();
    private final Set<BufferBuilder> startedBuffers = new HashSet<>();
    private Optional<RenderType> lastState = Optional.empty();

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        // Make sure buffers that can't be batched are ended correctly
        if (this.lastState.isPresent() && !this.lastState.get().canConsolidateConsecutiveGeometry()) {
            this.endLastBatch();
        }
        this.lastState = renderType.asOptional();

        BufferBuilder builder = this.buffers.computeIfAbsent(renderType, t -> new BufferBuilder(t.bufferSize()));
        if (this.startedBuffers.add(builder)) {
            builder.begin(renderType.mode(), renderType.format());
        }

        return builder;
    }

    @Override
    public void free() {
        // Make sure we free the memory before trying to dispose of the objects
        for (BufferBuilder value : this.buffers.values()) {
            MemoryUtil.memFree(((BufferBuilderAccessor) value).getBuffer());
        }
        this.buffers.clear();
        this.startedBuffers.clear();
        this.lastState = Optional.empty();
    }

    public void endLastBatch() {
        this.lastState.ifPresent(this::endBatch);
        this.lastState = Optional.empty();
    }

    public void endBatch() {
        for (RenderType renderType : this.buffers.keySet()) {
            this.endBatch(renderType);
        }
    }

    public void endBatch(RenderType renderType) {
        BufferBuilder builder = this.buffers.get(renderType);
        if (builder != null && this.startedBuffers.remove(builder)) {
            renderType.end(builder, RenderSystem.getVertexSorting());
            if (Objects.equals(this.lastState, renderType.asOptional())) {
                this.lastState = Optional.empty();
            }
        }
    }
}
