package dev.wutshy.focuscrosshair.config;

public enum ColorSkin {
    FROST("#FFF4F8FA", "#FFE8F1F5", "#FFB9EBED", "#FFFFFFFF"),
    IVORY("#FFF8F1DF", "#FFF2E8CE", "#FFFFDCA8", "#FFFFF8EE"),
    MINT("#FFD6F5E5", "#FFBDEBD7", "#FF89EAC2", "#FFE8FFF3"),
    AMBER("#FFFFDC9E", "#FFF2C87E", "#FFFFB95F", "#FFFFECCE"),
    LILAC("#FFE8DFFF", "#FFD4C9EF", "#FFC0ABFF", "#FFF6F1FF");

    private final String idle, block, interaction, entity;

    ColorSkin(String idle, String block, String interaction, String entity) {
        this.idle = idle;
        this.block = block;
        this.interaction = interaction;
        this.entity = entity;
    }

    public void apply(FocusConfig config) {
        config.defaultColor = idle;
        config.blockColor = block;
        config.interactableColor = interaction;
        config.entityColor = entity;
        config.refreshColors();
    }

    public String key() { return "focuscrosshair.skin." + name().toLowerCase(java.util.Locale.ROOT); }
}
