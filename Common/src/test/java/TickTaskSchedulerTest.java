import foundry.veil.impl.TickTaskSchedulerImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class TickTaskSchedulerTest {

    @Test
    public void testExecute() {
        TickTaskSchedulerImpl scheduler = new TickTaskSchedulerImpl();
        scheduler.schedule(() -> System.out.println("Fourth"), 10);
        scheduler.schedule(() -> System.out.println("First"), 2);
        scheduler.schedule(() -> System.out.println("Third"), 8);
        scheduler.schedule(() -> System.out.println("Second"), 8);
        CompletableFuture<?> cancelled = scheduler.schedule(() -> System.out.println("Cancelled"), 1);
        cancelled.cancel(false);
        scheduler.shutdown();
    }

    @Test
    public void testFixedRate() {
        TickTaskSchedulerImpl scheduler = new TickTaskSchedulerImpl();
        AtomicInteger ticks = new AtomicInteger();
        scheduler.scheduleAtFixedRate(ticks::incrementAndGet, 0, 2);
        for (int i = 0; i < 99; i++) {
            scheduler.run();
        }
        scheduler.shutdown();
        Assertions.assertEquals(51, ticks.get());
    }
}
