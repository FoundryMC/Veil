package foundry.veil.render.shader.modifier;

import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.util.regex.Pattern;

@ApiStatus.Internal
public interface ShaderModification {

    Pattern VERSION_PATTERN = Pattern.compile("^#version\\s+(\\d+)\\s*", Pattern.MULTILINE);
    Pattern OUT_PATTERN = Pattern.compile("^out (\\w+) (\\w+)\\s*;\\s*", Pattern.MULTILINE);
    Pattern IN_PATTERN = Pattern.compile("^(?:layout\\(.*\\))?\\s*in (\\w+) (\\w+)\\s*;\\s*", Pattern.MULTILINE);
    Pattern UNIFORM_PATTERN = Pattern.compile("^uniform \\w+ \\w+\\s*;\\s*", Pattern.MULTILINE);
    Pattern RETURN_PATTERN = Pattern.compile("return\\s+.+;");
    Pattern PLACEHOLDER_PATTERN = Pattern.compile("#(\\w+)");

    String inject(String source, boolean allowIncludes) throws IOException;

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
            return new Function(name, Pattern.compile("\\w+\\s+" + name + "\\s*\\(((\\s*\\w+\\s\\w+,){" + (parameters - 1) + "}\\s*\\w+\\s\\w+)\\)[^{]*\\{"), parameters, head, code);
        }
    }
}
