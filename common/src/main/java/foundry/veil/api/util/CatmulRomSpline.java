package foundry.veil.api.util;

import net.minecraft.core.Position;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * Generates a spline from control points without allocating new objects. Supports JOML and MojMath inputs.
 *
 * @author Ocelot
 * @since 1.1.0
 */
public final class CatmulRomSpline {

    private final Iter iter;

    public CatmulRomSpline() {
        this.iter = new Iter();
    }

    /**
     * Iterates through every point, generating intermediate catmul-rom spline segments.
     *
     * @param controlPoints The control points
     * @param numSegments   The number of segments to split into
     * @return An Iterable with all points
     */
    public Iterable<Vector3dc> generateJomlSpline(Collection<? extends Vector3dc> controlPoints, int numSegments) {
        if (controlPoints.size() < 4) {
            return Collections.emptyList();
        }
        this.iter.setJoml(controlPoints.iterator(), numSegments);
        return () -> this.iter;
    }

    /**
     * Iterates through every point, generating intermediate catmul-rom spline segments.
     *
     * @param controlPoints The control points
     * @param numSegments   The number of segments to split into
     * @return An Iterable with all points
     */
    public Iterable<Vector3dc> generateSpline(Collection<? extends Position> controlPoints, int numSegments) {
        if (controlPoints.size() < 4) {
            return Collections.emptyList();
        }
        this.iter.set(controlPoints.iterator(), numSegments);
        return () -> this.iter;
    }

    @SuppressWarnings("rawtypes")
    private static class Iter implements Iterator<Vector3dc> {

        private final Vector3d value;
        private Iterator points;
        private Object point0;
        private Object point1;
        private Object point2;
        private Object point3;
        private boolean joml;
        private int segments;
        private int currentSegment;

        private Iter() {
            this.value = new Vector3d();
            this.points = null;
            this.joml = false;
        }

        @SuppressWarnings("rawtypes")
        public void setJoml(Iterator points, int segments) {
            this.points = points;
            this.point0 = points.next();
            this.point1 = points.next();
            this.point2 = points.next();
            this.point3 = points.next();
            this.joml = true;
            this.segments = segments;
            this.currentSegment = 0;
        }

        @SuppressWarnings("rawtypes")
        public void set(Iterator points, int segments) {
            this.points = points;
            this.point0 = points.next();
            this.point1 = points.next();
            this.point2 = points.next();
            this.point3 = points.next();
            this.joml = false;
            this.segments = segments;
            this.currentSegment = 0;
        }

        @Override
        public boolean hasNext() {
            return this.currentSegment < this.segments && this.points.hasNext();
        }

        @Override
        public Vector3dc next() {
            if (this.currentSegment >= this.segments) {
                this.currentSegment = 0;
                this.point0 = this.point1;
                this.point1 = this.point2;
                this.point2 = this.point3;
                this.point3 = this.points.next();
            }

            double t = this.currentSegment / (double) this.segments;
            double t2 = t * t;
            double t3 = t2 * t;

            double a = -0.5 * t3 + t2 - 0.5 * t;
            double b = 1.5 * t3 - 2.5 * t2 + 1;
            double c = -1.5 * t3 + 2 * t2 + 0.5 * t;
            double d = 0.5 * t3 - 0.5 * t2;

            if (this.joml) {
                Vector3dc p0 = (Vector3dc) this.point0;
                Vector3dc p1 = (Vector3dc) this.point1;
                Vector3dc p2 = (Vector3dc) this.point2;
                Vector3dc p3 = (Vector3dc) this.point3;
                this.value.set(
                        p0.x() * a + p1.x() * b + p2.x() * c + p3.x() * d,
                        p0.y() * a + p1.y() * b + p2.y() * c + p3.y() * d,
                        p0.z() * a + p1.z() * b + p2.z() * c + p3.z() * d
                );
            } else {
                Position p0 = (Position) this.point0;
                Position p1 = (Position) this.point1;
                Position p2 = (Position) this.point2;
                Position p3 = (Position) this.point3;
                this.value.set(
                        p0.x() * a + p1.x() * b + p2.x() * c + p3.x() * d,
                        p0.y() * a + p1.y() * b + p2.y() * c + p3.y() * d,
                        p0.z() * a + p1.z() * b + p2.z() * c + p3.z() * d
                );
            }

            this.currentSegment++;
            return this.value;
        }
    }
}
