package foundry.veil.impl.client.render.shader.modifier;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

@ApiStatus.Internal
public record ReplaceShaderModification(int priority, ResourceLocation veilShader) implements ShaderModification {

    @Override
    public String inject(String source, int flags) throws IOException {
        throw new UnsupportedEncodingException("Replace modification replaces file");
    }

    @Override
    public int getPriority() {
        return this.priority;
    }
}
