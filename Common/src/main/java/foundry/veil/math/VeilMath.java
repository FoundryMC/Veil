package foundry.veil.math;

public class VeilMath {
    public static double remap(double value, double from1, double to1, double from2, double to2) {
        return (value - from1) / (to1 - from1) * (to2 - from2) + from2;
    }
}
