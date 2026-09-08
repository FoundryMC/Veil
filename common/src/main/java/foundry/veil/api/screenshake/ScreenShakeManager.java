package foundry.veil.api.screenshake;

import foundry.veil.api.screenshake.type.ScreenShakeType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.joml.Vector3f;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @since 4.5.0
 */
public final class ScreenShakeManager {

    private final List<ScreenShakeType> screenShakes = new LinkedList<>();
    private final Vector3f accumulated = new Vector3f();
    private final Vector3f renderPosition = new Vector3f();

    /**
     * Add a screen shake to the camera.
     */
    public void addScreenShake(ScreenShakeType instance) {
        this.screenShakes.add(instance);
    }

    /**
     * Remove a screen shake from the camera.
     */
    public void removeScreenShake(ScreenShakeType instance) {
        this.screenShakes.remove(instance);
    }

    @ApiStatus.Internal
    public void tick() {
        this.renderPosition.set(this.accumulated);
        this.accumulated.set(0);

        if (this.screenShakes.isEmpty()) {
            return;
        }

        Iterator<ScreenShakeType> iterator = this.screenShakes.iterator();
        while (iterator.hasNext()) {
            ScreenShakeType screenShake = iterator.next();
            screenShake.tick();
            this.accumulated.add(screenShake.getPositionOffset());
            if (screenShake.isRemoved()) {
                iterator.remove();
            }
        }
    }

    @Contract("_->new")
    public Vector3f getPosition(float partialTick) {
        return this.renderPosition.lerp(this.accumulated, partialTick, new Vector3f());
    }
}
