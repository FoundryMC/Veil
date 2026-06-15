package foundry.veil.api.client.render.shader.processor;

import foundry.veil.Veil;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.impl.client.render.shader.injection.ShaderInjectionManager;
import foundry.veil.impl.client.render.shader.injection.util.ShaderInjection;
import io.github.ocelot.glslprocessor.api.GlslSyntaxException;
import io.github.ocelot.glslprocessor.api.node.GlslTree;
import io.github.ocelot.glslprocessor.lib.anarres.cpp.LexerException;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Injects shader modifications into shader source files.
 *
 * @author Ocelot
 * @author Vowxky
 */
public class ShaderInjectProcessor implements ShaderPreProcessor {

    private final ShaderInjectionManager shaderInjectionManager;
    private final Set<ResourceLocation> appliedModifications;

    public ShaderInjectProcessor() {
        this.shaderInjectionManager = VeilRenderSystem.renderer().getShaderInjectionManager();
        this.appliedModifications = new HashSet<>();
    }

    @Override
    public void prepare() {
        this.appliedModifications.clear();
    }

    @Override
    public void modify(Context ctx, GlslTree tree) throws IOException, GlslSyntaxException, LexerException {
        ResourceLocation name = ctx.name();
        if (name == null || !this.appliedModifications.add(name)) {
            return;
        }

        boolean applyVersion = ctx.isSourceFile();
        for (ResourceLocation include : ctx.shaderImporter().addedImports()) {
            this.applyModifiers(include, tree, applyVersion);
        }
        this.applyModifiers(name, tree, applyVersion);
    }

    private void applyModifiers(ResourceLocation shaderId, GlslTree tree, boolean applyVersion) {
        List<ShaderInjection> modifiers = this.shaderInjectionManager.getModifiers(shaderId);
        if (modifiers.isEmpty()) {
            return;
        }
        try {
            for (ShaderInjection modifier : modifiers) {
                modifier.inject(tree, applyVersion);
            }
        } catch (Exception e) {
            Veil.LOGGER.error("Failed to transform shader: {}", shaderId, e);
        }
    }
}
