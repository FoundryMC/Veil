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
    private final List<ResourceLocation> importOrder;

    /**
     * Creates a new import processor that loads import files from the specified resource provider.
     *
     * @param resourceProvider The provider for import resources
     */
    public ShaderImportProcessor(ResourceProvider resourceProvider) {
        this.resourceProvider = resourceProvider;
        this.addedImports = new HashSet<>();
        this.imports = new HashMap<>();
        this.importOrder = new ArrayList<>();
    }

    @Override
    public void prepare() {
        this.addedImports.clear();
    }

    @Override
    public String modify(Context context) throws IOException {
        List<String> inputLines = context.getInput().lines().toList();
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
                        this.imports.put(source, this.loadImport(source));
                        this.importOrder.add(source);
                    }

                    String importString = this.imports.get(source);
                    if (importString == null) {
                        throw new IOException("Import previously failed to load");
                    }

                    long lineNumber = String.join("\n", output).lines().filter(s -> !s.startsWith("#line")).count() + 2;
                    int sourceNumber = this.importOrder.indexOf(source);
                    output.add("#line 0 " + (sourceNumber + 1));
                    output.add(context.modify(source, importString));
                    output.add("#line " + lineNumber + " " + sourceNumber);
                } catch (Exception e) {
                    throw new IOException("Failed to add import: " + line, e);
                }
            } catch (ResourceLocationException e) {
                throw new IOException("Invalid import: " + line, e);
            }
        }

        return String.join("\n", output);
    }

    private String loadImport(ResourceLocation source) throws IOException {
        Resource resource = this.resourceProvider.getResourceOrThrow(ShaderManager.INCLUDE_LISTER.idToFile(source));
        try (Reader reader = resource.openAsReader()) {
            return IOUtils.toString(reader);
        }
    }
}
