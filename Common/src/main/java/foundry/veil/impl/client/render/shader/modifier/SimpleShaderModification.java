package foundry.veil.impl.client.render.shader.modifier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.regex.Matcher;

@ApiStatus.Internal
public class SimpleShaderModification implements ShaderModification {

    private final int version;
    private final int priority;
    private final ResourceLocation[] includes;
    private final String output;
    private final String uniform;
    private final Function[] functions;

    public SimpleShaderModification(int version, int priority, ResourceLocation[] includes, @Nullable String output, @Nullable String uniform, Function[] functions) {
        this.version = version;
        this.priority = priority;
        this.includes = includes;
        this.output = output;
        this.uniform = uniform;
        this.functions = functions;
    }

    @Override
    public String inject(String source, int flags) throws IOException {
        int pointer;
        if ((flags & APPLY_VERSION) > 0) {
            Matcher versionMatcher = VERSION_PATTERN.matcher(source);
            if (!versionMatcher.find()) {
                throw new IOException("Failed to find version");
            }

            if (this.version == -1) {
                throw new IOException("Missing #version field");
            }

            try {
                int version = Integer.parseInt(versionMatcher.group(1));
                if (version < this.version) {
                    source = versionMatcher.replaceAll("#version " + this.version + "\n\n");

                    versionMatcher.reset(source);
                    if (!versionMatcher.find()) {
                        throw new IllegalStateException();
                    }
                }
            } catch (Exception e) {
                throw new IOException("Failed to inject version", e);
            }

            pointer = versionMatcher.end();
        } else {
            pointer = 0;
        }

        StringBuilder result = new StringBuilder(source);
        for (ResourceLocation include : this.includes) {
            String code = "#include " + include + "\n";
            result.insert(pointer, code);
            pointer += code.length();
        }

        this.processBody(pointer, result);

        return result.toString();
    }

    protected void processBody(int pointer, StringBuilder builder) throws IOException {
        if (!StringUtil.isNullOrEmpty(this.uniform)) {
            Matcher matcher = UNIFORM_PATTERN.matcher(builder);
            while (matcher.find()) {
                pointer = matcher.end();
            }

            String code = this.fillPlaceholders(this.uniform) + '\n';
            builder.insert(pointer, code);
            pointer += code.length();
        }

        if (!StringUtil.isNullOrEmpty(this.output)) {
            Matcher matcher = OUT_PATTERN.matcher(builder);
            while (matcher.find()) {
                pointer = matcher.end();
            }

            String code = this.fillPlaceholders(this.output) + '\n';
            builder.insert(pointer, code);
        }

        for (Function function : this.functions) {
            Matcher matcher = function.pattern().matcher(builder);
            if (!matcher.find()) {
                throw new IOException("Unknown function: " + function.name());
            }

            int head = matcher.end();
            pointer = head;
            if (!function.head()) {
                int parenthesis = 1;
                while (pointer < builder.length()) {
                    if (builder.charAt(pointer) == '{') {
                        parenthesis++;
                    }
                    if (builder.charAt(pointer) == '}') {
                        parenthesis--;
                    }
                    if (parenthesis == 0) {
                        pointer--;
                        break;
                    }
                    pointer++;
                }

                Matcher returnMatcher = RETURN_PATTERN.matcher(builder.substring(head, pointer));
                while (returnMatcher.find()) {
                    pointer = returnMatcher.start() - 1;
                }
            }

            String code = this.fillPlaceholders("\n{\n" + function.code() + "}");
            builder.insert(pointer, code);

            if (matcher.find()) {
                throw new IOException("Ambiguous method: " + function.name());
            }
        }
    }

    public String fillPlaceholders(String code) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(code);
        if (!matcher.find()) {
            return code;
        }

        StringBuilder sb = new StringBuilder();
        matcher.appendReplacement(sb, this.getPlaceholder(matcher.group(1)));
        while (matcher.find()) {
            matcher.appendReplacement(sb, this.getPlaceholder(matcher.group(1)));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    protected String getPlaceholder(String key) {
        return key;
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    public String getOutput() {
        return this.output;
    }
}
