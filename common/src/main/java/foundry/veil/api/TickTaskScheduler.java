package foundry.veil.api;

import foundry.veil.ext.MinecraftServerExtension;
import foundry.veil.impl.client.VeilClientSchedulerImpl;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.*;

/**
 * Schedules tasks to be run on future ticks.
 *
 * @author Ocelot
 * @since 1.0.0
 */
public interface TickTaskScheduler extends Executor {

    /**
     * Retrieves the tick task scheduler for the specified server or client.
     *
     * @param server The server to get the scheduler for or <code>null</code> for the client scheduler
     * @return The scheduler for that server
     */
    static TickTaskScheduler get(@Nullable MinecraftServer server) {
        return server == null ? VeilClientSchedulerImpl.getScheduler() : ((MinecraftServerExtension) server).veil$getOrCreateScheduler();
    }

    /**
     * Executes the specified command on the next particle system tick.
     *
     * @param command The runnable task
     * @throws RejectedExecutionException if the task cannot be scheduled for execution
     * @throws NullPointerException       if command is null
     */
    @Override
    void execute(@NotNull Runnable command);

    /**
     * Schedules the specified command to run in the specified number of ticks.
     *
     * @param command The runnable task
     * @param delay   The delay in ticks
     * @return A future that completes after the task has been run
     * @throws RejectedExecutionException if the task cannot be scheduled for execution
     * @throws NullPointerException       if command is null
     * @throws IllegalArgumentException   if delay less than or equal to zero
     */
    TickTask<?> schedule(@NotNull Runnable command, long delay);

    /**
     * Schedules the specified command to run in the specified number of ticks.
     *
     * @param callable The callable task
     * @param delay    The delay in ticks
     * @return A future that completes after the task has been run
     * @throws RejectedExecutionException if the task cannot be scheduled for execution
     * @throws NullPointerException       if command is null
     * @throws IllegalArgumentException   if delay less than or equal to zero
     */
    <V> TickTask<V> schedule(@NotNull Callable<V> callable, long delay);

    /**
     * Schedules the specified command to run after the specified initial delay in ticks and at each fixed time interval in ticks.
     *
     * @param command      The runnable task
     * @param initialDelay The initial delay in ticks
     * @param period       The period between task executions
     * @return A future that completes if there was an error
     * @throws RejectedExecutionException if the task cannot be scheduled for execution
     * @throws NullPointerException       if command is null
     * @throws IllegalArgumentException   if delay less than or equal to zero
     */
    TickTask<?> scheduleAtFixedRate(@NotNull Runnable command, long initialDelay, long period);

    /**
     * @return Whether the executor has shut down and will reject
     */
    boolean isShutdown();

    /**
     * A single task scheduled to run in the future.
     */
    interface TickTask<V> extends ScheduledFuture<V> {

        /**
         * @return The number of ticks until this task will run
         */
        long getDelay();

        /**
         * @return This task represented as a future
         */
        CompletableFuture<V> toCompletableFuture();
    }
}
