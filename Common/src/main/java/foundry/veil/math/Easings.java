package foundry.veil.math;

public class Easings {
    public static float ease(float x, Easing eType){
        return eType.ease(x);
    }

    public enum Easing implements IEasing {
        easeInOutBack {
            public float ease(float x) {
                float c1 = 1.70158F;
                float c2 = c1 * 1.525F;
                return (float) (x < 0.5 ? (Math.pow(2 * x, 2) * ((c2 + 1) * 2 * x - c2)) / 2 : (Math.pow(2 * x - 2, 2) * ((c2 + 1) * (x * 2 - 2) + c2) + 2) / 2);
            }
        },
        easeOutCubic{
            public float ease(float x) {
                return (float) ((float) 1 - Math.pow(1 - x, 3));
            }
        },
        easeOutQuart{
            public float ease(float x) {
                return (float) (1 - Math.pow(1 - x, 4));
            }
        },
        easeOutQuint{
            public float ease(float x) {
                return (float) (1 - Math.pow(1 - x, 5));
            }
        },
        easeOutSine{
            public float ease(float x) {
                return (float) Math.sin(x * (Math.PI / 2));
            }
        },
        easeOutExpo{
            public float ease(float x) {
                return (float) (x == 1 ? 1 : 1 - Math.pow(2, -10 * x));
            }
        },
        easeOutCirc{
            public float ease(float x) {
                return (float) Math.sqrt(1 - Math.pow(x - 1, 2));
            }
        },
        easeOutBack{
            public float ease(float x) {
                float c1 = 1.70158F;
                float c3 = c1 + 1;
                return (float) (1 + c3 * Math.pow(x - 1, 3) + c1 * Math.pow(x - 1, 2));
            }
        },
        easeOutElastic{
            public float ease(float x) {
                float c4 = (float) (2 * Math.PI / 3);
                return x == 0 ? 0 : x == 1 ? 1 : (float) Math.pow(2, -10 * x) * (float) Math.sin((x * 10 - 0.75F) * c4) + 1;
            }
        },
        easeOutBounce{
            public float ease(float x) {
                float n1 = 7.5625F;
                float d1 = 2.75F;
                if (x < 1 / d1) {
                    return n1 * x * x;
                } else if (x < 2 / d1) {
                    return n1 * (x -= 1.5F / d1) * x + 0.75F;
                } else if (x < 2.5 / d1) {
                    return n1 * (x -= 2.25F / d1) * x + 0.9375F;
                } else {
                    return n1 * (x -= 2.625F / d1) * x + 0.984375F;
                }
            }
        },
        easeInQuad{
            public float ease(float x) {
                return x * x;
            }
        },
        easeInCubic{
            public float ease(float x) {
                return x * x * x;
            }
        },
        easeInQuart{
            public float ease(float x) {
                return x * x * x * x;
            }
        },
        easeInQuint{
            public float ease(float x) {
                return x * x * x * x * x;
            }
        },
        easeInSine{
            public float ease(float x) {
                return (float) (1 - Math.cos(x * (Math.PI / 2)));
            }
        },
        easeInExpo{
            public float ease(float x) {
                return (float) (x == 0 ? 0 : Math.pow(2, 10 * x - 10));
            }
        },
        easeInCirc{
            public float ease(float x) {
                return (float) -(Math.sqrt(1 - x * x) - 1);
            }
        },
        easeInBack{
            public float ease(float x) {
                float c1 = 1.70158F;
                float c3 = c1 + 1;
                return (float) (c3 * x * x * x - c1 * x * x);
            }
        },
        easeInElastic{
            public float ease(float x) {
                float c4 = (float) (2 * Math.PI / 3);
                return x == 0 ? 0 : x == 1 ? 1 : (float) -Math.pow(2, 10 * x - 10) * (float) Math.sin((x * 10 - 0.75F) * c4);
            }
        },
        easeInBounce{
            public float ease(float x) {
                return 1 - Easings.Easing.easeOutBounce.ease(1 - x);
            }
        },
        easeInOutQuad{
            public float ease(float x) {
                return x < 0.5 ? 2 * x * x : 1 - (float) Math.pow(-2 * x + 2, 2) / 2;
            }
        },
        easeInOutCubic{
            public float ease(float x) {
                return x < 0.5 ? 4 * x * x * x : 1 - (float) Math.pow(-2 * x + 2, 3) / 2;
            }
        },
        easeInOutQuart{
            public float ease(float x) {
                return x < 0.5 ? 8 * x * x * x * x : 1 - (float) Math.pow(-2 * x + 2, 4) / 2;
            }
        },
        easeInOutQuint{
            public float ease(float x) {
                return x < 0.5 ? 16 * x * x * x * x * x : 1 - (float) Math.pow(-2 * x + 2, 5) / 2;
            }
        },
        easeInOutSine{
            public float ease(float x) {
                return (float) (-0.5 * (Math.cos(Math.PI * x) - 1));
            }
        },
        easeInOutExpo{
            public float ease(float x) {
                return x == 0 ? 0 : x == 1 ? 1 : x < 0.5 ? (float) Math.pow(2, 20 * x - 10) / 2 : (float) (2 - Math.pow(2, -20 * x + 10) / 2);
            }
        },
        easeInOutCirc{
            public float ease(float x) {
                return x < 0.5 ? (float) (-(Math.sqrt(1 - (float) Math.pow(2 * x, 2)) - 1) / 2) : (float) ((Math.sqrt(1 - (float) Math.pow(-2 * x + 2, 2)) + 1) / 2);
            }
        },

    }

    public interface IEasing {
        float ease(float x);
    }
}
