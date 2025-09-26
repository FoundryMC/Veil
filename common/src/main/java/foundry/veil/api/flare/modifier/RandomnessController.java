package foundry.veil.api.flare.modifier;

import net.minecraft.util.RandomSource;

/**
 * Controller with a random value each time {@link Controller#getValue()} is called.
 *
 * @author GuyApooye
 */
public class RandomnessController extends Controller {
    public static final RandomnessController INSTANCE = new RandomnessController("global::random");

    private final RandomSource randomSource = RandomSource.create(10840L);

    private RandomnessController(String name) {
        super(new ControllerIdentifier(name, "global"), null);
    }

    @Override
    public void update(float partialTick) {
    }

    @Override
    protected float getUpdatedValue() {
        return value;
    }

    @Override
    public float getValue() {
        value = randomSource.nextFloat();
        return value;
    }
}
