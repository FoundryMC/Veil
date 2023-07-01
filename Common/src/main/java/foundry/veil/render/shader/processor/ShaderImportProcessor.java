package foundry.veil.render.shader.processor;

import foundry.veil.render.shader.ShaderManager;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

/**
 * Processes a shader to add imports.
 *
 * @author Ocelot
 */
public class ShaderImportProcessor implements ShaderPreProcessor {

    private static final String INCLUDE_KEY = "#include ";

    private final ResourceProvider resourceProvider;
    private final Set<ResourceLocation> addedImports;
    private final Map<ResourceLocation, String> imports;

    /**
     * Creates a new import processor that loads import files from the specified resource provider.
     *
     * @param resourceProvider The provider for import resources
     */
    public ShaderImportProcessor(ResourceProvider resourceProvider) {
        this.resourceProvider = resourceProvider;
        this.addedImports = new HashSet<>();
        this.imports = new HashMap<>();
    }

    @Override
    public void prepare() {
        this.addedImports.clear();
    }

    @Override
    public String modify(Context context) throws IOException {
        Objects.requireNonNull(context, "context");

        List<String> inputLines = new LinkedList<>(Arrays.asList(context.getInput().split("\n")));
        List<String> output = new LinkedList<>();

        for (String line : inputLines) {
            if (!line.startsWith(ShaderImportProcessor.INCLUDE_KEY)) {
                output.add(line);
                continue;
            }

            try {
                String trimmedImport = line.substring(ShaderImportProcessor.INCLUDE_KEY.length()).trim();
                ResourceLocation source = new ResourceLocation(trimmedImport);

                // Only read and process the import if it hasn't been added yet
                if (!this.addedImports.add(source)) {
                    continue;
                }

                try {
                    if (!this.imports.containsKey(source)) {
                        this.imports.put(source, this.loadImport(context, source));
                    }

                    String importString = this.imports.get(source);
                    if (importString == null) {
                        throw new IOException("Import previously failed to load");
                    }

                    int lineNumber = output.size();
                    output.add("#line 1");
                    output.add(context.modify(importString));
                    output.add("#line " + lineNumber);
                } catch (Exception e) {
                    throw new IOException("Failed to add import: " + line, e);
                }
            } catch (ResourceLocationException e) {
                throw new IOException("Invalid import: " + line, e);
            }
        }

        return String.join("\n", output);
    }

    private String loadImport(Context context, ResourceLocation source) throws IOException {
        Resource resource = this.resourceProvider.getResourceOrThrow(ShaderManager.INCLUDE_LISTER.idToFile(source));
        try (Reader reader = resource.openAsReader()) {
            return IOUtils.toString(reader);
        }
    }
}
