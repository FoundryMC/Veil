package foundry.veil.forge.platform;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Lifecycle;
import foundry.veil.platform.registry.RegistrationProvider;
import foundry.veil.platform.registry.RegistryObject;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApiStatus.Internal
public class ForgeRegistrationFactory implements RegistrationProvider.Factory {

    @Override
    public <T> RegistrationProvider<T> create(ResourceKey<? extends Registry<T>> resourceKey, String modId) {
        ModContainer container = ModList.get().getModContainerById(modId).orElseThrow(() -> new NullPointerException("Cannot find mod container for id " + modId));
        if (!(container instanceof FMLModContainer forgeContainer)) {
            throw new ClassCastException("The container of the mod " + modId + " is not a FML one!");
        }

        DeferredRegister<T> register = DeferredRegister.create(resourceKey, modId);
        if (RegistryManager.ACTIVE.getRegistry(resourceKey) == null && !BuiltInRegistries.REGISTRY.containsKey(resourceKey.location())) {
            register.makeRegistry(() -> RegistryBuilder.<T>of().disableSaving().disableSync());
        }
        register.register(forgeContainer.getEventBus());
        return new Provider<>(modId, register);
    }

    private static class Provider<T> implements RegistrationProvider<T> {

        private final String modId;
        private final DeferredRegister<T> registry;
        private final RegistryWrapper<T> wrapper;

        private final Set<RegistryObject<T>> entries = new HashSet<>();
        private final Set<RegistryObject<T>> entriesView = Collections.unmodifiableSet(this.entries);

        private Provider(String modId, DeferredRegister<T> registry) {
            this.modId = modId;
            this.registry = registry;
            this.wrapper = new RegistryWrapper<>(registry.getRegistryKey());
        }

        @Override
        public String getModId() {
            return this.modId;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <I extends T> RegistryObject<I> register(String name, Supplier<? extends I> supplier) {
            net.minecraftforge.registries.RegistryObject<I> obj = this.registry.register(name, supplier);
            RegistryObject<I> ro = new RegistryObject<>() {
                @Override
                public ResourceKey<I> getResourceKey() {
                    return obj.getKey();
                }

                @Override
                public ResourceLocation getId() {
                    return obj.getId();
                }

                @Override
                public I get() {
                    return obj.get();
                }

                @Override
                public Holder<I> asHolder() {
                    return obj.getHolder().orElseThrow();
                }
            };
            this.entries.add((RegistryObject<T>) ro);
            return ro;
        }

        @Override
        public Set<RegistryObject<T>> getEntries() {
            return this.entriesView;
        }

        @Override
        public Registry<T> asVanillaRegistry() {
            return this.wrapper;
        }
    }

    // This sucks, but there's not really a point in doing anything better since Forge will be dropped in 1.21 in favor of NeoForged
    private record RegistryWrapper<T>(ResourceKey<? extends Registry<T>> resourceKey) implements Registry<T> {

        private IForgeRegistry<T> getRegistry() {
            return RegistryManager.ACTIVE.getRegistry(this.resourceKey);
        }

        @Override
        public @Nullable T get(@Nullable ResourceLocation name) {
            IForgeRegistry<T> forgeRegistry = this.getRegistry();
            if (name == null) {
                name = forgeRegistry.getDefaultKey();
                if (name == null) {
                    return null;
                }
            }
            return forgeRegistry.getValue(name);
        }

        @Override
        public @Nullable T get(@Nullable ResourceKey<T> name) {
            return this.get(name != null ? name.location() : null);
        }

        @Override
        public ResourceKey<? extends Registry<T>> key() {
            return this.resourceKey;
        }

        @Override
        public @Nullable ResourceLocation getKey(@NotNull T value) {
            return this.getRegistry().getKey(value);
        }

        @Override
        public @NotNull Optional<ResourceKey<T>> getResourceKey(@NotNull T value) {
            return this.getRegistry().getResourceKey(value);
        }

        @Override
        public boolean containsKey(@NotNull ResourceLocation key) {
            return this.getRegistry().containsKey(key);
        }

        @Override
        public boolean containsKey(@NotNull ResourceKey<T> key) {
            return this.resourceKey.registry().equals(key.registry()) && this.containsKey(key.location());
        }

        @Override
        public int getId(@Nullable T value) {
            return value != null ? RegistryManager.ACTIVE.getRegistry(this.resourceKey).getID(value) : -1;
        }

        @Override
        public @Nullable T byId(int id) {
            return RegistryManager.ACTIVE.getRegistry(this.resourceKey).getValue(id);
        }

        @Override
        public Lifecycle lifecycle(T value) {
            return Lifecycle.stable();
        }

        @Override
        public Lifecycle registryLifecycle() {
            return Lifecycle.stable();
        }

        @Override
        public Iterator<T> iterator() {
            return this.getRegistry().iterator();
        }

        @Override
        public Set<ResourceLocation> keySet() {
            return this.getRegistry().getKeys();
        }

        @Override
        public Set<ResourceKey<T>> registryKeySet() {
            return this.getRegistry().getEntries().stream().map(Map.Entry::getKey).collect(Collectors.toSet());
        }

        @Override
        public Set<Map.Entry<ResourceKey<T>, T>> entrySet() {
            return this.getRegistry().getEntries();
        }

        @Override
        public int size() {
            return this.getRegistry().getEntries().size();
        }

        @Override
        public @NotNull Optional<Holder.Reference<T>> getHolder(int id) {
            throw new UnsupportedOperationException("Holders are not supported");
        }

        @Override
        public @NotNull Optional<Holder.Reference<T>> getHolder(ResourceKey<T> key) {
            throw new UnsupportedOperationException("Holders are not supported");
        }

        @Override
        public @NotNull Holder<T> wrapAsHolder(@NotNull T value) {
            throw new UnsupportedOperationException("Holders are not supported");
        }

        @Override
        public Optional<Holder.Reference<T>> getRandom(RandomSource rand) {
            throw new UnsupportedOperationException("Holders are not supported");
        }

        @Override
        public Stream<Holder.Reference<T>> holders() {
            throw new UnsupportedOperationException("Holders are not supported");
        }

        @Override
        public Stream<Pair<TagKey<T>, HolderSet.Named<T>>> getTags() {
            throw new UnsupportedOperationException("Tags are not supported");
        }

        @Override
        public HolderSet.Named<T> getOrCreateTag(TagKey<T> name) {
            throw new UnsupportedOperationException("Tags are not supported");
        }

        @Override
        public Stream<TagKey<T>> getTagNames() {
            throw new UnsupportedOperationException("Tags are not supported");
        }

        @Override
        public Registry<T> freeze() {
            return this;
        }

        @Override
        public Holder.Reference<T> createIntrusiveHolder(T value) {
            throw new UnsupportedOperationException("Intrusive holders are not supported");
        }

        @Override
        public Optional<HolderSet.Named<T>> getTag(TagKey<T> name) {
            throw new UnsupportedOperationException("Tags are not supported");
        }

        @Override
        public void bindTags(Map<TagKey<T>, List<Holder<T>>> newTags) {
            throw new UnsupportedOperationException("Tags are not supported");
        }

        @Override
        public HolderOwner<T> holderOwner() {
            throw new UnsupportedOperationException("Holders are not supported");
        }

        @Override
        public HolderLookup.RegistryLookup<T> asLookup() {
            throw new UnsupportedOperationException("Holders are not supported");
        }

        @Override
        public void resetTags() {
            throw new UnsupportedOperationException("Tags are not supported");
        }
    }
}