package foundry.veil.impl.client.render.shader.injection.util;

import java.util.List;

public record ValidationResult(List<Diagnostic> diagnostics) {

    public ValidationResult {
        diagnostics = List.copyOf(diagnostics);
    }

    public boolean isValid() {
        return diagnostics.stream().noneMatch(d -> d.severity() == Severity.ERROR);
    }
}
