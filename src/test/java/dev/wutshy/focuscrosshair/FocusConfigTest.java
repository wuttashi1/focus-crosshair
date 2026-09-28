package dev.wutshy.focuscrosshair;

import dev.wutshy.focuscrosshair.config.ConfigManager;
import dev.wutshy.focuscrosshair.config.FocusConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class FocusConfigTest {
    @TempDir Path directory;

    @Test void firstLaunchAndRoundTrip() throws Exception {
        Path path = directory.resolve("focuscrosshair.json");
        ConfigManager manager = new ConfigManager(path);
        manager.load();
        assertTrue(Files.exists(path));
        manager.config.enabled = false;
        manager.config.gap = 3.4;
        manager.config.interactableColor = "#8099AABB";
        manager.save();
        ConfigManager reloaded = new ConfigManager(path);
        reloaded.load();
        assertFalse(reloaded.config.enabled);
        assertEquals(3.4, reloaded.config.gap);
        assertEquals(0x8099AABB, reloaded.config.interactableArgb);
    }

    @Test void malformedConfigurationsRecoverAndPreserveOriginal() throws Exception {
        String[] broken = {"{broken", "null", "[]", "{\"gap\":{}}", ""};
        for (int i = 0; i < broken.length; i++) {
            Path path = directory.resolve("config" + i + ".json");
            Files.writeString(path, broken[i]);
            ConfigManager manager = new ConfigManager(path);
            assertDoesNotThrow(manager::load);
            assertTrue(manager.config.enabled);
            assertEquals(2, manager.config.gap);
            try (var files = Files.list(directory)) {
                Path preserved = files.filter(file -> file.getFileName().toString().startsWith(path.getFileName() + ".broken-")).findFirst().orElseThrow();
                assertEquals(broken[i], Files.readString(preserved));
            }
        }
    }

    @Test void missingFieldsAndBadColorsUseDefaults() throws Exception {
        Path path = directory.resolve("config.json");
        Files.writeString(path, "{\"enabled\":false,\"defaultColor\":null,\"entityColor\":\"bad\",\"extra\":true}");
        ConfigManager manager = new ConfigManager(path);
        manager.load();
        assertFalse(manager.config.enabled);
        assertEquals(0.85, manager.config.crosshairOpacity);
        assertEquals(0xFFFFFFFF, manager.config.defaultArgb);
        assertEquals(0xFFF5F3EF, manager.config.entityArgb);
    }

    @Test void clampsEveryNumericSettingAndNonFiniteValues() {
        FocusConfig c = new FocusConfig();
        c.magnetismStrength = 99;
        c.motionInertiaStrength = -1;
        c.crosshairOpacity = Double.NaN;
        c.crosshairScale = Double.POSITIVE_INFINITY;
        c.lineThickness = 100;
        c.gap = -10;
        c.animationSpeed = 0;
        c.validate();
        assertEquals(6, c.magnetismStrength);
        assertEquals(0, c.motionInertiaStrength);
        assertEquals(0.85, c.crosshairOpacity);
        assertEquals(1, c.crosshairScale);
        assertEquals(2, c.lineThickness);
        assertEquals(0.5, c.gap);
        assertEquals(0.4, c.animationSpeed);
    }
}
