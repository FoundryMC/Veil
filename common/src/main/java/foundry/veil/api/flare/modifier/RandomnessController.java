package foundry.veil.api.flare.modifier;

import net.minecraft.util.RandomSource;

/**
 * Controller with a random value each time {@link Controller#getValue()} is called.
 *
 * @author GuyApooye
 */
public class RandomnessController extends GlobalController {
    public static final RandomnessController INSTANCE = new RandomnessController("random");

    private final RandomSource randomSource = RandomSource.create(10840L);

    private RandomnessController(String name) {
        super(name);
    }

    @Override
    protected float getUpdatedValue() {
        return value;
    }

    @Override
    public float getValue() {
        return value = randomSource.nextFloat();
    }
}
