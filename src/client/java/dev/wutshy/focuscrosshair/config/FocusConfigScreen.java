package dev.wutshy.focuscrosshair.config;

import dev.wutshy.focuscrosshair.FocusCrosshairClient;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.function.DoubleConsumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class FocusConfigScreen extends Screen {
    private static final String[] SECTIONS = {"general", "appearance", "animation", "context"};
    private static final String[][] OPTIONS = {
        {"enabled", "centerDot"},
        {"crosshairOpacity", "crosshairScale", "lineThickness", "gap", "defaultColor", "blockColor", "interactableColor", "entityColor"},
        {"animationSpeed", "visualMagnetism", "magnetismStrength", "motionInertia", "motionInertiaStrength", "movementAnimations", "breathing"},
        {"interactionPulse", "attackAnimation", "hitAnimation", "miningProgress", "itemUseAnimations", "lowHealthAnimation"}
    };
    private final Screen parent;
    private int section, page;
    private String colorOption;

    public FocusConfigScreen(Screen parent) {
        super(Component.translatable("focuscrosshair.title"));
        this.parent = parent;
    }

    private FocusConfig config() { return FocusCrosshairClient.CONFIG.config; }
    private static Component label(String key) { return Component.translatable("focuscrosshair." + key); }

    @Override
    protected void init() {
        int total = Math.min(width - 16, 420);
        int left = (width - total) / 2;
        int tabWidth = total / 4;
        for (int i = 0; i < 4; i++) {
            int selected = i;
            Button tab = Button.builder(label(SECTIONS[i]), button -> {
                section = selected;
                page = 0;
                colorOption = null;
                rebuildWidgets();
            }).bounds(left + i * tabWidth, 28, tabWidth - 2, 20).build();
            tab.active = section != i || colorOption != null;
            addRenderableWidget(tab);
        }
        if (colorOption != null) {
            addColorControls(left, total);
        } else {
            int rows = Math.max(1, (height - 140) / 24);
            int capacity = rows * 2;
            String[] options = OPTIONS[section];
            int pages = Math.max(1, (options.length + capacity - 1) / capacity);
            page = Math.min(page, pages - 1);
            int cellWidth = (total - 6) / 2;
            for (int index = page * capacity; index < Math.min(options.length, (page + 1) * capacity); index++) {
                int cell = index - page * capacity;
                addOption(options[index], left + (cell % 2) * (cellWidth + 6), 86 + (cell / 2) * 24, cellWidth);
            }
            if (pages > 1) {
                addRenderableWidget(Button.builder(Component.literal("‹"), button -> { page = (page + pages - 1) % pages; rebuildWidgets(); })
                    .bounds(left, height - 52, 30, 20).build());
                addRenderableWidget(Button.builder(Component.literal((page + 1) + " / " + pages + "  ›"), button -> { page = (page + 1) % pages; rebuildWidgets(); })
                    .bounds(left + total - 80, height - 52, 80, 20).build());
            }
        }
        addRenderableWidget(Button.builder(label("reset"), button -> {
            FocusCrosshairClient.CONFIG.config = new FocusConfig();
            FocusCrosshairClient.CROSSHAIR.reset();
            rebuildWidgets();
        }).bounds(left, height - 27, total / 2 - 3, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
            .bounds(left + total / 2 + 3, height - 27, total / 2 - 3, 20).build());
    }

    private void addOption(String name, int x, int y, int width) {
        try {
            Field field = FocusConfig.class.getField(name);
            if (field.getType() == boolean.class) {
                addRenderableWidget(Button.builder(toggleLabel(name, field.getBoolean(config())), button -> {
                    try {
                        boolean value = !field.getBoolean(config());
                        field.setBoolean(config(), value);
                        button.setMessage(toggleLabel(name, value));
                    } catch (IllegalAccessException exception) { throw new IllegalStateException(exception); }
                }).bounds(x, y, width, 20).build());
            } else if (field.getType() == String.class) {
                addRenderableWidget(Button.builder(label(name), button -> { colorOption = name; rebuildWidgets(); })
                    .bounds(x, y, width, 20).build());
            } else {
                double min = switch (name) {
                    case "crosshairOpacity" -> 0.1;
                    case "crosshairScale", "lineThickness", "gap" -> 0.5;
                    case "animationSpeed" -> 0.4;
                    default -> 0;
                };
                double max = switch (name) {
                    case "crosshairOpacity" -> 1;
                    case "magnetismStrength" -> 6;
                    case "gap" -> 5;
                    case "animationSpeed" -> 2.5;
                    default -> 2;
                };
                addRenderableWidget(new Slider(x, y, width, label(name), min, max, field.getDouble(config()), value -> {
                    try { field.setDouble(config(), value); }
                    catch (IllegalAccessException exception) { throw new IllegalStateException(exception); }
                }));
            }
        } catch (ReflectiveOperationException exception) { throw new IllegalStateException(exception); }
    }

    private void addColorControls(int x, int width) {
        try {
            Field field = FocusConfig.class.getField(colorOption);
            int color = (int)Long.parseLong(((String)field.get(config())).substring(1), 16);
            String[] channels = {"alpha", "red", "green", "blue"};
            for (int i = 0; i < 4; i++) {
                int shift = 24 - i * 8;
                addRenderableWidget(new Slider(x, 86 + i * 23, width, label(channels[i]), 0, 255, (color >>> shift) & 255, value -> {
                    try {
                        int current = (int)Long.parseLong(((String)field.get(config())).substring(1), 16);
                        current = (current & ~(255 << shift)) | ((int)Math.round(value) << shift);
                        field.set(config(), String.format(Locale.ROOT, "#%08X", current));
                        config().refreshColors();
                    } catch (IllegalAccessException exception) { throw new IllegalStateException(exception); }
                }));
            }
            addRenderableWidget(Button.builder(Component.translatable("gui.back"), button -> { colorOption = null; rebuildWidgets(); })
                .bounds(x, height - 52, width, 20).build());
        } catch (ReflectiveOperationException exception) { throw new IllegalStateException(exception); }
    }

    private static Component toggleLabel(String name, boolean value) {
        return label(name).copy().append(": ").append(Component.translatable(value ? "options.on" : "options.off"));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.centeredText(font, title, width / 2, 10, 0xFFFFFFFF);
        FocusCrosshairClient.CROSSHAIR.preview(graphics, width / 2f, 64);
        if (colorOption != null) graphics.centeredText(font, label(colorOption), width / 2, 74, 0xFFB9C8CE);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        FocusCrosshairClient.CONFIG.save();
        minecraft.gui.setScreen(parent);
    }

    @Override
    public void removed() { FocusCrosshairClient.CONFIG.save(); }

    private static final class Slider extends AbstractSliderButton {
        private final Component label;
        private final double min, max;
        private final DoubleConsumer setter;

        Slider(int x, int y, int width, Component label, double min, double max, double initial, DoubleConsumer setter) {
            super(x, y, width, 20, label, (initial - min) / (max - min));
            this.label = label;
            this.min = min;
            this.max = max;
            this.setter = setter;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(label.copy().append(": " + String.format(Locale.ROOT, max == 255 ? "%.0f" : "%.2f", min + value * (max - min))));
        }

        @Override
        protected void applyValue() { setter.accept(min + value * (max - min)); }
    }
}
