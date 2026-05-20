package foundry.veil.fabric.mixin.compat.imguimc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(targets = "foundry.imgui.impl.renderer.v0.ImGuiRendererGL33", remap = false)
public class ImGuiRendererGL33Mixin {

    /**
     * @author Ocelot
     * @reason ImGuiMC 1.2.3 emits "#version 130 core", but GLSL 130 does not allow a profile token.
     */
    @Overwrite
    protected String vertexShaderGlsl130() {
        return """
                #version 130
                uniform mat4 ProjMtx;
                in vec2 Position;
                in vec2 UV;
                in vec4 Color;
                out vec2 Frag_UV;
                out vec4 Frag_Color;
                void main()
                {
                    Frag_UV = UV;
                    Frag_Color = Color;
                    gl_Position = ProjMtx * vec4(Position.xy,0,1);
                }
                """;
    }

    /**
     * @author Ocelot
     * @reason ImGuiMC 1.2.3 emits "#version 130 core", but GLSL 130 does not allow a profile token.
     */
    @Overwrite
    protected String fragmentShaderGlsl130() {
        return """
                #version 130
                uniform sampler2D Texture;
                in vec2 Frag_UV;
                in vec4 Frag_Color;
                out vec4 Out_Color;
                void main()
                {
                    Out_Color = Frag_Color * texture(Texture, Frag_UV.st);
                }
                """;
    }
}
