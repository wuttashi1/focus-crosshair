package dev.wutshy.focuscrosshair.config;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class PngPickerScreen extends Screen {
    private final FocusConfigScreen parent;
    private Path directory = Path.of(System.getProperty("user.home"));
    private final List<Path> roots = new ArrayList<>();
    private List<Path> entries = List.of();
    private Component status = FocusConfigScreen.label("importHint");
    private EditBox pathField;
    private int page;

    public PngPickerScreen(FocusConfigScreen parent) {
        super(FocusConfigScreen.label("import"));
        this.parent = parent;
        FileSystems.getDefault().getRootDirectories().forEach(roots::add);
        readDirectory();
    }

    private void readDirectory() {
        try (var files = Files.list(directory)) {
            entries = files.filter(path -> Files.isDirectory(path) || path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".png"))
                .limit(1000).sorted(Comparator.<Path, Boolean>comparing(path -> !Files.isDirectory(path))
                    .thenComparing(path -> path.getFileName().toString(), String.CASE_INSENSITIVE_ORDER)).toList();
            status = FocusConfigScreen.label(entries.size() >= 1000 ? "picker.limit" : "importHint");
        } catch (IOException | SecurityException exception) {
            entries = List.of();
            status = FocusConfigScreen.label("picker.error");
        }
    }

    private void open(Path path) {
        if (Files.isDirectory(path)) {
            directory = path.toAbsolutePath().normalize();
            page = 0;
            readDirectory();
            rebuildWidgets();
        } else parent.importPng(path);
    }

    @Override
    protected void init() {
        int span = Math.min(width - 24, 600), left = (width - span) / 2;
        pathField = new EditBox(font, left, 36, span - 64, 20, FocusConfigScreen.label("picker.path"));
        pathField.setMaxLength(4096);
        pathField.setValue(directory.toString());
        addRenderableWidget(pathField);
        button(FocusConfigScreen.label("picker.open"), left + span - 60, 36, 60, () -> {
            try { open(Path.of(pathField.getValue().strip().replaceAll("^\"|\"$", ""))); }
            catch (InvalidPathException | SecurityException exception) { status = FocusConfigScreen.label("picker.error"); }
        });
        button(FocusConfigScreen.label("picker.parent"), left, 60, span / 2 - 3, () -> {
            if (directory.getParent() != null) open(directory.getParent());
        });
        button(FocusConfigScreen.label("picker.drive"), left + span / 2 + 3, 60, span / 2 - 3, () -> {
            int index = roots.indexOf(directory.getRoot());
            if (!roots.isEmpty()) open(roots.get((index + 1) % roots.size()));
        });
        int capacity = Math.max(1, (height - 142) / 24);
        int pages = Math.max(1, (entries.size() + capacity - 1) / capacity);
        page = Math.clamp(page, 0, pages - 1);
        for (int i = page * capacity; i < Math.min(entries.size(), (page + 1) * capacity); i++) {
            Path entry = entries.get(i);
            String name = (Files.isDirectory(entry) ? "+ " : "PNG  ") + entry.getFileName();
            Button row = button(Component.literal(font.plainSubstrByWidth(name, span - 16)), left, 86 + (i % capacity) * 24, span, () -> open(entry));
            row.setTooltip(Tooltip.create(Component.literal(entry.toString())));
        }
        button(Component.literal("<"), left, height - 50, 32, () -> { page = Math.floorMod(page - 1, pages); rebuildWidgets(); });
        button(Component.literal((page + 1) + " / " + pages + "  >"), left + span - 70, height - 50, 70, () -> { page = (page + 1) % pages; rebuildWidgets(); });
        button(Component.translatable("gui.cancel"), left, height - 26, span, this::onClose);
    }

    private Button button(Component title, int x, int y, int width, Runnable action) {
        return addRenderableWidget(Button.builder(title, button -> action.run()).bounds(x, y, width, 20).build());
    }

    @Override
    public void onFilesDrop(List<Path> files) { parent.onFilesDrop(files); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        g.fill(0, 0, width, height, 0xF510171D);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        g.centeredText(font, title, width / 2, 10, 0xFFF0F6F5);
        g.centeredText(font, font.plainSubstrByWidth(status.getString(), width - 24), width / 2, 23, 0xFFAAC5CE);
        super.extractRenderState(g, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() { minecraft.gui.setScreen(parent); }
}
