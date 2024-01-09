package foundry.veil.render.shader.modifier;

import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Allows shader source files to be modified without overwriting the file.
 *
 * @author Ocelot
 */
@ApiStatus.Internal
public interface ShaderModification {

    Pattern VERSION_PATTERN = Pattern.compile("^#version\\s+(\\d+)\\s*\\w*\\s*", Pattern.MULTILINE);
    Pattern OUT_PATTERN = Pattern.compile("^out (\\w+) (\\w+)\\s*;\\s*", Pattern.MULTILINE);
    Pattern IN_PATTERN = Pattern.compile("^(?:layout\\(.*\\))?\\s*in (\\w+) (\\w+)\\s*;\\s*", Pattern.MULTILINE);
    Pattern UNIFORM_PATTERN = Pattern.compile("^uniform \\w+ \\w+\\s*;\\s*", Pattern.MULTILINE);
    Pattern RETURN_PATTERN = Pattern.compile("return\\s+.+;");
    Pattern PLACEHOLDER_PATTERN = Pattern.compile("#(\\w+)");

    /**
     * Whether the version is required and will be applied
     */
    int APPLY_VERSION = 0b01;
    /**
     * Whether [OUT] is a valid command
     */
    int ALLOW_OUT = 0b10;

    /**
     * Injects this modification into the specified shader source.
     *
     * @param source The source to inject into
     * @param flags  The flags to use when injecting
     * @return The injected shader source
     * @throws IOException If an error occurs with the format or applying the modifications
     */
    String inject(String source, int flags) throws IOException;

    /**
     * @return The priority of this modification. A higher priority will be applied before a lower priority modification
     */
    int getPriority();

    static ShaderModification parse(String input, boolean vertex) throws ShaderModificationSyntaxException {
        return ShaderModificationParser.parse(ShaderModifierLexer.createTokens(input), vertex);
    }

    record Function(String name, Pattern pattern, int parameters, boolean head, String code) {

        public static Function create(String name, int parameters, boolean head, String code) {
            if (parameters == -1) {
                return new Function(name, Pattern.compile("\\w+\\s+" + name + "\\s*\\([^)]*\\)[^{]*\\{"), parameters, head, code);
            }
            if (parameters == 0) {
                return new Function(name, Pattern.compile("\\w+\\s+" + name + "\\s*\\(\\s*\\)[^{]*\\{"), parameters, head, code);
            }
            if (parameters == 1) {
                return new Function(name, Pattern.compile("\\w+\\s+" + name + "\\s*\\(\\s*\\w+\\s\\w+)\\)[^{]*\\{"), parameters, head, code);
            }
            return new Function(name, Pattern.compile("\\w+\\s+" + name + "\\s*\\(((\\s*\\w+\\s\\w+,)\\{" + (parameters - 1) + "}\\s*\\w+\\s\\w+)\\)[^{]*\\{"), parameters, head, code);
        }
    }
}
