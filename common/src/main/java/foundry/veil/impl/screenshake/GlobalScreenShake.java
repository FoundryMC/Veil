package foundry.veil.impl.screenshake;

import com.google.common.base.Suppliers;
import foundry.veil.api.screenshake.type.ScreenShakeType;
import gg.moonflower.molangcompiler.api.MolangExpression;
import gg.moonflower.molangcompiler.api.MolangRuntime;
import net.minecraft.util.RandomSource;

import java.util.function.Supplier;

/**
 * A screen shake that affects the player regardless of distance.
 */
public class GlobalScreenShake extends ScreenShakeType {

    private final MolangExpression expression;
    private final Supplier<MolangRuntime> environment;

    public GlobalScreenShake(MolangExpression strengthExpression, int length) {
        super(RandomSource.create(), length);
        this.expression = strengthExpression;

        this.environment = Suppliers.memoize(() -> MolangRuntime.runtime()
                .setQuery("age", () -> (float) (this.length - this.ticksRemaining))
                .setQuery("agePercent", () -> (float) (this.ticksRemaining) / this.length)
                .setQuery("length", this.length)
                .create());
    }

    @Override
    protected float getStrength() {
        MolangRuntime runtime = this.environment.get();
        return runtime.safeResolve(this.expression);
    }
}
