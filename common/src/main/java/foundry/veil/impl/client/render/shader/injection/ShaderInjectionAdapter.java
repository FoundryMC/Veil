package foundry.veil.impl.client.render.shader.injection;

import foundry.veil.Veil;
import foundry.veil.impl.client.render.shader.injection.util.ShaderInjection;
import foundry.veil.impl.client.render.shader.injection.util.ShaderInjectionDefinition;
import foundry.veil.impl.client.render.shader.injection.util.ShaderInjectionFunction;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Adapts JSON-based shader injection definitions into
 * {@link ShaderInjection} objects for GLSL tree transformation.
 *
 * @author Vowxky
 */
public final class ShaderInjectionAdapter {

    private static final String DEFAULT_FUNCTION = "main";
    private static final int DEFAULT_PARAM_COUNT = -1;
    private static final Pattern INCLUDE_PATTERN = Pattern.compile("^\\s*#include\\s+[\"<]?([^>\"]+)[>\"]?\\s*$");
    private static final FileToIdConverter INCLUDE_LISTER = new FileToIdConverter("pinwheel/shaders/include", ".glsl");
    private static final Set<String> DEBUG_LOGGED = new HashSet<>();

    public static List<ShaderInjection> toModifications(ShaderInjectionDefinition definition, ResourceProvider provider) throws IOException {
        List<ShaderInjection> mods = new ArrayList<>();
        for (ResourceLocation path : definition.redirects()) {
            String code = loadGlsl(path, provider);
            mods.add(buildModification(definition, code));
        }
        return mods;
    }

    private static ShaderInjection buildModification(ShaderInjectionDefinition def, String code) {
        code = expandDefines(code);
        InjectionBodyParser.Result parsed = InjectionBodyParser.parse(code);

        if (parsed.body().isEmpty() && parsed.globals().isEmpty()) {
            Veil.LOGGER.warn("Shader injection '{}' target '{}' has no head() or tail() marker — skipping", def.id(), def.target());
            return new SimpleShaderInjection(parsed.version(), def.priority(), new ShaderInjectionFunction[0], null);
        }

        if (def.debug() && DEBUG_LOGGED.add(def.id() != null ? def.id().toString() : String.valueOf(def.target()))) {
            StringBuilder sb = new StringBuilder("\n═══ Injection: ").append(def.target()).append(" ═══\n");
            sb.append("  Version: ").append(parsed.version() >= 0 ? parsed.version() : "auto").append('\n');
            sb.append("  Body:    ").append(parsed.body().replace("\n", "\n           ")).append('\n');
            if (!parsed.globals().isEmpty()) {
                sb.append("  Globals: ").append(parsed.globals().replace("\n", "\n           "));
            }
            Veil.LOGGER.info(sb.toString());
        }

        ShaderInjectionFunction func = new ShaderInjectionFunction(DEFAULT_FUNCTION, DEFAULT_PARAM_COUNT, parsed.isHead(), parsed.body());
        return new SimpleShaderInjection(parsed.version(), def.priority(), new ShaderInjectionFunction[]{func}, parsed.globals().isEmpty() ? null : parsed.globals());
    }

    private static String loadGlsl(ResourceLocation path, ResourceProvider provider) throws IOException {
        ResourceLocation fullPath = ResourceLocation.fromNamespaceAndPath(path.getNamespace(), "pinwheel/shader_injection/" + path.getPath());
        return resolveIncludes(fullPath, provider, new HashSet<>());
    }

    private static String resolveIncludes(ResourceLocation loc, ResourceProvider provider, Set<ResourceLocation> visited) throws IOException {
        if (!visited.add(loc)) {
            return "";
        }
        String source;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(provider.open(loc), java.nio.charset.StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) sb.append(line).append('\n');
            source = sb.toString();
        }

        StringBuilder out = new StringBuilder();
        for (String line : source.split("\n", -1)) {
            Matcher m = INCLUDE_PATTERN.matcher(line);
            if (m.matches()) {
                ResourceLocation includeId = ResourceLocation.parse(m.group(1));
                ResourceLocation includePath = INCLUDE_LISTER.idToFile(includeId);
                out.append(resolveIncludes(includePath, provider, visited));
            } else {
                out.append(line).append('\n');
            }
        }
        return out.toString();
    }

    private static final Pattern DEFINE_PATTERN = Pattern.compile("^\\s*#define\\s+(\\w+)\\s*(.*?)\\s*$");

    private static String expandDefines(String source) {
        List<String[]> defines = new ArrayList<>();
        StringBuilder result = new StringBuilder();

        for (String line : source.split("\n", -1)) {
            Matcher m = DEFINE_PATTERN.matcher(line);
            if (m.matches()) {
                defines.add(new String[]{m.group(1), m.group(2)});
            } else {
                if (result.length() > 0) result.append('\n');
                result.append(line);
            }
        }

        String expanded = result.toString();
        for (String[] define : defines) {
            String name = define[0];
            String value = define[1];
            if (!value.isEmpty()) {
                expanded = expanded.replaceAll("\\b" + name + "\\b", Matcher.quoteReplacement(value));
            }
        }
        return expanded;
    }
}
