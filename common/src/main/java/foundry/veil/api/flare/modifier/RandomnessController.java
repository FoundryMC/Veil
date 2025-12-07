package foundry.veil.api.flare.modifier;

import net.minecraft.util.RandomSource;

/**
 * Controller with a random value each time {@link HostBoundController#getValue()} is called.
 *
 * @author GuyApooye
 * @since 2.5.0
 */
public class RandomnessController extends GlobalController {

    public static final RandomnessController INSTANCE = new RandomnessController("random");

    private final RandomSource randomSource = RandomSource.create(10840L);

    private RandomnessController(String name) {
        super(name);
    }

    @Override
    public float getUpdatedValue() {
        return this.randomSource.nextFloat();
    }
}
