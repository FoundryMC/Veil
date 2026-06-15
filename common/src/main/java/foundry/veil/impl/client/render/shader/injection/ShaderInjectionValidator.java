package foundry.veil.impl.client.render.shader.injection;

import foundry.veil.impl.client.render.shader.injection.util.Diagnostic;
import foundry.veil.impl.client.render.shader.injection.util.Severity;
import foundry.veil.impl.client.render.shader.injection.util.ShaderInjectionDefinition;
import foundry.veil.impl.client.render.shader.injection.util.ValidationResult;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Validates {@link ShaderInjectionDefinition} instances.
 *
 * @author Vowxky
 */
@ApiStatus.Internal
public final class ShaderInjectionValidator {

    public static ValidationResult validate(ShaderInjectionDefinition injection) {
        return validate(injection, null);
    }

    public static ValidationResult validate(ShaderInjectionDefinition injection, @Nullable String resourcePath) {
        List<Diagnostic> diagnostics = new ArrayList<>();
        validateInjection(injection, resourcePath, diagnostics);
        return new ValidationResult(diagnostics);
    }

    public static ValidationResult validate(Collection<ShaderInjectionDefinition> injections) {
        return validate(injections, null);
    }

    public static ValidationResult validate(Collection<ShaderInjectionDefinition> injections, @Nullable String resourcePath) {
        List<Diagnostic> diagnostics = new ArrayList<>();
        for (ShaderInjectionDefinition injection : injections) {
            validateInjection(injection, resourcePath, diagnostics);
        }
        return new ValidationResult(diagnostics);
    }

    private static void validateInjection(ShaderInjectionDefinition injection, @Nullable String resourcePath, List<Diagnostic> diagnostics) {
        if (injection == null) {
            diagnostics.add(new Diagnostic(resourcePath, null, null, "injection", "Injection definition is null", Severity.ERROR));
            return;
        }

        if (injection.targets().isEmpty()) {
            diagnostics.add(new Diagnostic(resourcePath, injection.id(), null, "targets", "Missing targets", Severity.ERROR));
        }

        boolean hasReplace = injection.replace() != null;
        boolean hasRedirects = !injection.redirects().isEmpty();

        if (hasReplace && hasRedirects) {
            diagnostics.add(new Diagnostic(resourcePath, injection.id(), injection.target(), "replace", "Replace cannot be combined with redirect", Severity.ERROR));
        }

        if (!hasReplace && !hasRedirects) {
            diagnostics.add(new Diagnostic(resourcePath, injection.id(), injection.target(), "redirect", "No redirect or replace specified", Severity.ERROR));
        }
    }

}
