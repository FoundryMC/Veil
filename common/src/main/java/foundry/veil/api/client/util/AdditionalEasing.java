package foundry.veil.api.client.util;

import net.minecraft.util.Mth;

public enum AdditionalEasing {

    EASE_OUT_IN_QUAD {
        public float ease(float x) {
            return x < 0.5 ? 1 - Mth.square(x - 1) : 1 + Mth.square(x - 1);
        }
    },
    EASE_OUT_IN_CUBIC {
        public float ease(float x) {
            return 1 - (float) Math.pow(x - 1, 3);
        }
    },
    EASE_OUT_IN_QUART {
        public float ease(float x) {
            return x < 0.5 ? 1 - (float) Math.pow(x - 1, 4) : 1 + (float) Math.pow(x - 1, 4);
        }
    },
    EASE_OUT_IN_QUINT {
        public float ease(float x) {
            return 1 - (float) Math.pow(x - 1, 5);
        }
    };

    public abstract float ease(float x);

}
