package foundry.veil.api.client.render.shader.processor;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.impl.client.render.shader.injection.ShaderInjectionManager;
import io.github.ocelot.glslprocessor.api.GlslSyntaxException;
import io.github.ocelot.glslprocessor.api.node.GlslTree;
import io.github.ocelot.glslprocessor.lib.anarres.cpp.LexerException;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Modifies shader sources with the shader modification system.
 *
 * @author Ocelot
 * @deprecated Use {@link ShaderInjectProcessor} instead.
 */
@ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
@Deprecated(forRemoval = true)
public class ShaderModifyProcessor implements ShaderPreProcessor {

    private final ShaderInjectionManager shaderInjectionManager;
    private final Set<ResourceLocation> appliedModifications;

    public ShaderModifyProcessor() {
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
            this.shaderInjectionManager.applyModifiers(include, tree, applyVersion);
        }
        this.shaderInjectionManager.applyModifiers(name, tree, applyVersion);
    }
}
