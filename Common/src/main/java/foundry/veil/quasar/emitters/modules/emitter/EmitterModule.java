package foundry.veil.quasar.emitters.modules.emitter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.Vec3;

public class EmitterModule implements BaseEmitterModule {
    public static Codec<EmitterModule> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Codec.INT.fieldOf("max_lifetime").forGetter(EmitterModule::getMaxLifetime),
                Codec.BOOL.fieldOf("loop").forGetter(EmitterModule::getLoop),
                Codec.INT.fieldOf("rate").forGetter(EmitterModule::getRate),
                Codec.INT.fieldOf("count").forGetter(EmitterModule::getCount)
        ).apply(instance, EmitterModule::new);
    });
    /**
     * Position of the emitter
     */
    Vec3 position;
    /**
     * Maximum number of ticks the emitter will be active for
     */
    int maxLifetime;
    /**
     * Current number of ticks the emitter has been active for
     */
    int currentLifetime = 0;
    /**
     * Whether or not the emitter will loop. If true, the emitter will reset after maxLifetime ticks
     */
    boolean loop = false;
    /**
     * The rate at which particles are emitted. <count> particles per <rate> ticks.
     * E.G. rate = 2, count = 1 means 1 particle every 2 ticks
     */
    int rate = 1;
    public int baseRate = rate;
    /**
     * The number of particles emitted per <rate> ticks
     */
    int count = 1;
    public int baseCount = count;

    /**
     * Whether or not the emitter has completed its lifetime
     */
    boolean complete = false;

    /**
     * Constructs a new emitter module
     *
     * @param position    Position of the emitter
     * @param maxLifetime Maximum number of ticks the emitter will be active for
     * @param loop        Whether or not the emitter will loop. If true, the emitter will reset after maxLifetime ticks
     * @param rate        The rate at which particles are emitted. count particles per rate ticks.
     * @param count       The number of particles emitted per rate ticks
     */
    public EmitterModule(Vec3 position, int maxLifetime, boolean loop, int rate, int count) {
        this.position = position;
        this.maxLifetime = maxLifetime;
        this.loop = loop;
        this.rate = rate;
        this.count = count;
        this.baseRate = rate;
        this.baseCount = count;
    }

    private EmitterModule(int maxLifetime, boolean loop, int rate, int count) {
        this.maxLifetime = maxLifetime;
        this.loop = loop;
        this.rate = rate;
        this.count = count;
        this.baseRate = rate;
        this.baseCount = count;
    }

    /**
     * Tick the emitter. This is run to track the basic functionality of the emitter.
     */
    public void tick(Runnable action) {
        currentLifetime++;
        action.run();
        if (currentLifetime >= maxLifetime) {
            if (loop) {
                currentLifetime = 0;
            } else {
                complete = true;
            }
        }
    }

    /**
     * Whether or not the emitter has completed its lifetime
     */
    public boolean isComplete() {
        return complete;
    }

    /**
     * Resets the emitter to its initial state
     */
    public void reset() {
        currentLifetime = 0;
        complete = false;
    }

    public EmitterModule instance(){
        return new EmitterModule(maxLifetime, loop, rate, count);
    }

    /**
     * Position of the emitter
     */
    public Vec3 getPosition() {
        return position;
    }

    /**
     * The rate at which particles are emitted. count particles per rate ticks.
     */
    public int getRate() {
        return rate;
    }

    /**
     * The number of particles emitted per rate ticks
     */
    public int getCount() {
        return count;
    }

    /**
     * Number of ticks the emitter has been active for
     */
    public int getCurrentLifetime() {
        return currentLifetime;
    }

    /**
     * Maximum number of ticks the emitter will be active for
     */
    public int getMaxLifetime() {
        return maxLifetime;
    }

    /**
     * Whether or not the emitter will loop. If true, the emitter will reset after maxLifetime ticks
     */
    public boolean getLoop() {
        return loop;
    }

    /**
     * Whether or not the emitter has completed its lifetime
     */
    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    /**
     * Set the position of the emitter
     */
    public void setPosition(Vec3 position) {
        this.position = position;
    }

    /**
     * Set the rate at which particles are emitted. count particles per rate ticks.
     */
    public void setRate(int rate) {
        this.rate = rate;
    }

    /**
     * Set the number of particles emitted per rate ticks
     */
    public void setCount(int count) {
        this.count = count;
    }

    public void setMaxLifetime(int lifetime) {
        this.maxLifetime = lifetime;
    }

    public void setLoop(boolean b) {
        this.loop = b;
    }
}
