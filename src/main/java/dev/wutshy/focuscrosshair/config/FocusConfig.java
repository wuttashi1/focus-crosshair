package dev.wutshy.focuscrosshair.config;

public final class FocusConfig {
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
