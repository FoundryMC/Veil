package foundry.veil.impl.client.render.shader.injection.util;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

@ApiStatus.Internal
public record ValidationResult(@Unmodifiable List<Diagnostic> diagnostics) {

    public ValidationResult {
        diagnostics = List.copyOf(diagnostics);
    }

    public boolean isValid() {
        if (this.diagnostics.isEmpty()) {
            return true;
        }

        for (Diagnostic diagnostic : this.diagnostics) {
            if (diagnostic.severity() == Severity.ERROR) {
                return false;
            }
        }
        return true;
    }
}
