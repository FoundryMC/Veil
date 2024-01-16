package foundry.veil.quasar.emitters;

import foundry.veil.quasar.client.particle.data.QuasarParticleDataRegistry;
import foundry.veil.quasar.emitters.modules.emitter.settings.EmitterSettingsRegistry;
import foundry.veil.quasar.emitters.modules.particle.render.RenderModuleRegistry;
import foundry.veil.quasar.emitters.modules.particle.update.UpdateModuleRegistry;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class ParticleEmitterRegistry {
    private static final BiMap<ResourceLocation, ParticleEmitter> EMITTERS_BY_ID = HashBiMap.create();

    public static void bootstrap(){
        EmitterSettingsRegistry.bootstrap();
        QuasarParticleDataRegistry.bootstrap();
        UpdateModuleRegistry.bootstrap();
        RenderModuleRegistry.bootstrap();
    }

    public static void register(ResourceLocation id, ParticleEmitter emitter) {
        EMITTERS_BY_ID.put(id, emitter);
    }

    public static ParticleEmitter getEmitter(ResourceLocation id) {
        return EMITTERS_BY_ID.get(id);
    }

    public static ResourceLocation getEmitterId(ParticleEmitter emitter) {
        return EMITTERS_BY_ID.inverse().get(emitter);
    }

    public static void clearRegisteredEmitters() {
        EMITTERS_BY_ID.clear();
    }

    public static Iterable<ResourceLocation> getEmitterNames() {
        return EMITTERS_BY_ID.keySet().stream().toList();
    }

    public static List<ResourceLocation> getEmitters() {
        return EMITTERS_BY_ID.keySet().stream().toList();
    }
}
