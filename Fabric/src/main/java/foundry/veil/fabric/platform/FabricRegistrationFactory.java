package foundry.veil.fabric.platform;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import foundry.veil.platform.registry.RegistrationProvider;
import foundry.veil.platform.registry.RegistryObject;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

@ApiStatus.Internal
public class FabricRegistrationFactory implements RegistrationProvider.Factory {

    @SuppressWarnings("unchecked")
    @Override
    public <T> RegistrationProvider<T> create(ResourceKey<? extends Registry<T>> key, String modId) {
        Registry<?> reg = BuiltInRegistries.REGISTRY.get(key.location());
        if (reg == null) {
            reg = FabricRegistryBuilder.createSimple((ResourceKey<Registry<T>>) key).buildAndRegister();
        }
        return new Provider<>(modId, (Registry<T>) reg);
    }

    @Override
    public <T> RegistrationProvider<T> create(Registry<T> registry, String modId) {
        return new Provider<>(modId, registry);
    }

    private static class Provider<T> implements RegistrationProvider<T> {

        private final String modId;
        private final Registry<T> registry;

        private final Set<RegistryObject<T>> entries = new HashSet<>();
        private final Set<RegistryObject<T>> entriesView = Collections.unmodifiableSet(this.entries);

        private Provider(String modId, Registry<T> registry) {
            this.modId = modId;
            this.registry = registry;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <I extends T> RegistryObject<I> register(String name, Supplier<? extends I> supplier) {
            ResourceLocation id = new ResourceLocation(this.modId, name);
            RegistryObject<I> old = (RegistryObject<I>) this.registry.get(id);
            if (old != null) {
                return old;
            }

            I obj = Registry.register(this.registry, id, supplier.get());
            ResourceKey<I> key = ResourceKey.create((ResourceKey<? extends Registry<I>>) this.registry.key(), id);

            RegistryObject<I> ro = new RegistryObject<>() {

                @Override
                public ResourceKey<I> getResourceKey() {
                    return key;
                }

                @Override
                public ResourceLocation getId() {
                    return id;
                }

                @Override
                public I get() {
                    return obj;
                }

                @Override
                public Holder<I> asHolder() {
                    return (Holder<I>) Provider.this.registry.getHolderOrThrow((ResourceKey<T>) key);
                }
            };
            this.entries.add((RegistryObject<T>) ro);
            return ro;
        }

        @Override
        public Collection<RegistryObject<T>> getEntries() {
            return this.entriesView;
        }

        @Override
        public Registry<T> asVanillaRegistry() {
            return this.registry;
        }

        @Override
        public String getModId() {
            return this.modId;
        }
    }
}