package dev.wutshy.focuscrosshair.config;

public final class FocusConfig {
    public CrosshairStyle style = CrosshairStyle.FOCUS;
    public double bounce = 0.65;
    public double segmentLength = 2.5;
    public double dotSize = 1;
    public double outlineOpacity = 0.4;
    public double baseRotation = 0;
    public double focusStrength = 1;
    public double airScale = 1.12;
    public double blockScale = 0.94;
    public double interactableScale = 0.84;
    public double entityScale = 0.78;
    public double airGap = 1.25;
    public double blockGap = 0.85;
    public double interactableGap = 0.65;
    public double entityGap = 0.5;
    public boolean distanceResponse = true;
    public double distanceStrength = 0.12;
    public double distanceRange = 6;
    public double pulseStrength = 1;
    public boolean damageAnimation = true;
    public double customSize = 12;
    public boolean customTint = false;
    public boolean enabled = true;
    public boolean centerDot = true;
    public boolean visualMagnetism = true;
    public double magnetismStrength = 2;
    public boolean motionInertia = true;
    public double motionInertiaStrength = 1.2;
    public boolean movementAnimations = true;
    public boolean interactionPulse = true;
    public boolean attackAnimation = true;
    public boolean hitAnimation = true;
    public boolean miningProgress = true;
    public boolean itemUseAnimations = true;
    public boolean lowHealthAnimation = true;
    public boolean breathing = false;
    public double crosshairOpacity = 0.85;
    public double crosshairScale = 1;
    public double lineThickness = 0.8;
    public double gap = 2;
    public double animationSpeed = 1;
    public String defaultColor = "#FFFFFFFF";
    public String blockColor = "#FFF1F4F6";
    public String interactableColor = "#FFE0F5FA";
    public String entityColor = "#FFF5F3EF";
    public transient int defaultArgb, blockArgb, interactableArgb, entityArgb;

    public FocusConfig() { refreshColors(); }

    public void validate() {
        if (style == null) style = CrosshairStyle.FOCUS;
        bounce = clamp(bounce, 0, 1, 0.65);
        segmentLength = clamp(segmentLength, 1, 8, 2.5);
        dotSize = clamp(dotSize, 0.5, 4, 1);
        outlineOpacity = clamp(outlineOpacity, 0, 1, 0.4);
        baseRotation = clamp(baseRotation, -180, 180, 0);
        focusStrength = clamp(focusStrength, 0, 2, 1);
        airScale = clamp(airScale, 0.5, 1.8, 1.12);
        blockScale = clamp(blockScale, 0.5, 1.8, 0.94);
        interactableScale = clamp(interactableScale, 0.5, 1.8, 0.84);
        entityScale = clamp(entityScale, 0.5, 1.8, 0.78);
        airGap = clamp(airGap, 0.2, 2, 1.25);
        blockGap = clamp(blockGap, 0.2, 2, 0.85);
        interactableGap = clamp(interactableGap, 0.2, 2, 0.65);
        entityGap = clamp(entityGap, 0.2, 2, 0.5);
        distanceStrength = clamp(distanceStrength, 0, 0.4, 0.12);
        distanceRange = clamp(distanceRange, 1, 16, 6);
        pulseStrength = clamp(pulseStrength, 0, 2, 1);
        customSize = clamp(customSize, 4, 48, 12);
        magnetismStrength = clamp(magnetismStrength, 0, 6, 2);
        motionInertiaStrength = clamp(motionInertiaStrength, 0, 2, 1.2);
        crosshairOpacity = clamp(crosshairOpacity, 0.1, 1, 0.85);
        crosshairScale = clamp(crosshairScale, 0.5, 2, 1);
        lineThickness = clamp(lineThickness, 0.5, 2, 0.8);
        gap = clamp(gap, 0.5, 5, 2);
        animationSpeed = clamp(animationSpeed, 0.4, 2.5, 1);
        defaultColor = validColor(defaultColor, "#FFFFFFFF");
        blockColor = validColor(blockColor, "#FFF1F4F6");
        interactableColor = validColor(interactableColor, "#FFE0F5FA");
        entityColor = validColor(entityColor, "#FFF5F3EF");
        refreshColors();
    }

    public void refreshColors() {
        defaultArgb = parse(defaultColor);
        blockArgb = parse(blockColor);
        interactableArgb = parse(interactableColor);
        entityArgb = parse(entityColor);
    }

    private static String validColor(String value, String fallback) {
        return value != null && value.matches("#[0-9a-fA-F]{8}") ? value : fallback;
    }

    private static int parse(String value) { return (int) Long.parseLong(value.substring(1), 16); }

    private static double clamp(double value, double min, double max, double fallback) {
        return Double.isFinite(value) ? Math.clamp(value, min, max) : fallback;
    }
}
