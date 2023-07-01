package foundry.veil.render.ui;

public class VeilUITooltipHandler {
    private static boolean shouldShowTooltip = false;

    public static boolean shouldShowTooltip() {
        return shouldShowTooltip;
    }

    public static void setShouldShowTooltip(boolean shouldShowTooltip) {
        VeilUITooltipHandler.shouldShowTooltip = shouldShowTooltip;
    }
}
