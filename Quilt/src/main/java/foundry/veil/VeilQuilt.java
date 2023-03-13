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
        }
    }
}
