package foundry.veil.api.client.render.shader.processor;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Processes shader sources line by line.
 *
 * @author Ocelot
 */
public abstract class ShaderLineProcessor implements ShaderPreProcessor {

    @Override
    public @NotNull String modify(@NotNull Context context) throws IOException {
        StringBuilder finalSource = new StringBuilder();
        List<String> lines = new ArrayList<>(Arrays.asList(context.getInput().split("\n")));
        List<String> output = new LinkedList<>();

        for (String line : lines) {
            output.clear();
            output.add(line);
            this.modify(context, line, output);
            output.forEach(s -> finalSource.append(s).append("\n"));
        }

        return finalSource.toString();
    }

    /**
     * Modifies a single line in a shader source.
     *
     * @param context  Context for modifying shaders
     * @param original A copy of the original source
     * @param line     The data to use as the current line.
     *                 The original source is the first element and can be modified as necessary.
     *                 The source can be replaced by clearing the list and adding new source.
     * @throws IOException If any error occurs while editing the source
     */
    public abstract void modify(@NotNull Context context, @NotNull String original, @NotNull List<String> line) throws IOException;
}
