package foundry.veil.api.client.util;

public abstract sealed class EasingWrapper permits EasingWrapper.EasingsDotNet, EasingWrapper.Additional {

    public abstract float ease(float x);

    public abstract String name();

    public static final class EasingsDotNet extends EasingWrapper {
        public final Easing easing;

        public EasingsDotNet(Easing easing) {
            this.easing = easing;
        }

        @Override
        public float ease(float x) {
            return easing.ease(x);
        }

        @Override
        public String name() {
            return easing.name();
        }
    }

    public static final class Additional extends EasingWrapper {
        public final AdditionalEasing easing;

        public Additional(AdditionalEasing easing) {
            this.easing = easing;
        }

        @Override
        public float ease(float x) {
            return easing.ease(x);
        }

        @Override
        public String name() {
            return easing.name();
        }
    }
}
