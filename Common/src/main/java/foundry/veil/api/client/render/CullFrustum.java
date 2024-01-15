package foundry.veil.api.client.render;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2dc;
import org.joml.Vector2fc;
import org.joml.Vector3dc;
import org.joml.Vector3fc;

public interface CullFrustum {

    /**
     * Test whether the given point is within the frustum defined by <code>this</code> frustum culler.
     *
     * @param point the point to test
     * @return <code>true</code> if the given point is inside the frustum; <code>false</code> otherwise
     */
    default boolean testPoint(Vector3fc point) {
        return this.testPoint(point.x(), point.y(), point.z());
    }

    /**
     * Test whether the given point is within the frustum defined by <code>this</code> frustum culler.
     *
     * @param point the point to test
     * @return <code>true</code> if the given point is inside the frustum; <code>false</code> otherwise
     */
    default boolean testPoint(Vector3dc point) {
        return this.testPoint(point.x(), point.y(), point.z());
    }

    /**
     * Test whether the given point is within the frustum defined by <code>this</code> frustum culler.
     *
     * @param point the point to test
     * @return <code>true</code> if the given point is inside the frustum; <code>false</code> otherwise
     */
    default boolean testPoint(Vec3 point) {
        return this.testPoint(point.x(), point.y(), point.z());
    }

    /**
     * Test whether the given point <code>(x, y, z)</code> is within the frustum defined by <code>this</code> frustum culler.
     *
     * @param x the x-coordinate of the point
     * @param y the y-coordinate of the point
     * @param z the z-coordinate of the point
     * @return <code>true</code> if the given point is inside the frustum; <code>false</code> otherwise
     */
    boolean testPoint(double x, double y, double z);

    /**
     * Test whether the given sphere is partly or completely within or outside of the frustum defined by <code>this</code> frustum culler.
     * <p>
     * The algorithm implemented by this method is conservative. This means that in certain circumstances a <i>false positive</i>
     * can occur, when the method returns <code>true</code> for spheres that do not intersect the frustum.
     * See <a href="http://iquilezles.org/www/articles/frustumcorrect/frustumcorrect.htm">iquilezles.org</a> for an examination of this problem.
     *
     * @param center the sphere's center
     * @param radius the sphere's radius
     * @return <code>true</code> if the given sphere is partly or completely inside the frustum;
     * <code>false</code> otherwise
     */
    default boolean testSphere(Vector3dc center, float radius) {
        return this.testSphere(center.x(), center.y(), center.z(), radius);
    }

    /**
     * Test whether the given sphere is partly or completely within or outside of the frustum defined by <code>this</code> frustum culler.
     * <p>
     * The algorithm implemented by this method is conservative. This means that in certain circumstances a <i>false positive</i>
     * can occur, when the method returns <code>true</code> for spheres that do not intersect the frustum.
     * See <a href="http://iquilezles.org/www/articles/frustumcorrect/frustumcorrect.htm">iquilezles.org</a> for an examination of this problem.
     *
     * @param center the sphere's center
     * @param radius the sphere's radius
     * @return <code>true</code> if the given sphere is partly or completely inside the frustum;
     * <code>false</code> otherwise
     */
    default boolean testSphere(Vector3fc center, float radius) {
        return this.testSphere(center.x(), center.y(), center.z(), radius);
    }

    /**
     * Test whether the given sphere is partly or completely within or outside of the frustum defined by <code>this</code> frustum culler.
     * <p>
     * The algorithm implemented by this method is conservative. This means that in certain circumstances a <i>false positive</i>
     * can occur, when the method returns <code>true</code> for spheres that do not intersect the frustum.
     * See <a href="http://iquilezles.org/www/articles/frustumcorrect/frustumcorrect.htm">iquilezles.org</a> for an examination of this problem.
     *
     * @param x the x-coordinate of the sphere's center
     * @param y the y-coordinate of the sphere's center
     * @param z the z-coordinate of the sphere's center
     * @param r the sphere's radius
     * @return <code>true</code> if the given sphere is partly or completely inside the frustum;
     * <code>false</code> otherwise
     */
    boolean testSphere(double x, double y, double z, float r);

    /**
     * Test whether the given axis-aligned box is partly or completely within or outside of the frustum defined by <code>this</code> frustum culler.
     * The box is specified via its <code>min</code> and <code>max</code> corner coordinates.
     * <p>
     * The algorithm implemented by this method is conservative. This means that in certain circumstances a <i>false positive</i>
     * can occur, when the method returns <code>true</code> for boxes that do not intersect the frustum.
     * See <a href="http://iquilezles.org/www/articles/frustumcorrect/frustumcorrect.htm">iquilezles.org</a> for an examination of this problem.
     *
     * @param aabb the axis-aligned box
     * @return <code>true</code> if the axis-aligned box is completely or partly inside of the frustum; <code>false</code> otherwise
     */
    boolean testAab(AABB aabb);

    /**
     * Test whether the given axis-aligned box is partly or completely within or outside of the frustum defined by <code>this</code> frustum culler.
     * The box is specified via its <code>min</code> and <code>max</code> corner coordinates.
     * <p>
     * The algorithm implemented by this method is conservative. This means that in certain circumstances a <i>false positive</i>
     * can occur, when the method returns <code>true</code> for boxes that do not intersect the frustum.
     * See <a href="http://iquilezles.org/www/articles/frustumcorrect/frustumcorrect.htm">iquilezles.org</a> for an examination of this problem.
     *
     * @param min the minimum corner coordinates of the axis-aligned box
     * @param max the maximum corner coordinates of the axis-aligned box
     * @return <code>true</code> if the axis-aligned box is completely or partly inside of the frustum; <code>false</code> otherwise
     */
    default boolean testAab(Vector3dc min, Vector3dc max) {
        return this.testAab(min.x(), min.y(), min.z(), max.x(), max.y(), max.z());
    }

    /**
     * Test whether the given axis-aligned box is partly or completely within or outside of the frustum defined by <code>this</code> frustum culler.
     * The box is specified via its <code>min</code> and <code>max</code> corner coordinates.
     * <p>
     * The algorithm implemented by this method is conservative. This means that in certain circumstances a <i>false positive</i>
     * can occur, when the method returns <code>true</code> for boxes that do not intersect the frustum.
     * See <a href="http://iquilezles.org/www/articles/frustumcorrect/frustumcorrect.htm">iquilezles.org</a> for an examination of this problem.
     *
     * @param min the minimum corner coordinates of the axis-aligned box
     * @param max the maximum corner coordinates of the axis-aligned box
     * @return <code>true</code> if the axis-aligned box is completely or partly inside of the frustum; <code>false</code> otherwise
     */
    default boolean testAab(Vector3fc min, Vector3fc max) {
        return this.testAab(min.x(), min.y(), min.z(), max.x(), max.y(), max.z());
    }

    /**
     * Test whether the given axis-aligned box is partly or completely within or outside of the frustum defined by <code>this</code> frustum culler.
     * The box is specified via its min and max corner coordinates.
     * <p>
     * The algorithm implemented by this method is conservative. This means that in certain circumstances a <i>false positive</i>
     * can occur, when the method returns <code>true</code> for boxes that do not intersect the frustum.
     * See <a href="http://iquilezles.org/www/articles/frustumcorrect/frustumcorrect.htm">iquilezles.org</a> for an examination of this problem.
     * <p>
     * Reference: <a href="http://old.cescg.org/CESCG-2002/DSykoraJJelinek/">Efficient View Frustum Culling</a>
     *
     * @param minX the x-coordinate of the minimum corner
     * @param minY the y-coordinate of the minimum corner
     * @param minZ the z-coordinate of the minimum corner
     * @param maxX the x-coordinate of the maximum corner
     * @param maxY the y-coordinate of the maximum corner
     * @param maxZ the z-coordinate of the maximum corner
     * @return <code>true</code> if the axis-aligned box is completely or partly inside of the frustum; <code>false</code> otherwise
     */
    boolean testAab(double minX, double minY, double minZ, double maxX, double maxY, double maxZ);

    /**
     * Test whether the given XY-plane (at <code>Z = 0</code>) is partly or completely within or outside of the frustum defined by <code>this</code> frustum culler.
     * The plane is specified via its <code>min</code> and <code>max</code> corner coordinates.
     * <p>
     * The algorithm implemented by this method is conservative. This means that in certain circumstances a <i>false positive</i>
     * can occur, when the method returns <code>true</code> for planes that do not intersect the frustum.
     * See <a href="http://iquilezles.org/www/articles/frustumcorrect/frustumcorrect.htm">iquilezles.org</a> for an examination of this problem.
     *
     * @param min the minimum corner coordinates of the XY-plane
     * @param max the maximum corner coordinates of the XY-plane
     * @return <code>true</code> if the XY-plane is completely or partly inside of the frustum; <code>false</code> otherwise
     */
    default boolean testPlaneXY(Vector2dc min, Vector2dc max) {
        return this.testPlaneXY(min.x(), min.y(), max.x(), max.y());
    }

    /**
     * Test whether the given XY-plane (at <code>Z = 0</code>) is partly or completely within or outside of the frustum defined by <code>this</code> frustum culler.
     * The plane is specified via its <code>min</code> and <code>max</code> corner coordinates.
     * <p>
     * The algorithm implemented by this method is conservative. This means that in certain circumstances a <i>false positive</i>
     * can occur, when the method returns <code>true</code> for planes that do not intersect the frustum.
     * See <a href="http://iquilezles.org/www/articles/frustumcorrect/frustumcorrect.htm">iquilezles.org</a> for an examination of this problem.
     *
     * @param min the minimum corner coordinates of the XY-plane
     * @param max the maximum corner coordinates of the XY-plane
     * @return <code>true</code> if the XY-plane is completely or partly inside of the frustum; <code>false</code> otherwise
     */
    default boolean testPlaneXY(Vector2fc min, Vector2fc max) {
        return this.testPlaneXY(min.x(), min.y(), max.x(), max.y());
    }

    /**
     * Test whether the given XY-plane (at <code>Z = 0</code>) is partly or completely within or outside of the frustum defined by <code>this</code> frustum culler.
     * The plane is specified via its min and max corner coordinates.
     * <p>
     * The algorithm implemented by this method is conservative. This means that in certain circumstances a <i>false positive</i>
     * can occur, when the method returns <code>true</code> for planes that do not intersect the frustum.
     * See <a href="http://iquilezles.org/www/articles/frustumcorrect/frustumcorrect.htm">iquilezles.org</a> for an examination of this problem.
     * <p>
     * Reference: <a href="http://old.cescg.org/CESCG-2002/DSykoraJJelinek/">Efficient View Frustum Culling</a>
     *
     * @param minX the x-coordinate of the minimum corner
     * @param minY the y-coordinate of the minimum corner
     * @param maxX the x-coordinate of the maximum corner
     * @param maxY the y-coordinate of the maximum corner
     * @return <code>true</code> if the XY-plane is completely or partly inside of the frustum; <code>false</code> otherwise
     */
    boolean testPlaneXY(double minX, double minY, double maxX, double maxY);

    /**
     * Test whether the given XZ-plane (at <code>Y = 0</code>) is partly or completely within or outside of the frustum defined by <code>this</code> frustum culler.
     * The plane is specified via its min and max corner coordinates.
     * <p>
     * The algorithm implemented by this method is conservative. This means that in certain circumstances a <i>false positive</i>
     * can occur, when the method returns <code>true</code> for planes that do not intersect the frustum.
     * See <a href="http://iquilezles.org/www/articles/frustumcorrect/frustumcorrect.htm">iquilezles.org</a> for an examination of this problem.
     * <p>
     * Reference: <a href="http://old.cescg.org/CESCG-2002/DSykoraJJelinek/">Efficient View Frustum Culling</a>
     *
     * @param minX the x-coordinate of the minimum corner
     * @param minZ the z-coordinate of the minimum corner
     * @param maxX the x-coordinate of the maximum corner
     * @param maxZ the z-coordinate of the maximum corner
     * @return <code>true</code> if the XZ-plane is completely or partly inside of the frustum; <code>false</code> otherwise
     */
    boolean testPlaneXZ(double minX, float minZ, float maxX, float maxZ);

    /**
     * Test whether the given line segment, defined by the end points <code>a</code> and <code>b</code>,
     * is partly or completely within the frustum defined by <code>this</code> frustum culler.
     *
     * @param a the line segment's first end point
     * @param b the line segment's second end point
     * @return <code>true</code> if the given line segment is partly or completely inside the frustum;
     * <code>false</code> otherwise
     */
    default boolean testLineSegment(Vector3dc a, Vector3dc b) {
        return this.testLineSegment(a.x(), a.y(), a.z(), b.x(), b.y(), b.z());
    }

    /**
     * Test whether the given line segment, defined by the end points <code>a</code> and <code>b</code>,
     * is partly or completely within the frustum defined by <code>this</code> frustum culler.
     *
     * @param a the line segment's first end point
     * @param b the line segment's second end point
     * @return <code>true</code> if the given line segment is partly or completely inside the frustum;
     * <code>false</code> otherwise
     */
    default boolean testLineSegment(Vector3fc a, Vector3fc b) {
        return this.testLineSegment(a.x(), a.y(), a.z(), b.x(), b.y(), b.z());
    }

    /**
     * Test whether the given line segment, defined by the end points <code>(aX, aY, aZ)</code> and <code>(bX, bY, bZ)</code>,
     * is partly or completely within the frustum defined by <code>this</code> frustum culler.
     *
     * @param aX the x coordinate of the line segment's first end point
     * @param aY the y coordinate of the line segment's first end point
     * @param aZ the z coordinate of the line segment's first end point
     * @param bX the x coordinate of the line segment's second end point
     * @param bY the y coordinate of the line segment's second end point
     * @param bZ the z coordinate of the line segment's second end point
     * @return <code>true</code> if the given line segment is partly or completely inside the frustum;
     * <code>false</code> otherwise
     */
    boolean testLineSegment(double aX, double aY, double aZ, double bX, double bY, double bZ);
}
