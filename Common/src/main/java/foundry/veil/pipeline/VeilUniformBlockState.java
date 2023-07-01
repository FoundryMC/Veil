package foundry.veil.pipeline;

import foundry.veil.render.shader.definition.ShaderBlock;
import foundry.veil.render.shader.definition.ShaderBlockImpl;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * Manages the state of uniform block bindings and their associated shader names.
 *
 * @author Ocelot
 */
@ApiStatus.Internal
public class VeilUniformBlockState {

    private final Map<ShaderBlockImpl<?>, Integer> boundBlocks;
    private final Map<Integer, CharSequence> shaderBindings;
    private final IntSet usedBindings;
    private int nextBinding;

    VeilUniformBlockState() {
        this.boundBlocks = new HashMap<>();
        this.shaderBindings = new HashMap<>();
        this.usedBindings = new IntOpenHashSet();
    }

    /**
     * Looks for a stale binding that can be replaced with a new one.
     */
    private void freeBinding() {
        Iterator<Map.Entry<ShaderBlockImpl<?>, Integer>> iterator = this.boundBlocks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<ShaderBlockImpl<?>, Integer> entry = iterator.next();
            int binding = entry.getValue();
            if (this.usedBindings.contains(binding)) {
                continue;
            }

            this.unbind(binding, entry.getKey());
            iterator.remove();

            this.nextBinding = binding;
            return;
        }

        throw new IllegalStateException("Too many shader blocks bound, failed to find empty space.");
    }

    /**
     * Binds the specified block and returns the used binding.
     *
     * @param block The block to bind
     * @return The binding used
     */
    public int bind(ShaderBlock<?> block) {
        if (!(block instanceof ShaderBlockImpl<?> impl)) {
            throw new UnsupportedOperationException("Cannot bind " + block.getClass());
        }

        int binding = this.boundBlocks.getOrDefault(block, -1);
        if (binding == -1) {
            if (this.nextBinding >= VeilRenderSystem.maxUniformBuffersBindings()) {
                this.freeBinding();
            }

            binding = this.nextBinding;
            this.boundBlocks.put(impl, binding);

            // Find the next open binding
            while (this.boundBlocks.containsValue(this.nextBinding)) {
                this.nextBinding++;
            }
        }

        impl.bind(binding);
        this.usedBindings.add(binding);
        return binding;
    }

    /**
     * Binds and assigns the bound index to all shaders under the specified name.
     *
     * @param name  The name of the block to bind in shader code
     * @param block The block to bind
     */
    public void bind(CharSequence name, ShaderBlock<?> block) {
        int binding = this.bind(block);
        CharSequence boundName = this.shaderBindings.get(binding);
        if (!Objects.equals(name, boundName)) {
            this.shaderBindings.put(binding, name);
            VeilRenderSystem.renderer().getShaderManager().setGlobal(shader -> shader.setUniformBlock(name, binding));
        }
    }

    /**
     * Unbinds the specified shader block.
     *
     * @param block The block to unbind
     */
    public void unbind(ShaderBlock<?> block) {
        if (!(block instanceof ShaderBlockImpl<?>)) {
            throw new UnsupportedOperationException("Cannot unbind " + block.getClass());
        }

        Iterator<Map.Entry<ShaderBlockImpl<?>, Integer>> iterator = this.boundBlocks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<ShaderBlockImpl<?>, Integer> entry = iterator.next();
            ShaderBlockImpl<?> impl = entry.getKey();
            if (!impl.equals(block)) {
                continue;
            }

            this.unbind(entry.getValue(), impl);
            iterator.remove();
        }
    }

    private void unbind(int binding, ShaderBlockImpl<?> block) {
        block.unbind(binding);

        CharSequence name = this.shaderBindings.remove(binding);
        if (name != null) {
            VeilRenderSystem.renderer().getShaderManager().setGlobal(shader -> shader.setUniformBlock(name, 0));
        }

        // Fill the gap since the spot is open now
        if (binding < this.nextBinding) {
            this.nextBinding = binding;
        }
    }

    /**
     * Forces all shader bindings to be re-uploaded the next time {@link #bind(CharSequence, ShaderBlock)} is called.
     */
    public void queueUpload() {
        this.shaderBindings.clear();
    }

    /**
     * Clears all used bindings from the current frame.
     */
    public void clear() {
        this.usedBindings.clear();
    }
}
