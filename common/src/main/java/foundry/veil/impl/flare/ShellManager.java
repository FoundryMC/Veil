package foundry.veil.impl.flare;

import foundry.veil.api.CodecReloadListener;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import foundry.veil.api.flare.model.ShellBakery;
import foundry.veil.api.flare.model.BakedShell;
import foundry.veil.api.flare.model.UnbakedShell;
import foundry.veil.api.flare.data.model.FlareShell;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Map;

public class ShellManager extends CodecReloadListener<FlareShell> {
    private ShellBakery shellBakery;

    public ShellManager() {
        super(FlareShell.CODEC, FileToIdConverter.json("flare/shells"));
    }

    @ApiStatus.Internal
    @Override
    protected void apply(Map<ResourceLocation, FlareShell> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        this.shellBakery = new ShellBakery(map, List.of(), profilerFiller);
        this.shellBakery.bakeShells();
    }

    public UnbakedShell getUnbakedShell(ResourceLocation shellLocation) {
        return shellBakery.getShell(shellLocation);
    }

    public BakedShell getBakedShell(ResourceLocation shellLocation) {
        return shellBakery.getBakedShell(shellLocation);
    }
}
