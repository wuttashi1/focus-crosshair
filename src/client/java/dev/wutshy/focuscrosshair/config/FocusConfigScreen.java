package dev.wutshy.focuscrosshair.config;

import dev.wutshy.focuscrosshair.FocusCrosshairClient;
import dev.wutshy.focuscrosshair.render.CrosshairRenderer;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class FocusConfigScreen extends Screen {
    private static final String[] SECTIONS = {"styles", "shape", "animation", "focus", "context", "colors"};
    private static final int[] TARGETS = {0, 1, 2, 4};
    private static final String[] TARGET_NAMES = {"air", "block", "interactable", "entity"};
    private final Screen parent;
    private final CrosshairRenderer preview = new CrosshairRenderer();
    private final List<Consumer<Slot>> controls = new ArrayList<>();
    private int section, page, pages, previewMode, colorIndex = -1;
    private int left, total, controlWidth, previewX, previewWidth;
    private boolean near = true, enlarged = true;
    private long lastFrame;
    private double previewClock;
    private Component status = label("studioHint");
    private record Slot(int x, int y, int width) {}

    public FocusConfigScreen(Screen parent) {
        super(Component.translatable("focuscrosshair.title"));
        this.parent = parent;
    }

    private FocusConfig config() { return FocusCrosshairClient.CONFIG.config; }
    static Component label(String key) { return Component.translatable("focuscrosshair." + key); }

    @Override
    protected void init() {
        total = Math.min(width - 24, 780);
        left = (width - total) / 2;
        previewWidth = Math.clamp(total / 3, 104, 210);
        controlWidth = total - previewWidth - 16;
        previewX = left + controlWidth + 16;
        int tabWidth = total / SECTIONS.length;
        for (int i = 0; i < SECTIONS.length; i++) {
            int selected = i;
            Button tab = button(label(SECTIONS[i]), left + i * tabWidth, 37, tabWidth - 2, () -> {
                section = selected;
                page = 0;
                colorIndex = -1;
                status = label("studioHint");
                rebuildWidgets();
            });
            tab.active = selected != section || colorIndex >= 0;
        }
        controls.clear();
        if (colorIndex >= 0) colorControls();
        else sectionControls();
        int rows = Math.max(1, (height - 156) / 24);
        int columns = controlWidth >= 360 ? 2 : 1;
        int capacity = rows * columns;
        int cellWidth = (controlWidth - 16 - (columns - 1) * 6) / columns;
        pages = Math.max(1, (controls.size() + capacity - 1) / capacity);
        page = Math.clamp(page, 0, pages - 1);
        for (int i = page * capacity; i < Math.min(controls.size(), (page + 1) * capacity); i++) {
            int cell = i - page * capacity;
            controls.get(i).accept(new Slot(left + 8 + (cell % columns) * (cellWidth + 6), 82 + (cell / columns) * 24, cellWidth));
        }
        if (pages > 1) {
            button(Component.literal("<"), left + 8, height - 64, 28, () -> changePage(-1));
            button(Component.literal(">"), left + controlWidth - 36, height - 64, 28, () -> changePage(1));
        }
        button(label(previewMode == 0 ? "preview.auto" : "target." + TARGET_NAMES[previewMode - 1]), previewX + 8, height - 114, previewWidth - 16, () -> {
            previewMode = (previewMode + 1) % 5;
            rebuildWidgets();
        });
        int half = (previewWidth - 20) / 2;
        button(label(near ? "preview.near" : "preview.far"), previewX + 8, height - 90, half, () -> { near = !near; rebuildWidgets(); });
        button(Component.literal(enlarged ? "3x" : "1x"), previewX + 12 + half, height - 90, half, () -> { enlarged = !enlarged; rebuildWidgets(); });
        button(label("preview.attack"), previewX + 8, height - 66, half, () -> preview.previewPulse(false));
        button(label("preview.hit"), previewX + 12 + half, height - 66, half, () -> preview.previewPulse(true));
        button(label("reset"), left, height - 28, total / 2 - 4, () -> {
            FocusCrosshairClient.CONFIG.config = new FocusConfig();
            FocusCrosshairClient.CROSSHAIR.reset();
            preview.reset();
            status = label("resetDone");
            rebuildWidgets();
        });
        button(Component.translatable("gui.done"), left + total / 2 + 4, height - 28, total / 2 - 4, this::onClose);
    }

    private void sectionControls() {
        FocusConfig c = config();
        switch (section) {
            case 0 -> {
                toggle("enabled", () -> c.enabled, v -> c.enabled = v);
                for (CrosshairStyle style : CrosshairStyle.values()) {
                    if (style == CrosshairStyle.CUSTOM) continue;
                    action(Component.translatable(style.key()).copy().append(c.style == style ? "  *" : ""), () -> {
                        c.style = style;
                        rebuildWidgets();
                    });
                }
                action(label("import"), () -> minecraft.gui.setScreen(new PngPickerScreen(this)));
                if (FocusCrosshairClient.CUSTOM.available()) action(Component.translatable(CrosshairStyle.CUSTOM.key()), () -> {
                    c.style = CrosshairStyle.CUSTOM;
                    rebuildWidgets();
                });
                for (ColorSkin skin : ColorSkin.values()) action(Component.translatable(skin.key()), () -> {
                    skin.apply(c);
                    status = Component.translatable(skin.key());
                });
            }
            case 1 -> {
                number("crosshairScale", .5, 2, () -> c.crosshairScale, v -> c.crosshairScale = v);
                number("gap", .5, 5, () -> c.gap, v -> c.gap = v);
                number("segmentLength", 1, 8, () -> c.segmentLength, v -> c.segmentLength = v);
                number("lineThickness", .5, 2, () -> c.lineThickness, v -> c.lineThickness = v);
                toggle("centerDot", () -> c.centerDot, v -> c.centerDot = v);
                number("dotSize", .5, 4, () -> c.dotSize, v -> c.dotSize = v);
                number("crosshairOpacity", .1, 1, () -> c.crosshairOpacity, v -> c.crosshairOpacity = v);
                number("outlineOpacity", 0, 1, () -> c.outlineOpacity, v -> c.outlineOpacity = v);
                number("baseRotation", -180, 180, () -> c.baseRotation, v -> c.baseRotation = v);
                number("customSize", 4, 48, () -> c.customSize, v -> c.customSize = v);
                toggle("customTint", () -> c.customTint, v -> c.customTint = v);
            }
            case 2 -> {
                number("bounce", 0, 1, () -> c.bounce, v -> c.bounce = v);
                number("animationSpeed", .4, 2.5, () -> c.animationSpeed, v -> c.animationSpeed = v);
                number("pulseStrength", 0, 2, () -> c.pulseStrength, v -> c.pulseStrength = v);
                toggle("motionInertia", () -> c.motionInertia, v -> c.motionInertia = v);
                number("motionInertiaStrength", 0, 2, () -> c.motionInertiaStrength, v -> c.motionInertiaStrength = v);
                toggle("movementAnimations", () -> c.movementAnimations, v -> c.movementAnimations = v);
                toggle("breathing", () -> c.breathing, v -> c.breathing = v);
                toggle("visualMagnetism", () -> c.visualMagnetism, v -> c.visualMagnetism = v);
                number("magnetismStrength", 0, 6, () -> c.magnetismStrength, v -> c.magnetismStrength = v);
            }
            case 3 -> {
                number("focusStrength", 0, 2, () -> c.focusStrength, v -> c.focusStrength = v);
                number("airScale", .5, 1.8, () -> c.airScale, v -> c.airScale = v);
                number("blockScale", .5, 1.8, () -> c.blockScale, v -> c.blockScale = v);
                number("interactableScale", .5, 1.8, () -> c.interactableScale, v -> c.interactableScale = v);
                number("entityScale", .5, 1.8, () -> c.entityScale, v -> c.entityScale = v);
                number("airGap", .2, 2, () -> c.airGap, v -> c.airGap = v);
                number("blockGap", .2, 2, () -> c.blockGap, v -> c.blockGap = v);
                number("interactableGap", .2, 2, () -> c.interactableGap, v -> c.interactableGap = v);
                number("entityGap", .2, 2, () -> c.entityGap, v -> c.entityGap = v);
                toggle("distanceResponse", () -> c.distanceResponse, v -> c.distanceResponse = v);
                number("distanceStrength", 0, .4, () -> c.distanceStrength, v -> c.distanceStrength = v);
                number("distanceRange", 1, 16, () -> c.distanceRange, v -> c.distanceRange = v);
            }
            case 4 -> {
                toggle("interactionPulse", () -> c.interactionPulse, v -> c.interactionPulse = v);
                toggle("attackAnimation", () -> c.attackAnimation, v -> c.attackAnimation = v);
                toggle("hitAnimation", () -> c.hitAnimation, v -> c.hitAnimation = v);
                toggle("damageAnimation", () -> c.damageAnimation, v -> c.damageAnimation = v);
                toggle("miningProgress", () -> c.miningProgress, v -> c.miningProgress = v);
                toggle("itemUseAnimations", () -> c.itemUseAnimations, v -> c.itemUseAnimations = v);
                toggle("lowHealthAnimation", () -> c.lowHealthAnimation, v -> c.lowHealthAnimation = v);
            }
            case 5 -> {
                String[] names = {"defaultColor", "blockColor", "interactableColor", "entityColor"};
                for (int i = 0; i < names.length; i++) {
                    int index = i;
                    action(label(names[i]).copy().append("  " + colorValue(i)), () -> {
                        colorIndex = index;
                        previewMode = index + 1;
                        page = 0;
                        rebuildWidgets();
                    });
                }
                for (ColorSkin skin : ColorSkin.values()) action(Component.translatable(skin.key()), () -> {
                    skin.apply(c);
                    rebuildWidgets();
                });
            }
        }
    }

    private String colorValue(int index) {
        FocusConfig c = config();
        return switch (index) { case 1 -> c.blockColor; case 2 -> c.interactableColor; case 3 -> c.entityColor; default -> c.defaultColor; };
    }

    private void colorControls() {
        String[] names = {"alpha", "red", "green", "blue"};
        for (int i = 0; i < 4; i++) {
            int shift = 24 - i * 8;
            number(names[i], 0, 255, () -> (Long.parseLong(colorValue(colorIndex).substring(1), 16) >>> shift) & 255, value -> {
                int color = (int)Long.parseLong(colorValue(colorIndex).substring(1), 16);
                color = (color & ~(255 << shift)) | ((int)Math.round(value) << shift);
                String hex = String.format(Locale.ROOT, "#%08X", color);
                switch (colorIndex) {
                    case 1 -> config().blockColor = hex;
                    case 2 -> config().interactableColor = hex;
                    case 3 -> config().entityColor = hex;
                    default -> config().defaultColor = hex;
                }
                config().refreshColors();
            });
        }
        action(Component.translatable("gui.back"), () -> { colorIndex = -1; page = 0; rebuildWidgets(); });
    }

    private void action(Component text, Runnable callback) {
        controls.add(slot -> button(text, slot.x, slot.y, slot.width, callback));
    }

    private void toggle(String key, BooleanSupplier getter, Consumer<Boolean> setter) {
        controls.add(slot -> addRenderableWidget(Button.builder(toggleLabel(key, getter.getAsBoolean()), button -> {
            setter.accept(!getter.getAsBoolean());
            button.setMessage(toggleLabel(key, getter.getAsBoolean()));
        }).bounds(slot.x, slot.y, slot.width, 20).build()));
    }

    private static Component toggleLabel(String key, boolean value) {
        return label(key).copy().append(": ").append(Component.translatable(value ? "options.on" : "options.off"));
    }

    private void number(String key, double min, double max, DoubleSupplier getter, DoubleConsumer setter) {
        controls.add(slot -> {
            Slider slider = new Slider(slot.x, slot.y, slot.width, label(key), min, max, getter.getAsDouble(), setter);
            switch (key) {
                case "bounce", "focusStrength", "distanceStrength", "distanceRange", "customSize", "baseRotation" ->
                    slider.setTooltip(Tooltip.create(label(key + ".tip")));
                default -> {}
            }
            addRenderableWidget(slider);
        });
    }

    private Button button(Component text, int x, int y, int width, Runnable callback) {
        return addRenderableWidget(Button.builder(text, button -> callback.run()).bounds(x, y, width, 20).build());
    }

    private void changePage(int direction) {
        page = Math.floorMod(page + direction, pages);
        rebuildWidgets();
    }

    @Override
    public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        if (pages > 1 && x >= left && x < left + controlWidth && y >= 76 && y < height - 40 && vertical != 0) {
            changePage(vertical > 0 ? -1 : 1);
            return true;
        }
        return super.mouseScrolled(x, y, horizontal, vertical);
    }

    public void importPng(Path path) {
        try {
            FocusCrosshairClient.CUSTOM.importFile(path);
            config().style = CrosshairStyle.CUSTOM;
            FocusCrosshairClient.CONFIG.save();
            status = label("importSuccess");
        } catch (IOException | RuntimeException exception) {
            status = label("importError");
        }
        minecraft.gui.setScreen(this);
        rebuildWidgets();
    }

    @Override
    public void onFilesDrop(List<Path> files) {
        if (files.size() == 1) importPng(files.getFirst());
        else {
            status = label("importError");
            minecraft.gui.setScreen(this);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, width, height, 0xF510171D);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        g.text(font, title, left, 14, 0xFFF0F6F5);
        g.text(font, "1.1", left + total - 22, 14, 0xFF91ADB6);
        g.text(font, font.plainSubstrByWidth(status.getString(), total), left, 63, 0xFFAAC5CE);
        g.fill(left, 76, left + controlWidth, height - 42, 0xFF1B2831);
        g.fill(previewX, 76, previewX + previewWidth, height - 42, 0xFF0B1117);
        g.outline(previewX, 76, previewWidth, height - 118, 0xFF334951);
        g.fill(previewX + 1, 77, previewX + previewWidth - 1, height - 120, 0xFF233841);
        long now = System.nanoTime();
        double dt = lastFrame == 0 ? 0 : Math.clamp((now - lastFrame) * 1e-9, 0, .05);
        lastFrame = now;
        previewClock += dt;
        int target = previewMode == 0 ? (int)(previewClock / 1.8) % 4 : previewMode - 1;
        int top = 78, bottom = height - 120;
        g.enableScissor(previewX + 2, top, previewX + previewWidth - 2, bottom);
        g.pose().pushMatrix();
        g.pose().translate(previewX + previewWidth / 2f, (top + bottom) / 2f);
        g.pose().scale(enlarged ? 3 : 1);
        preview.preview(g, 0, 0, dt, TARGETS[target], near ? 1 : config().distanceRange);
        g.pose().popMatrix();
        g.disableScissor();
        if (pages > 1) g.centeredText(font, (page + 1) + " / " + pages, left + controlWidth / 2, height - 58, 0xFFADC3CB);
        super.extractRenderState(g, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() { FocusCrosshairClient.CONFIG.save(); minecraft.gui.setScreen(parent); }

    @Override
    public void removed() { FocusCrosshairClient.CONFIG.save(); lastFrame = 0; }

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
            setTooltip(Tooltip.create(label));
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(label.copy().append(": " + String.format(Locale.ROOT, max >= 48 ? "%.0f" : "%.2f", min + value * (max - min))));
        }

        @Override
        protected void applyValue() { setter.accept(min + value * (max - min)); }
    }
}
