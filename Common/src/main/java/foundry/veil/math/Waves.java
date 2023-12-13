package foundry.veil.math;

public class Waves {
    public static double approxGerstner(float x) {
        return 1 - Math.sqrt(2 * (Math.sin(2 * x) + 1)) / 2;
    }
}
