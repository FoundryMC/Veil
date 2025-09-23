package foundry.veil.api.flare.modifier;

import net.minecraft.util.RandomSource;

public class RandomnessController extends Controller {
    public static final RandomnessController INSTANCE = new RandomnessController("global::random");

    private final RandomSource randomSource = RandomSource.create(10840L);

    private RandomnessController(String name) {
        super(new ControllerIdentifier(name, "global"), null);
    }

    @Override
    public void update(float partialTick) {
        value = getUpdatedValue();
    }

    @Override
    protected float getUpdatedValue() {
        return randomSource.nextFloat();
    }
}
