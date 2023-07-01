package foundry.veil.molang;

import foundry.veil.Veil;
import gg.moonflower.molangcompiler.api.MolangCompiler;
import org.jetbrains.annotations.ApiStatus;

/**
 * Manages the Veil MoLang compiler.
 *
 * @author Ocelot
 */
public final class VeilMolang {

    private static final MolangCompiler COMPILER = MolangCompiler.create(MolangCompiler.DEFAULT_FLAGS, Veil.class.getClassLoader());

    @ApiStatus.Internal
    public static void init() {
    }

    /**
     * @return The global Veil molang compiler
     */
    public static MolangCompiler get() {
        return COMPILER;
    }
}
