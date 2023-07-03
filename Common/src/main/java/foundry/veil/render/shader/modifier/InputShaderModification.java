package foundry.veil.render.shader.modifier;

import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.util.function.Supplier;
import java.util.regex.Matcher;

@ApiStatus.Internal
public class InputShaderModification implements ShaderModification {

    private final int priority;
    private final Supplier<String> input;

    public InputShaderModification(int priority, Supplier<String> input) {
        this.priority = priority;
        this.input = input;
    }

    @Override
    public String inject(String source, int flags) throws IOException {
        StringBuilder result = new StringBuilder(source);

        int pointer = 0;
        Matcher matcher = IN_PATTERN.matcher(result);
        while (matcher.find()) {
            pointer = matcher.end();
        }

        String code = this.input.get() + '\n';
        result.insert(pointer, code);
        return result.toString();
    }

    @Override
    public int getPriority() {
        return this.priority;
    }
}
