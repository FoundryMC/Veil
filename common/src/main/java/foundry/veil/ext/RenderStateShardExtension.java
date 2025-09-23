package foundry.veil.ext;

import org.spongepowered.asm.mixin.Unique;

import java.util.List;

public interface RenderStateShardExtension {
    @Unique
    void veil$addSetup(Runnable preDraw);

    void veil$addClear(Runnable postDraw);

    List<Runnable> veil$getSetup();

    List<Runnable> veil$getClear();
}
