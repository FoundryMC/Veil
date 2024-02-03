package foundry.veil.mixin.client.quasar;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(RegistryDataLoader.RegistryData.class)
public interface RegistryDataAccessor {

    @Invoker
    Pair<WritableRegistry<?>, RegistryDataLoader.Loader> invokeCreate(Lifecycle lifecycle, Map<ResourceKey<?>, Exception> errors);
}
