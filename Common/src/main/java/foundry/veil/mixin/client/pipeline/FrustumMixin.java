package foundry.veil.mixin.client.pipeline;

import foundry.veil.api.client.render.CullFrustum;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;
import org.joml.FrustumIntersection;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Frustum.class)
public abstract class FrustumMixin implements CullFrustum {

    @Shadow
    @Final
    private FrustumIntersection intersection;

    @Shadow
    private double camX;

    @Shadow
    private double camY;

    @Shadow
    private double camZ;

    @Shadow
    public abstract boolean isVisible(AABB aABB);

    @Shadow
    protected abstract boolean cubeInFrustum(double d, double e, double f, double g, double h, double i);

    @Unique
    private final Vector3d veil$position = new Vector3d();

    @Override
    public boolean testPoint(double x, double y, double z) {
        return this.intersection.testPoint((float) (x - this.camX), (float) (y - this.camY), (float) (z - this.camZ));
    }

    @Override
    public boolean testSphere(double x, double y, double z, float r) {
        return this.intersection.testSphere((float) (x - this.camX), (float) (y - this.camY), (float) (z - this.camZ), r);
    }

    @Override
    public boolean testAab(AABB aabb) {
        return this.isVisible(aabb);
    }

    @Override
    public boolean testAab(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return this.cubeInFrustum(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public boolean testPlaneXY(double minX, double minY, double maxX, double maxY) {
        return this.intersection.testPlaneXY((float) (minX - this.camX), (float) (minY - this.camY), (float) (maxX - this.camX), (float) (maxY - this.camY));
    }

    @Override
    public boolean testPlaneXZ(double minX, float minZ, float maxX, float maxZ) {
        return this.intersection.testPlaneXZ((float) (minX - this.camX), (float) (minZ - this.camZ), (float) (maxX - this.camX), (float) (maxZ - this.camZ));
    }

    @Override
    public boolean testLineSegment(double aX, double aY, double aZ, double bX, double bY, double bZ) {
        return this.intersection.testLineSegment((float) (aX - this.camX), (float) (aY - this.camY), (float) (aZ - this.camZ), (float) (bX - this.camX), (float) (bY - this.camY), (float) (bZ - this.camZ));
    }

    @Override
    public Vector3dc getPosition() {
        return this.veil$position.set(this.camX, this.camY, this.camZ);
    }
}
