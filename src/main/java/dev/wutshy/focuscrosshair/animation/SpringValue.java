package dev.wutshy.focuscrosshair.animation;

public final class SpringValue {
    public double value;
    public double velocity;
    public double target;

    public SpringValue(double initial) { reset(initial); }

    public void reset(double initial) {
        value = target = initial;
        velocity = 0;
    }

    public void impulse(double amount) { velocity += amount; }

    public void update(double delta, double speed) {
        update(delta, 420 * speed * speed, 34 * speed);
    }

    public void animate(double delta, double speed, double bounce) {
        update(delta, 420 * speed * speed, (41 - 29 * Math.clamp(bounce, 0, 1)) * speed);
    }

    public void update(double delta, double stiffness, double damping) {
        if (!Double.isFinite(delta) || delta <= 0) return;
        double remaining = Math.min(delta, 0.05);
        while (remaining > 0) {
            double step = Math.min(remaining, 1.0 / 480.0);
            velocity += ((target - value) * stiffness - velocity * damping) * step;
            value += velocity * step;
            remaining -= step;
        }
    }
}
