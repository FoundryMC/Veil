package foundry.veil.forge.platform;

import foundry.veil.platform.registry.RegistrationProvider;
import foundry.veil.platform.registry.RegistryObject;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;
import net.minecraftforge.registries.DeferredRegister;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public class ForgeRegistrationFactory implements RegistrationProvider.Factory {

    @Override
    public <T> RegistrationProvider<T> create(ResourceKey<? extends Registry<T>> resourceKey, String modId) {
        Optional<? extends ModContainer> containerOpt = ModList.get().getModContainerById(modId);
        if (containerOpt.isEmpty()) {
            throw new NullPointerException("Cannot find mod container for id " + modId);
        }

        if (!(containerOpt.get() instanceof FMLModContainer fmlModContainer)) {
            throw new ClassCastException("The container of the mod " + modId + " is not a FML one!");
        }

        DeferredRegister<T> register = DeferredRegister.create(resourceKey, modId);
        register.register(fmlModContainer.getEventBus());
        return new Provider<>(modId, register);
    }

    private static class Provider<T> implements RegistrationProvider<T> {

        private final String modId;
        private final DeferredRegister<T> registry;

        private final Set<RegistryObject<T>> entries = new HashSet<>();
        private final Set<RegistryObject<T>> entriesView = Collections.unmodifiableSet(this.entries);

        private Provider(String modId, DeferredRegister<T> registry) {
            this.modId = modId;
            this.registry = registry;
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
    }
}