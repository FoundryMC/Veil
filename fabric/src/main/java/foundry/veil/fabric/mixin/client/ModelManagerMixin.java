package foundry.veil.fabric.mixin.client;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import foundry.veil.Veil;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(ModelManager.class)
public class ModelManagerMixin {

    @ModifyExpressionValue(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/resources/model/ModelManager;VANILLA_ATLASES:Ljava/util/Map;", opcode = Opcodes.GETSTATIC))
    private static Map<ResourceLocation, ResourceLocation> addAtlases(Map<ResourceLocation, ResourceLocation> original) {
        final ImmutableMap.Builder<ResourceLocation, ResourceLocation> builder = ImmutableMap.builder();
        builder.putAll(original);
        builder.put(Veil.veilPath("textures/atlas/light_projection.png"), Veil.veilPath("light_projection"));
        return builder.build();
    }

}
