package dev.wutshy.focuscrosshair.config;

public enum CrosshairStyle {
    FOCUS, BRACKETS, DIAMOND, RING, CHEVRON, CUSTOM;

    public String key() { return "focuscrosshair.style." + name().toLowerCase(java.util.Locale.ROOT); }
}
