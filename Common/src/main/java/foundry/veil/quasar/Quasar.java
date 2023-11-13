package foundry.veil.quasar;

import foundry.veil.quasar.emitters.ParticleSystemManager;
import foundry.veil.quasar.emitters.modules.ModuleType;
import foundry.veil.quasar.net.DNDNetworking;
import foundry.veil.quasar.registry.AllParticleTypes;
import com.google.gson.Gson;
import com.mojang.logging.LogUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Quasar.MODID)
public class Quasar {

    public static final Gson GSON = new Gson();
    public static final String MODID = "quasar";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Quasar() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;
        MinecraftForge.EVENT_BUS.register(this);
        AllParticleTypes.register(modEventBus);
        ModuleType.bootstrap();
        DNDNetworking.init();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> QuasarClient.onCtorClient(modEventBus, forgeEventBus));
    }


    @SubscribeEvent
    public void tick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.START && event.side.isClient()) {
            ParticleSystemManager.getInstance().tick();
        }
    }

    public Vec3 getColor(Level level){
        if(Math.random() < 0.25) {
            return new Vec3(1,1,1);
        }
        return new Vec3(104 * (1-(Math.random() * 0.1)), 52 * (1-(Math.random() * 0.1)), 235 * (1-(Math.random() * 0.1)));
    }
}
