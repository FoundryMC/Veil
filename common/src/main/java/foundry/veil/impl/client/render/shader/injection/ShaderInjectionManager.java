package foundry.veil.impl.client.render.shader.injection;

import foundry.veil.Veil;
import foundry.veil.impl.client.render.shader.injection.util.ShaderInjection;
import foundry.veil.impl.client.render.shader.injection.util.ShaderInjectionDefinition;
import io.github.ocelot.glslprocessor.api.node.GlslTree;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Manages modifications for both vanilla and Veil shader files.
 *
 * @author Ocelot, Vowxky
 */
@ApiStatus.Internal
public class ShaderInjectionManager extends SimplePreparableReloadListener<ShaderInjectionManager.Preparations> {

    private static final Pattern SHADER_EXTENSION = Pattern.compile("\\.(fsh|vsh|tcsh|tesh|gsh|comp)$");

    private Map<ResourceLocation, List<ShaderInjection>> shaders = Collections.emptyMap();
    private Map<ShaderInjection, ResourceLocation> names = Collections.emptyMap();
    private Map<ResourceLocation, ResourceLocation> replacements = Collections.emptyMap();

    /**
     * Applies all registered shader modifications to the specified GLSL tree.
     */
    public void applyModifiers(ResourceLocation shaderId, GlslTree tree, boolean applyVersion) {
        Collection<ShaderInjection> modifiers = this.getModifiers(shaderId);
        if (modifiers.isEmpty()) return;
        try {
            for (ShaderInjection modifier : modifiers) {
                modifier.inject(tree, applyVersion);
            }
        } catch (Exception e) {
            Veil.LOGGER.error("Failed to transform shader: {}", shaderId, e);
        }
    }

    /**
     * Fetches all registered modifications for the specified shader.
     */
    public List<ShaderInjection> getModifiers(ResourceLocation shaderId) {
        return this.shaders.getOrDefault(shaderId, Collections.emptyList());
    }

    /**
     * Returns the resource ID under which the given modification was registered, or null.
     */
    @Nullable
    public ResourceLocation getModifierId(ShaderInjection modification) {
        return this.names.get(modification);
    }

    /**
     * Returns the replacement shader ID for the target, or null if none.
     */
    @Nullable
    public ResourceLocation getReplacement(ResourceLocation target) {
        ResourceLocation stripped = stripShaderExtension(target);
        return this.replacements.get(stripped != null ? stripped : target);
    }

    /**
     * Strips the shader extension (.fsh, .vsh, etc.) from a path, or null if none.
     */
    @Nullable
    private static ResourceLocation stripShaderExtension(ResourceLocation location) {
        String path = location.getPath();
        String stripped = SHADER_EXTENSION.matcher(path).replaceFirst("");
        return !stripped.equals(path) ? ResourceLocation.fromNamespaceAndPath(location.getNamespace(), stripped) : null;
    }

    /**
     * Loads and adapts all shader injection JSON definitions from resource packs.
     */
    @Override
    protected @NotNull Preparations prepare(@NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller) {
        Map<ResourceLocation, List<ShaderInjection>> modifiers = new HashMap<>();
        Map<ShaderInjection, ResourceLocation> names = new HashMap<>();
        Map<ResourceLocation, ResourceLocation> replacements = new HashMap<>();

        ShaderInjectionLoader.LoadedPatches loaded = ShaderInjectionLoader.load(resourceManager);
        for (Map.Entry<ResourceLocation, List<ShaderInjectionLoader.LoadedPatch>> entry : loaded.byTarget().entrySet()) {
            ResourceLocation target = entry.getKey();
            List<ShaderInjectionLoader.LoadedPatch> patches = entry.getValue();

            List<ShaderInjection> mods = modifiers.computeIfAbsent(target, k -> new LinkedList<>());
            for (ShaderInjectionLoader.LoadedPatch patch : patches) {
                ShaderInjectionDefinition definition = patch.definition();
                if (definition.replace() != null) {
                    ResourceLocation replaceTarget = stripShaderExtension(target);
                    if (replaceTarget == null) {
                        replaceTarget = target;
                    }
                    replacements.put(replaceTarget, definition.replace());
                    if (definition.debug()) {
                        Veil.LOGGER.info("\n═══ Replace: {} ═══\n  Target:   {}\n  Replace:  {}\n  Source:   {}",
                                replaceTarget,
                                target,
                                definition.replace(),
                                patch.resourceLocation());
                    }
                    continue;
                }

                try {
                    for (ShaderInjection m : ShaderInjectionAdapter.toModifications(definition, resourceManager)) {
                        mods.add(m);
                        names.put(m, definition.id() != null ? definition.id() : patch.resourceLocation());
                    }
                } catch (Throwable t) {
                    Veil.LOGGER.error("Couldn't adapt shader injection {}", patch.resourceLocation(), t);
                }
            }
        }

        return new Preparations(modifiers, names, replacements);
    }

    /**
     * Applies the loaded preparations to the live injection and replacement maps.
     */
    @Override
    protected void apply(@NotNull Preparations preparations, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller) {
        this.shaders = Collections.unmodifiableMap(preparations.shaders);
        this.names = Collections.unmodifiableMap(preparations.names);
        this.replacements = Collections.unmodifiableMap(preparations.replacements);
        Veil.LOGGER.info("Loaded {} shader redirects, {} replacements", this.names.size(), this.replacements.size());
        if (!this.replacements.isEmpty()) {
            Veil.LOGGER.debug("Active shader replacements:");
            this.replacements.forEach((target, replacement) -> Veil.LOGGER.debug("  {} -> {}", target, replacement));
        }
    }

    @ApiStatus.Internal
    public record Preparations(Map<ResourceLocation, List<ShaderInjection>> shaders,
                               Map<ShaderInjection, ResourceLocation> names,
                               Map<ResourceLocation, ResourceLocation> replacements) {
    }
}
