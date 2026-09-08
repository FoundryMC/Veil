package foundry.veil.api.screenshake.type;

import foundry.veil.api.molang.VeilMolang;
import gg.moonflower.molangcompiler.api.MolangExpression;
import gg.moonflower.molangcompiler.api.MolangRuntime;
import gg.moonflower.molangcompiler.api.exception.MolangSyntaxException;
import net.minecraft.util.RandomSource;

import java.util.function.Supplier;

/**
 * A screen shake that affects the player regardless of distance.
 *
 * @author Neddslayer
 * @since 4.5.0
 */
public class GlobalScreenShake extends ScreenShakeType {

    private final MolangExpression expression;
    private final Supplier<MolangRuntime> environment;

    public GlobalScreenShake(String strengthExpression, int length) throws MolangSyntaxException {
        this(VeilMolang.get().compile(strengthExpression), length);
    }

    public GlobalScreenShake(MolangExpression strengthExpression, int length) {
        super(RandomSource.create(), length);
        this.expression = strengthExpression;
        this.environment = () -> MolangRuntime.runtime()
                .setQuery("age", this::age)
                .setQuery("agePercent", () -> this.age() / this.length())
                .setQuery("length", this::length)
                .create();
    }

    @Override
    protected float getStrength() {
        return this.environment.get().safeResolve(this.expression);
    }
}
