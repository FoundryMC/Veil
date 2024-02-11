package foundry.veil.mixin.client.quasar;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(RegistryDataLoader.class)
public interface RegistryDataLoaderAccessor {

    @Invoker
    static RegistryOps.RegistryInfoLookup invokeCreateContext(RegistryAccess $$0, List<Pair<WritableRegistry<?>, RegistryDataLoader.Loader>> $$1) {
        throw new AssertionError();
    }
}
