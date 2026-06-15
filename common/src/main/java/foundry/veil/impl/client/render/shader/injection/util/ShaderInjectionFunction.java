package foundry.veil.impl.client.render.shader.injection.util;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record ShaderInjectionFunction(String name, int parameters, boolean head, String code) {
}
