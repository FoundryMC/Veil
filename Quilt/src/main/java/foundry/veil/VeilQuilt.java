package foundry.veil;

import com.mojang.math.Vector3f;
import foundry.veil.math.Easings;
import foundry.veil.test.EnergySphereFx;
import foundry.veil.test.PostProcessingEffectsRegistry;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public class VeilQuilt implements ModInitializer {
    @Override
    public void onInitialize(ModContainer mod) {
        Veil.init();
        if(QuiltLoader.isDevelopmentEnvironment()){
            UseItemCallback.EVENT.register((player, world, hand) -> {
                if(player.level.isClientSide){
                    if(player.getItemInHand(hand).is(Items.ALLIUM)){
                        PostProcessingEffectsRegistry.ENERGY_SPHERE.addFxInstance(new EnergySphereFx(new Vector3f(Vec3.atCenterOf(player.getOnPos())), 0, 1){
                            @Override
                            public void update(double deltaTime) {
                                super.update(deltaTime);

                                float t = getTime() / 7.5F;

                                if (t > 1) {
                                    remove();
                                    return;
                                }
                                t = Mth.lerp(Easings.ease(t, Easings.Easing.easeOutCirc), 0, 1);

                                this.radius = t * 300F;
                                this.intensity = (300F - radius) / 300F;
                                this.intensity = (float) Mth.clamp(intensity, 0., 1.);
                            }
                        });
                    }
                }
                return InteractionResultHolder.consume(player.getItemInHand(hand));
            });
        }
    }
}
