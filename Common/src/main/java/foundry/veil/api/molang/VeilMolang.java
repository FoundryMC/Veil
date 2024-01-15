package foundry.veil.api.molang;

import gg.moonflower.molangcompiler.api.GlobalMolangCompiler;
import gg.moonflower.molangcompiler.api.MolangCompiler;

/**
 * Manages the Veil MoLang compiler.
 *
 * @author Ocelot
 */
public final class VeilMolang {

    private static MolangCompiler compiler = input -> GlobalMolangCompiler.get().compile(input);

    /**
     * @return The current molang compiler instance
     */
    public static MolangCompiler get() {
        return compiler;
    }

    /**
     * Sets the current molang compiler instance.
     *
     * @param compiler The new compiler to use
     */
    public static void set(MolangCompiler compiler) {
        VeilMolang.compiler = compiler;
    }
}
