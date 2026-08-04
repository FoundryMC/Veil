package foundry.veil.api.screenshake;

import foundry.veil.api.screenshake.type.ScreenShakeType;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class ScreenShakeManager {

    private final List<ScreenShakeType> screenShakes = new ArrayList<>();
    private final Vector3f accumulated = new Vector3f();
    private final Vector3f renderPosition = new Vector3f();

    /**
     * Add a screen shake to the camera.
     */
    public void addScreenShake(ScreenShakeType instance) {
        screenShakes.add(instance);
    }

    /**
     * Remove a screen shake from the camera.
     */
    public void removeScreenShake(ScreenShakeType instance) {
        screenShakes.remove(instance);
    }

    @ApiStatus.Internal
    public void tick() {
        this.renderPosition.set(this.accumulated);
        this.accumulated.set(0);

        for (ScreenShakeType screenShake : screenShakes) {
            screenShake.tick();
            this.accumulate(screenShake);
        }

        List.copyOf(screenShakes).forEach(shake -> {
            if (shake.isRemoved()) screenShakes.remove(shake);
        });
    }

    public Vector3f getPosition(float partialTick) {
        return this.renderPosition.lerp(this.accumulated, partialTick, new Vector3f());
    }

    private void accumulate(ScreenShakeType instance) {
        this.accumulated.add(instance.getPositionOffset());
    }
}
