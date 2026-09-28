package dev.wutshy.focuscrosshair;

import dev.wutshy.focuscrosshair.animation.SpringValue;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SpringValueTest {
    @Test void convergesAcrossFrameRatesAndAnimationSpeeds() {
        for (int fps : new int[]{20, 30, 60, 144, 360, 2000}) {
            for (double speed : new double[]{0.4, 1, 2.5}) {
                SpringValue spring = new SpringValue(0);
                spring.target = 5;
                spring.impulse(100);
                for (int i = 0; i < fps * 5; i++) {
                    spring.update(1.0 / fps, speed);
                    assertTrue(Double.isFinite(spring.value));
                    assertTrue(Math.abs(spring.value) < 20);
                }
                assertEquals(5, spring.value, 0.001);
                assertEquals(0, spring.velocity, 0.001);
            }
        }
    }

    @Test void frameRateDoesNotChangeResponseMaterially() {
        double reference = trajectory(240);
        for (int fps : new int[]{20, 30, 60, 120, 1000}) assertEquals(reference, trajectory(fps), 0.008);
    }

    private double trajectory(int fps) {
        SpringValue spring = new SpringValue(0);
        spring.target = 1;
        for (int i = 0; i < fps / 5; i++) spring.update(1.0 / fps, 1);
        return spring.value;
    }

    @Test void freezesAndInvalidDeltasCannotExplode() {
        SpringValue spring = new SpringValue(0);
        spring.target = 1;
        spring.update(400, 2.5);
        assertTrue(spring.value >= 0 && spring.value < 2);
        double value = spring.value;
        spring.update(Double.NaN, 1);
        spring.update(Double.POSITIVE_INFINITY, 1);
        spring.update(-1, 1);
        spring.update(0, 1);
        assertEquals(value, spring.value);
        spring.reset(7);
        assertEquals(7, spring.target);
        assertEquals(0, spring.velocity);
    }
}
