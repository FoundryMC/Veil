package foundry.veil.render.shader.processor;

import foundry.veil.render.shader.ShaderManager;
import foundry.veil.render.shader.ShaderModificationManager;
import foundry.veil.render.shader.modifier.ShaderModification;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Modifies shader sources with the shader modification system.
 *
 * @author Ocelot
 */
public class ShaderModifyProcessor implements ShaderPreProcessor {

    private final ShaderModificationManager shaderModificationManager;
    private final Set<ResourceLocation> appliedModifications;

    public ShaderModifyProcessor(ShaderModificationManager shaderModificationManager) {
        this.shaderModificationManager = shaderModificationManager;
        this.appliedModifications = new HashSet<>();
    }

    @Override
    public void prepare() {
        this.appliedModifications.clear();
    }

    @Override
    public String modify(Context context) throws IOException {
        ResourceLocation name = context.getName();
        if (name == null || !this.appliedModifications.add(name)) {
            return context.getInput();
        }
        int flags = context.isSourceFile() ? ShaderModification.APPLY_VERSION | ShaderModification.ALLOW_OUT : 0;
        FileToIdConverter converter = context.isSourceFile() ? context.getConverter() : ShaderManager.INCLUDE_LISTER;
        return context.modify(context.getName(), this.shaderModificationManager.applyModifiers(converter.idToFile(name), context.getInput(), flags));
    }
}
