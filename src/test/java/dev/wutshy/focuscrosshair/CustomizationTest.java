package dev.wutshy.focuscrosshair;

import com.google.gson.Gson;
import dev.wutshy.focuscrosshair.animation.FocusTargets;
import dev.wutshy.focuscrosshair.animation.SpringValue;
import dev.wutshy.focuscrosshair.config.ColorSkin;
import dev.wutshy.focuscrosshair.config.CrosshairStyle;
import dev.wutshy.focuscrosshair.config.FocusConfig;
import dev.wutshy.focuscrosshair.config.PngImport;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class CustomizationTest {
    @TempDir Path directory;

    @Test void legacyConfigGetsBouncyDefaultsWithoutLosingPreferences() {
        FocusConfig c = new Gson().fromJson("{\"gap\":3,\"centerDot\":false,\"animationSpeed\":0.7}", FocusConfig.class);
        c.validate();
        assertEquals(.65, c.bounce);
        assertEquals(CrosshairStyle.FOCUS, c.style);
        assertEquals(3, c.gap);
        assertFalse(c.centerDot);
        assertEquals(.7, c.animationSpeed);
        assertTrue(c.distanceResponse);
    }

    @Test void invalidStyleAndSettingsHaveSafeFallbacks() {
        FocusConfig c = new Gson().fromJson("{\"style\":\"UNKNOWN\",\"bounce\":99,\"entityScale\":-4}", FocusConfig.class);
        c.distanceRange = Double.NaN;
        c.customSize = Double.POSITIVE_INFINITY;
        c.validate();
        assertEquals(CrosshairStyle.FOCUS, c.style);
        assertEquals(1, c.bounce);
        assertEquals(.5, c.entityScale);
        assertEquals(6, c.distanceRange);
        assertEquals(12, c.customSize);
    }

    @Test void focusStatesAreDistinctAndNearTargetsContractMore() {
        FocusConfig c = new FocusConfig();
        assertTrue(FocusTargets.scale(c, 0, 6) > FocusTargets.scale(c, 1, 6));
        assertTrue(FocusTargets.scale(c, 1, 6) > FocusTargets.scale(c, 2, 6));
        assertTrue(FocusTargets.scale(c, 2, 6) > FocusTargets.scale(c, 4, 6));
        for (int kind = 1; kind <= 4; kind++) {
            assertTrue(FocusTargets.scale(c, kind, 1) < FocusTargets.scale(c, kind, 6));
            assertTrue(FocusTargets.gap(c, kind, 1) < FocusTargets.gap(c, kind, 6));
        }
        c.distanceResponse = false;
        assertEquals(FocusTargets.scale(c, 4, 1), FocusTargets.scale(c, 4, 6));
        c.focusStrength = 0;
        assertEquals(1, FocusTargets.scale(c, 4, 1));
        assertEquals(c.gap, FocusTargets.gap(c, 4, 1));
    }

    @Test void defaultSpringOvershootsAndRecoversAtDifferentFrameRates() {
        for (int fps : new int[]{20, 30, 60, 144, 1000}) {
            SpringValue spring = new SpringValue(0);
            spring.target = 1;
            double peak = 0;
            for (int i = 0; i < fps * 3; i++) {
                spring.animate(1.0 / fps, 1, .65);
                peak = Math.max(peak, spring.value);
            }
            assertTrue(peak > 1.08 && peak < 1.2, "Expected visible, restrained bounce at " + fps);
            assertEquals(1, spring.value, .001);
        }
    }

    @Test void strongestBounceIsStableAtSlowAndFastSpeeds() {
        for (double speed : new double[]{.4, 1, 2.5}) {
            SpringValue spring = new SpringValue(1);
            spring.target = .6;
            for (int i = 0; i < 1000; i++) {
                spring.animate(i % 2 == 0 ? .05 : .001, speed, 1);
                assertTrue(Double.isFinite(spring.value));
                assertTrue(spring.value > 0 && spring.value < 2);
            }
            assertEquals(.6, spring.value, .001);
        }
    }

    @Test void fiveColorSkinsPersistTheirColors() {
        assertEquals(5, ColorSkin.values().length);
        assertEquals(6, CrosshairStyle.values().length);
        for (ColorSkin skin : ColorSkin.values()) {
            FocusConfig c = new FocusConfig();
            skin.apply(c);
            FocusConfig copy = new Gson().fromJson(new Gson().toJson(c), FocusConfig.class);
            copy.validate();
            assertEquals(c.interactableArgb, copy.interactableArgb);
            assertEquals(c.defaultArgb, copy.defaultArgb);
        }
    }

    @Test void pngHeaderAcceptsSupportedBoundsAndRejectsOtherFiles() throws Exception {
        assertDoesNotThrow(() -> PngImport.validate(header(1, 1)));
        assertDoesNotThrow(() -> PngImport.validate(header(512, 512)));
        for (int[] size : new int[][]{{0, 32}, {32, -1}, {513, 1}, {1, 513}, {Integer.MAX_VALUE, 2}})
            assertThrows(IOException.class, () -> PngImport.validate(header(size[0], size[1])));
        assertThrows(IOException.class, () -> PngImport.validate(new byte[32]));
        assertThrows(IOException.class, () -> PngImport.validate(new byte[100]));
        assertThrows(IOException.class, () -> PngImport.validate(new byte[PngImport.MAX_BYTES + 1]));
        Path image = directory.resolve("crosshair.png");
        Files.write(image, header(64, 32));
        assertArrayEquals(header(64, 32), PngImport.read(image));
        assertThrows(IOException.class, () -> PngImport.read(directory));
    }

    private static byte[] header(int width, int height) {
        return ByteBuffer.allocate(33).putLong(0x89504E470D0A1A0AL).putInt(13).putInt(0x49484452).putInt(width).putInt(height).array();
    }
}
