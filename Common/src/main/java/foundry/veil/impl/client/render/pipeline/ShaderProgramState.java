package foundry.veil.impl.client.render.pipeline;

import net.minecraft.client.renderer.RenderStateShard;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ShaderProgramState extends RenderStateShard.ShaderStateShard {

    private final Runnable setup;

    public ShaderProgramState(Runnable setup) {
        this.setup = setup;
    }

    @Override
    public void setupRenderState() {
        this.setup.run();
    }
}
