package dev.wutshy.focuscrosshair.animation;

import dev.wutshy.focuscrosshair.config.FocusConfig;

public final class FocusTargets {
    private FocusTargets() {}

    public static double scale(FocusConfig c, int kind, double distance) {
        double factor = switch (kind) {
            case 1 -> c.blockScale;
            case 2 -> c.interactableScale;
            case 3, 4 -> c.entityScale;
            default -> c.airScale;
        };
        double proximity = kind == 0 || !c.distanceResponse ? 0 : 1 - Math.clamp(distance / c.distanceRange, 0, 1);
        return Math.clamp(1 + (factor - 1) * c.focusStrength - proximity * c.distanceStrength, 0.35, 2);
    }

    public static double gap(FocusConfig c, int kind, double distance) {
        double factor = switch (kind) {
            case 1 -> c.blockGap;
            case 2 -> c.interactableGap;
            case 3, 4 -> c.entityGap;
            default -> c.airGap;
        };
        double proximity = kind == 0 || !c.distanceResponse ? 0 : 1 - Math.clamp(distance / c.distanceRange, 0, 1);
        return Math.max(0.15, c.gap * (1 + (factor - 1) * c.focusStrength) - proximity * c.distanceStrength * 2);
    }
}
