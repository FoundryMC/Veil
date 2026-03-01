package foundry.veil.api.client.render.dynamicbuffer;

import org.jetbrains.annotations.ApiStatus;

/**
 * A change in the currently active dynamic buffers
 *
 * @param previouslyEnabledBuffersMask A mask representing the enabled state of every dynamic buffer
 * @param enabledBuffersMask           A mask representing the previous enabled state of every dynamic buffer
 * @author RyanH
 * @since 2.3.0
 */
public record DynamicBuffersChange(int previouslyEnabledBuffersMask, int enabledBuffersMask) {

    @ApiStatus.Internal
    public DynamicBuffersChange {
    }

    /**
     * @return a new array containing all buffers that were previously enabled
     */
    public DynamicBufferType[] getPreviouslyEnabledBuffers() {
        return DynamicBufferType.decode(this.previouslyEnabledBuffersMask);
    }

    /**
     * Creates a new array containing all dynamic buffers that are now enabled.
     * <br>
     * The returned values are in the correct attachment order starting at attachment 1.
     * Attachment 0 will always be the regular vanilla minecraft framebuffer attachment.
     *
     * @return a new array containing all dynamic buffers that are now enabled
     */
    public DynamicBufferType[] getEnabledBuffers() {
        return DynamicBufferType.decode(this.enabledBuffersMask);
    }

    /**
     * @return a mask representing the enabled state of every dynamic buffer
     * @deprecated Use {@link #enabledBuffersMask()} instead
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "4.0.0")
    @Deprecated
    public int getPreviouslyEnabledBuffersMask() {
        return this.previouslyEnabledBuffersMask;
    }

    /**
     * @return a mask representing the previous enabled state of every dynamic buffer
     * @deprecated Use {@link #enabledBuffersMask()} instead
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "4.0.0")
    @Deprecated
    public int getEnabledBuffersMask() {
        return this.enabledBuffersMask;
    }

    /**
     * Checks if a specific {@link DynamicBufferType} changed enabled status
     *
     * @param buffer the buffer type to check
     * @return if the buffer was enabled / disabled
     */
    public boolean hasChanged(final DynamicBufferType buffer) {
        final int mask = buffer.getMask();
        return ((this.enabledBuffersMask & mask) ^ (this.previouslyEnabledBuffersMask & mask)) != 0;
    }

    /**
     * Checks if a specific {@link DynamicBufferType} is enabled in the new state
     *
     * @param buffer the buffer type to check
     * @return if the buffer is now enabled
     */
    public boolean isEnabled(final DynamicBufferType buffer) {
        return (this.enabledBuffersMask & buffer.getMask()) != 0;
    }

    /**
     * Checks if a specific {@link DynamicBufferType} was enabled in the previous state
     *
     * @param buffer the buffer type to check
     * @return if the buffer was previously enabled
     */
    public boolean wasPreviouslyEnabled(final DynamicBufferType buffer) {
        return (this.previouslyEnabledBuffersMask & buffer.getMask()) != 0;
    }
}