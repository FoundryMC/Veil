package foundry.veil.quasar.emitters;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import foundry.veil.quasar.client.particle.data.QuasarParticleDataRegistry;
import foundry.veil.quasar.data.ParticleEmitterData;
import foundry.veil.quasar.emitters.modules.emitter.settings.EmitterSettingsRegistry;
import foundry.veil.quasar.emitters.modules.particle.render.RenderModuleRegistry;
import foundry.veil.quasar.emitters.modules.particle.update.UpdateModuleRegistry;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

@Deprecated
public class ParticleEmitterRegistry {

    private static final BiMap<ResourceLocation, ParticleEmitterData> EMITTERS_BY_ID = HashBiMap.create();

    public static void bootstrap() {
        EmitterSettingsRegistry.bootstrap();
        QuasarParticleDataRegistry.bootstrap();
        UpdateModuleRegistry.bootstrap();
        RenderModuleRegistry.bootstrap();
    }

    public static void register(ResourceLocation id, ParticleEmitterData emitter) {
        EMITTERS_BY_ID.put(id, emitter);
    }

    public static @Nullable ParticleEmitterData getEmitter(ResourceLocation id) {
        return EMITTERS_BY_ID.get(id);
    }

    public static @Nullable ResourceLocation getEmitterId(ParticleEmitterData emitter) {
        return EMITTERS_BY_ID.inverse().get(emitter);
    }

    public static void clearRegisteredEmitters() {
        EMITTERS_BY_ID.clear();
    }

    public static Set<ResourceLocation> getEmitters() {
        return EMITTERS_BY_ID.keySet();
    }
}
