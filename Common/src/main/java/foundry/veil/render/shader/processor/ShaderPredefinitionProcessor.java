package foundry.veil.render.shader.processor;

import foundry.veil.render.shader.definition.ShaderPreDefinitions;
import foundry.veil.render.shader.program.ProgramDefinition;

import java.util.Locale;

/**
 * Adds the predefinition data to shaders.
 *
 * @author Ocelot
 */
public class ShaderPredefinitionProcessor implements ShaderPreProcessor {

    @Override
    public String modify(Context context) {
        ProgramDefinition programDefinition = context.getDefinition();
        String input = context.getInput();
        if (programDefinition == null) {
            return input;
        }

        ShaderPreDefinitions definitions = context.getPreDefinitions();
        StringBuilder builder = new StringBuilder();

        definitions.addStaticDefinitions(value -> builder.append(value).append('\n'));
        for (String name : programDefinition.definitions()) {
            String definition = definitions.getDefinition(name);

            if (definition != null) {
                builder.append(definition).append('\n');
            } else {
                String definitionDefault = programDefinition.definitionDefaults().get(name);
                if (definitionDefault != null) {
                    builder.append("#define ")
                            .append(name.toUpperCase(Locale.ROOT))
                            .append(' ')
                            .append(definitionDefault)
                            .append('\n');
                }
            }

            context.addDefinitionDependency(name);
        }

        builder.append(input);
        return builder.toString();
    }
}
