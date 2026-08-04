package foundry.veil.impl.screenshake;

import foundry.veil.api.client.util.Easing;
import gg.moonflower.molangcompiler.api.MolangExpression;
import gg.moonflower.molangcompiler.api.exception.MolangSyntaxException;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Screen shake that originates from a point in the world, strength decreasing with distance.
 */
public class LocalScreenShake extends GlobalScreenShake {

    private final float radius;
    private final Easing falloff;
    private final Vec3 position;

    public LocalScreenShake(String expression, Vec3 position, int length, float radius, Easing falloff) throws MolangSyntaxException {
        super(expression, length);
        this.radius = radius;
        this.falloff = falloff;
        this.position = position;
    }

    public LocalScreenShake(MolangExpression expression, Vec3 position, int length, float radius, Easing falloff) {
        super(expression, length);
        this.radius = radius;
        this.falloff = falloff;
        this.position = position;
    }

    @Override
    protected float getStrength() {
        float strength = super.getStrength();
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        float distanceMultiplier = Mth.clamp( (float) (1.0f - (camera.getPosition().distanceTo(this.position) / this.radius)), 0, 1);
        return falloff.ease(distanceMultiplier) * strength;
    }
}
