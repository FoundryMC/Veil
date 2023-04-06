package foundry.veil;

import com.mojang.math.Vector3f;
import foundry.veil.postprocessing.DynamicEffectInstance;
import foundry.veil.postprocessing.PostProcessingHandler;
import foundry.veil.postprocessing.PostProcessor;
import foundry.veil.test.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.joml.Random;

import java.util.List;
import java.util.Objects;

public class VeilFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Veil.init();

        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getItemInHand(hand);
            if(!player.level.isClientSide) return InteractionResultHolder.pass(stack);
            if(stack.getItem().equals(Items.RAW_GOLD)){
                PostProcessingEffectsRegistry.INSTANCES.forEach(instantiatedPostProcessor -> instantiatedPostProcessor.getFxInstances().forEach(DynamicEffectInstance::remove));
                Minecraft.getInstance().player.sendSystemMessage(Component.literal("Removed all effects"));
            }
            if(stack.getItem().equals(Items.AMETHYST_SHARD)){
                PostProcessingHandler.getInstances().stream().filter(Objects::nonNull).forEach(PostProcessor::init);
            }
            if (stack.getItem().equals(Items.ALLIUM)) {
                if (player.level.isClientSide) {
                    Vector3f pos = new Vector3f(player.position());
                    PostProcessingEffectsRegistry.BLOOM.addFxInstance(new BloomFx(List.of(
                            () -> Minecraft.getInstance().player.bob,
                            () -> (float)Minecraft.getInstance().player.totalExperience,
                            () -> (float)Minecraft.getInstance().player.experienceLevel,
                            () -> Minecraft.getInstance().player.sprintTime+0.0f,
                            () -> Minecraft.getInstance().player.getHealth(),
                            () -> Minecraft.getInstance().player.getMaxHealth(),
                            () -> Minecraft.getInstance().player.getAbsorptionAmount(),
                            pos::x,
                            pos::y,
                            pos::z,
                            () -> (float)player.position().x,
                            () -> (float)player.position().y,
                            () -> (float)player.position().z
                    )));
                }
            } else if (stack.getItem().equals(Items.POPPY)) {
                if (player.level.isClientSide) {
                    PostProcessingEffectsRegistry.OUTLINE.addFxInstance(new OutlineFx(new Vector3f(Vec3.atCenterOf(player.getOnPos()))) {
                    });

                }
            } else if (stack.getItem().equals(Items.AZURE_BLUET)) {
                if (player.level.isClientSide) {
                    PostProcessingEffectsRegistry.SCANLINE.addFxInstance(new BasicFx() {
                    });
                }
            } else if (stack.getItem().equals(Items.OXEYE_DAISY)) {
                if (player.level.isClientSide) {
                    PostProcessingEffectsRegistry.AREA.addFxInstance(new AreaFx(new Vector3f(Vec3.atCenterOf(player.getOnPos()))) {
                    });
                }
            }
            return InteractionResultHolder.pass(stack);
        });
    }
}
