package foundry.veil.math;

import net.minecraft.util.Mth;

public class Easings {
    public static float ease(float x, Easing eType){
        return eType.ease(x);
    }

    public enum Easing implements IEasing {
        linear {
            public float ease(float x) { return x;}
        },
        easeInQuad {
            public float ease(float x) {
                return x * x;
            }
        },
        easeOutQuad {
            public float ease(float x) {
                return 1 - (1 - x) * (1 - x);
            }
        },
        easeInOutQuad {
            public float ease(float x) {
                return x < 0.5 ? 2 * x * x : (float) (1 - Math.pow(-2 * x + 2, 2) / 2);
            }
        },
        easeInCubic {
            public float ease(float x) {
                return x * x * x;
            }
        },
        easeOutCubic {
            public float ease(float x) {
                return (float) (1 - Math.pow(1 - x, 3));
            }
        },
        easeInOutCubic {
            public float ease(float x) {
                return x < 0.5 ? 4 * x * x * x : (float) (1 - Math.pow(-2 * x + 2, 3) / 2);
            }
        },
        easeInQuart {
            public float ease(float x) {
                return x * x * x * x;
            }
        },
        easeOutQuart {
            public float ease(float x) {
                return (float) (1 - Math.pow(1 - x, 4));
            }
        },
        easeInOutQuart {
            public float ease(float x) {
                return x < 0.5 ? 8 * x * x * x * x : (float) (1 - Math.pow(-2 * x + 2, 4) / 2);
            }
        },
        easeInQuint {
            public float ease(float x) {
                return x * x * x * x * x;
            }
        },
        easeOutQuint {
            public float ease(float x) {
                return (float) (1 - Math.pow(1 - x, 5));
            }
        },
        easeInOutQuint {
            public float ease(float x) {
                return x < 0.5 ? 16 * x * x * x * x * x : (float) (1 - Math.pow(-2 * x + 2, 5) / 2);
            }
        },
        easeInSine {
            public float ease(float x) {
                return 1 - Mth.cos((float) ((x * Math.PI) / 2));
            }
        },
        easeOutSine {
            public float ease(float x) {
                return Mth.sin((float) ((x * Math.PI) / 2));
            }
        },
        easeInOutSine {
            public float ease(float x) {
                return -(Mth.cos((float) (Math.PI * x)) - 1) / 2;
            }
        },
        easeInExpo {
            public float ease(float x) {
                return x == 0 ? 0 : (float) Math.pow(2, 10 * x - 10);
            }
        },
        easeOutExpo {
            public float ease(float x) {
                return x == 1 ? 1 : (float) (1 - Math.pow(2, -10 * x));
            }
        },
        easeInOutExpo {
            public float ease(float x) {
                return x == 0
                        ? 0
                        : (float) (x == 1
                        ? 1
                        : x < 0.5
                        ? Math.pow(2, 20 * x - 10) / 2
                        : (2 - Math.pow(2, -20 * x + 10)) / 2);
            }
        },
        easeInCirc {
            public float ease(float x) {
                return (float) (1 - Math.sqrt(1 - Math.pow(x, 2)));
            }
        },
        easeOutCirc {
            public float ease(float x) {
                return (float) Math.sqrt(1 - Math.pow(x - 1, 2));
            }
        },
        easeInOutCirc {
            public float ease(float x) {
                return (float) (x < 0.5 ? (1 - Math.sqrt(1 - Math.pow(2 * x, 2))) / 2 : (Math.sqrt(1 - Math.pow(-2 * x + 2, 2)) + 1) / 2);
            }
        },
        easeInBack {
            public float ease(float x) {
                return 2.70158F * x * x * x - 1.70158F * x * x;
            }
        },
        easeOutBack {
            public float ease(float x) {
                return (float) (1 + 2.70158F * Math.pow(x - 1, 3) + 1.70158F * Math.pow(x - 1, 2));
            }
        },
        easeInOutBack {
            public float ease(float x) {
                return (float) (x < 0.5
                        ? (Math.pow(2 * x, 2) * ((2.5949095F + 1) * 2 * x - 2.5949095F)) / 2
                        : (Math.pow(2 * x - 2, 2) * ((2.5949095F + 1) * (x * 2 - 2) + 2.5949095F) + 2) / 2);
            }
        },
        easeInElastic {
            public float ease(float x) {
                return x == 0
                        ? 0
                        : (float) (x == 1
                        ? 1
                        : -Math.pow(2, 10 * x - 10) * Mth.sin((float) ((x * 10 - 10.75) * ((2 * Math.PI) / 3))));
            }
        },
        easeOutElastic {
            public float ease(float x) {
                return x == 0
                        ? 0
                        : (float) (x == 1
                        ? 1
                        : Math.pow(2, -10 * x) * Mth.sin((float) ((x * 10 - 0.75) * ((2 * Math.PI) / 3))) + 1);
            }
        },
        easeInOutElastic {
            public float ease(float x) {
                return x == 0
                        ? 0
                        : (float) (x == 1
                        ? 1
                        : x < 0.5
                        ? -(Math.pow(2, 20 * x - 10) * Mth.sin((float) ((20 * x - 11.125) * ((2 * Math.PI) / 4.5)))) / 2
                        : (Math.pow(2, -20 * x + 10) * Mth.sin((float) ((20 * x - 11.125) * ((2 * Math.PI) / 4.5)))) / 2 + 1);
            }
        },
        easeInBounce {
            public float ease(float x) {
                return 1 - bounceOut(1 - x);
            }
        },
        easeOutBounce {
            public float ease(float x) {
                return 1 - bounceOut(1 - x);
            }
        },
        easeInOutBounce {
            public float ease(float x) {
                return x < 0.5
                        ? (1 - bounceOut(1 - 2 * x)) / 2
                        : (1 + bounceOut(2 * x - 1)) / 2;
            }
        };

        private static float bounceOut(float x) {
            float n1 = 7.5625F;
            float d1 = 2.75F;

            if (x < 1 / d1) {
                return n1 * x * x;
            } else if (x < 2 / d1) {
                return n1 * (x -= 1.5 / d1) * x + 0.75F;
            } else if (x < 2.5 / d1) {
                return n1 * (x -= 2.25 / d1) * x + 0.9375F;
            } else {
                return n1 * (x -= 2.625 / d1) * x + 0.984375F;
            }
        }
    }

    public interface IEasing {
        float ease(float x);
    }
}
