package dev.wutshy.focuscrosshair.render;

import dev.wutshy.focuscrosshair.FocusCrosshairClient;
import dev.wutshy.focuscrosshair.animation.SpringValue;
import dev.wutshy.focuscrosshair.animation.FocusTargets;
import dev.wutshy.focuscrosshair.config.CrosshairStyle;
import dev.wutshy.focuscrosshair.config.FocusConfig;
import dev.wutshy.focuscrosshair.mixin.MiningAccess;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public final class CrosshairRenderer {
    private static final Identifier ATTACK_FULL = Identifier.withDefaultNamespace("hud/crosshair_attack_indicator_full");
    private static final Identifier ATTACK_BACKGROUND = Identifier.withDefaultNamespace("hud/crosshair_attack_indicator_background");
    private static final Identifier ATTACK_PROGRESS = Identifier.withDefaultNamespace("hud/crosshair_attack_indicator_progress");
    private final SpringValue scale = new SpringValue(1), gap = new SpringValue(2), opacity = new SpringValue(0.85);
    private final SpringValue length = new SpringValue(2.5), thickness = new SpringValue(0.8), dot = new SpringValue(1);
    private final SpringValue focus = new SpringValue(0), rotation = new SpringValue(0);
    private final SpringValue offsetX = new SpringValue(0), offsetY = new SpringValue(0);
    private final SpringValue interaction = new SpringValue(0), hit = new SpringValue(0), attack = new SpringValue(0), damage = new SpringValue(0);
    private final SpringValue mining = new SpringValue(0), ring = new SpringValue(0);
    private final SpringValue[] springs = {scale, gap, opacity, length, thickness, dot, focus, rotation, offsetX, offsetY, interaction, hit, attack, damage, mining, ring};
    private ClientLevel lastLevel;
    private LocalPlayer lastPlayer;
    private BlockPos miningPos;
    private boolean grounded, cameraReady;
    private float lastYaw, lastPitch;
    private long lastFrame;
    private double clock;
    private int targetKind;
    private BlockPos targetBlock;
    private BlockState targetState;

    private FocusConfig config() { return FocusCrosshairClient.CONFIG.config; }

    public void reset() {
        for (SpringValue spring : springs) spring.reset(0);
        scale.reset(1);
        gap.reset(config().gap);
        opacity.reset(config().crosshairOpacity);
        length.reset(2.5);
        thickness.reset(config().lineThickness);
        dot.reset(1);
        lastFrame = 0;
        cameraReady = false;
        miningPos = targetBlock = null;
        targetState = null;
        targetKind = 0;
        clock = 0;
    }

    public void tick(Minecraft client) {
        if (client.level != lastLevel || client.player != lastPlayer) {
            lastLevel = client.level;
            lastPlayer = client.player;
            reset();
            grounded = client.player != null && client.player.onGround();
        }
        if (client.player == null || client.level == null || client.isPaused()) return;
        boolean nowGrounded = client.player.onGround();
        if (config().enabled && config().movementAnimations && client.gui.screen() == null && nowGrounded != grounded) {
            gap.impulse((nowGrounded ? -9 : 7) * config().pulseStrength);
        }
        grounded = nowGrounded;
    }

    private boolean active(Minecraft client) {
        return config().enabled && client.player != null && client.level != null && !client.isPaused()
            && client.gui.screen() == null && !client.player.isSpectator();
    }

    public void attack() {
        if (active(Minecraft.getInstance()) && config().attackAnimation) attack.impulse(40 * config().pulseStrength);
    }

    public void interaction() {
        if (active(Minecraft.getInstance()) && config().interactionPulse) interaction.impulse(38 * config().pulseStrength);
    }

    public void damage(ClientboundDamageEventPacket packet) {
        Minecraft client = Minecraft.getInstance();
        if (!active(client)) return;
        if (packet.entityId() == client.player.getId() && config().damageAnimation) damage.impulse(24 * config().pulseStrength);
        else if (config().hitAnimation && packet.sourceCauseId() == client.player.getId()) hit.impulse(42 * config().pulseStrength);
    }

    private boolean visible(Minecraft client) {
        if (client.player == null || client.level == null || client.gameMode == null || client.gui.screen() != null
            || !client.options.getCameraType().isFirstPerson() || client.player.isSleeping()
            || client.debugEntries.isCurrentlyEnabled(DebugScreenEntries.THREE_DIMENSIONAL_CROSSHAIR)) return false;
        if (!client.player.isSpectator()) return true;
        if (client.hitResult instanceof EntityHitResult entity) return entity.getEntity() instanceof MenuProvider;
        if (client.hitResult instanceof BlockHitResult block && block.getType() == HitResult.Type.BLOCK)
            return client.level.getBlockState(block.getBlockPos()).getMenuProvider(client.level, block.getBlockPos()) != null;
        return false;
    }

    private void target(Minecraft client) {
        HitResult result = client.hitResult;
        if (result instanceof EntityHitResult entity && entity.getEntity().isAlive()) {
            targetKind = entity.getEntity() instanceof LivingEntity ? 4 : 3;
            targetBlock = null;
        } else if (result instanceof BlockHitResult block && block.getType() == HitResult.Type.BLOCK) {
            BlockState state = client.level.getBlockState(block.getBlockPos());
            if (!block.getBlockPos().equals(targetBlock) || state != targetState) {
                targetBlock = block.getBlockPos();
                targetState = state;
                Block type = state.getBlock();
                boolean interactable = state.getMenuProvider(client.level, targetBlock) != null
                    || type instanceof DoorBlock || type instanceof TrapDoorBlock || type instanceof ButtonBlock
                    || type instanceof LeverBlock || type instanceof BedBlock || type instanceof FenceGateBlock
                    || type instanceof BellBlock || type instanceof NoteBlock || type instanceof JukeboxBlock
                    || type instanceof CakeBlock || type instanceof ComposterBlock;
                targetKind = state.isAir() ? 0 : interactable ? 2 : 1;
            }
        } else {
            targetKind = 0;
            targetBlock = null;
        }
    }

    public void extract(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        Minecraft client = Minecraft.getInstance();
        if (!visible(client)) {
            reset();
            return;
        }
        long now = System.nanoTime();
        double dt = lastFrame == 0 || client.isPaused() ? 0 : Math.clamp((now - lastFrame) * 1e-9, 0, 0.05);
        lastFrame = now;
        clock += dt;
        target(client);
        FocusConfig c = config();
        LocalPlayer player = client.player;
        double distance = client.hitResult == null ? c.distanceRange
            : client.hitResult.getLocation().distanceTo(client.gameRenderer.mainCamera().position());
        applyFocus(c, targetKind, distance);
        if (c.movementAnimations) {
            if (player.isSprinting()) gap.target += 0.55;
            if (player.isCrouching()) gap.target -= 0.3;
            if (player.isSwimming()) gap.target += 0.5;
            if (player.isFallFlying()) { gap.target += 0.8; length.target -= 0.3; }
        }
        if (c.breathing) scale.target += Math.sin(clock * 1.3) * 0.012;
        if (c.lowHealthAnimation && player.getHealth() <= 12 && !player.isSpectator()) {
            scale.target += Math.sin(clock * (player.getHealth() <= 6 ? 3.2 : 2.1)) * (player.getHealth() <= 6 ? 0.025 : 0.01);
        }
        if (c.itemUseAnimations && player.isUsingItem()) {
            double ticks = player.getTicksUsingItem() + delta.getGameTimeDeltaPartialTick(false);
            switch (player.getUseItem().getUseAnimation()) {
                case EAT, DRINK -> gap.target -= 0.22 + 0.18 * Math.sin(ticks * 0.8);
                case BOW -> gap.target -= Math.min(1, ticks / 20) * 0.9;
                case CROSSBOW -> gap.target -= Math.min(1, ticks / Math.max(1, CrossbowItem.getChargeDuration(player.getUseItem(), player))) * 0.9;
                case BLOCK -> { gap.target -= 0.45; scale.target -= 0.04; }
                case SPYGLASS -> { gap.target -= 0.3; opacity.target *= 0.7; }
                case TRIDENT, SPEAR -> gap.target -= Math.min(1, ticks / 10) * 0.55;
                default -> gap.target -= 0.18;
            }
        }
        float yaw = client.gameRenderer.mainCamera().yRot();
        float pitch = client.gameRenderer.mainCamera().xRot();
        double dx = cameraReady && dt > 0 ? Mth.wrapDegrees(yaw - lastYaw) / dt : 0;
        double dy = cameraReady && dt > 0 ? (pitch - lastPitch) / dt : 0;
        lastYaw = yaw;
        lastPitch = pitch;
        cameraReady = true;
        offsetX.target = c.motionInertia ? Math.clamp(-dx * 0.003, -c.motionInertiaStrength, c.motionInertiaStrength) : 0;
        offsetY.target = c.motionInertia ? Math.clamp(-dy * 0.003, -c.motionInertiaStrength, c.motionInertiaStrength) : 0;
        boolean destroying = c.miningProgress && client.gameMode.isDestroying() && client.gameMode instanceof MiningAccess;
        if (destroying) {
            MiningAccess access = (MiningAccess) client.gameMode;
            BlockPos pos = access.focuscrosshair$position();
            if (!pos.equals(miningPos)) { mining.reset(0); ring.reset(0); miningPos = pos; }
            mining.target = Math.clamp(access.focuscrosshair$progress(), 0, 1);
            ring.target = 0.65;
        } else {
            ring.target = 0;
            if (ring.value < 0.01) { mining.reset(0); miningPos = null; }
        }
        if (!c.attackAnimation) attack.reset(0);
        if (!c.hitAnimation) hit.reset(0);
        if (!c.interactionPulse) interaction.reset(0);
        if (!c.damageAnimation) damage.reset(0);
        for (SpringValue spring : springs) spring.animate(dt, c.animationSpeed, c.bounce);
        graphics.nextStratum();
        draw(graphics, graphics.guiWidth() / 2f, graphics.guiHeight() / 2f, c);
        attackIndicator(graphics, client);
    }

    private void applyFocus(FocusConfig c, int kind, double distance) {
        focus.target = switch (kind) { case 1 -> 0.5; case 2 -> 0.7; case 3 -> 0.8; case 4 -> 1; default -> 0; };
        scale.target = FocusTargets.scale(c, kind, distance);
        gap.target = FocusTargets.gap(c, kind, distance);
        length.target = c.segmentLength * (kind == 2 ? 0.85 : 1);
        thickness.target = c.lineThickness;
        opacity.target = Math.min(1, c.crosshairOpacity + focus.target * 0.12);
        dot.target = c.centerDot ? c.dotSize * (1 + focus.target * 0.25) : 0;
        rotation.target = Math.toRadians(c.baseRotation);
    }

    public void preview(GuiGraphicsExtractor graphics, float x, float y, double dt, int kind, double distance) {
        targetKind = kind;
        FocusConfig c = config();
        applyFocus(c, kind, distance);
        if (!c.attackAnimation) attack.reset(0);
        if (!c.hitAnimation) hit.reset(0);
        for (SpringValue spring : springs) spring.animate(dt, c.animationSpeed, c.bounce);
        draw(graphics, x, y, c);
    }

    public void previewPulse(boolean confirmedHit) {
        if (confirmedHit) hit.impulse(42 * config().pulseStrength);
        else attack.impulse(40 * config().pulseStrength);
    }

    private void draw(GuiGraphicsExtractor g, float x, float y, FocusConfig c) {
        int rgb = switch (targetKind) { case 1 -> c.blockArgb; case 2 -> c.interactableArgb; case 3, 4 -> c.entityArgb; default -> c.defaultArgb; };
        int color = alpha(rgb, opacity.value);
        double s = c.crosshairScale * Math.clamp(scale.value + attack.value * 0.05 - hit.value * 0.06 + damage.value * 0.04, 0.3, 2.3);
        double distance = Math.max(0.15, gap.value + attack.value * 0.65 - hit.value * 0.45 + damage.value * 0.45);
        double compression = c.visualMagnetism ? Math.clamp(focus.value, 0, 1) * c.magnetismStrength * 0.1 : 0;
        double ox = Math.clamp(offsetX.value, -2, 2), oy = Math.clamp(offsetY.value, -2, 2);
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale((float)s);
        g.pose().rotate((float)rotation.value);
        CrosshairStyle style = c.style;
        boolean custom = style == CrosshairStyle.CUSTOM && FocusCrosshairClient.CUSTOM.available();
        double t = Math.max(0.4, thickness.value), l = Math.max(1, length.value);
        int outline = alpha(0xFF101820, opacity.value * c.outlineOpacity);
        if (custom) {
            FocusCrosshairClient.CUSTOM.draw(g, c.customSize, c.customTint ? color : alpha(0xFFFFFFFF, opacity.value));
        } else if (style == CrosshairStyle.RING) {
            double radius = distance + l * 0.6;
            for (int i = 0; i < 32; i++) {
                if (i % 8 != 0) segment(g, radius, -0.35, t, 0.7, color, outline);
                g.pose().rotate((float)(Math.PI / 16));
            }
        } else if (style == CrosshairStyle.DIAMOND) {
            double r = (distance + l * 0.5) * 0.7;
            g.pose().rotate((float)(Math.PI / 4));
            for (int i = 0; i < 4; i++) {
                segment(g, -r, -r, r * 2, t, color, outline);
                g.pose().rotate((float)(Math.PI / 2));
            }
            g.pose().rotate((float)(-Math.PI / 4));
        } else if (style == CrosshairStyle.CHEVRON) {
            g.pose().pushMatrix();
            g.pose().translate(0, (float)-distance);
            g.pose().rotate((float)(Math.PI / 4));
            segment(g, 0, -t / 2, l + 1, t, color, outline);
            g.pose().rotate((float)(Math.PI / 2));
            segment(g, 0, -t / 2, l + 1, t, color, outline);
            g.pose().popMatrix();
        } else {
            for (int i = 0; i < 4; i++) {
                double drift = switch(i) { case 0 -> ox; case 1 -> oy; case 2 -> -ox; default -> -oy; };
                double d = Math.max(0.15, distance + drift * 0.45 - (i % 2 == 0 ? compression : 0));
                if (style == CrosshairStyle.BRACKETS) {
                    segment(g, d, d + l - t, l, t, color, outline);
                    segment(g, d + l - t, d, t, l, color, outline);
                } else segment(g, d, -t / 2, l, t, color, outline);
                g.pose().rotate((float)(Math.PI / 2));
            }
        }
        if (!custom && dot.value > 0.01) {
            double size = Math.max(0, dot.value + hit.value * 0.25);
            segment(g, -size / 2, -size / 2, size, size, color, outline);
        }
        double radius = custom ? c.customSize / 2 + 2 : distance + l + 2;
        if (ring.value > 0.01) radial(g, radius, mining.value, alpha(rgb, ring.value), 0.55);
        if (interaction.value > 0.025) radial(g, radius + interaction.value * 1.3, 1, alpha(rgb, interaction.value * 0.22), 0.45);
        g.pose().popMatrix();
    }

    private static void segment(GuiGraphicsExtractor g, double x, double y, double width, double height, int color, int outline) {
        if ((outline >>> 24) != 0) rect(g, x - 0.35, y - 0.35, width + 0.7, height + 0.7, outline);
        rect(g, x, y, width, height, color);
    }

    private static void radial(GuiGraphicsExtractor g, double radius, double progress, int color, double width) {
        int count = (int)Math.ceil(Math.clamp(progress, 0, 1) * 16);
        for (int i = 0; i < count; i++) {
            double angle = i * Math.PI / 8 - Math.PI / 2;
            rect(g, Math.cos(angle) * radius - width / 2, Math.sin(angle) * radius - width / 2, width, width, color);
        }
    }

    private static void rect(GuiGraphicsExtractor g, double x, double y, double width, double height, int color) {
        g.pose().pushMatrix();
        g.pose().translate((float)x, (float)y);
        g.pose().scale((float)width, (float)height);
        g.fill(0, 0, 1, 1, color);
        g.pose().popMatrix();
    }

    private static int alpha(int color, double opacity) {
        return (Math.clamp((int)((color >>> 24) * opacity), 0, 255) << 24) | (color & 0xFFFFFF);
    }

    private static void attackIndicator(GuiGraphicsExtractor graphics, Minecraft client) {
        if (client.options.attackIndicator().get() != AttackIndicatorStatus.CROSSHAIR) return;
        float strength = client.player.getAttackStrengthScale(0);
        boolean full = client.crosshairPickEntity instanceof LivingEntity && client.crosshairPickEntity.isAlive()
            && strength >= 1 && client.player.getCurrentItemAttackStrengthDelay() > 5;
        AttackRange range = client.player.getActiveItem().get(DataComponents.ATTACK_RANGE);
        full &= range == null || client.hitResult != null && range.isInRange(client.player, client.hitResult.getLocation());
        int x = graphics.guiWidth() / 2 - 8, y = graphics.guiHeight() / 2 + 9;
        if (full) graphics.blitSprite(RenderPipelines.CROSSHAIR, ATTACK_FULL, x, y, 16, 16);
        else if (strength < 1) {
            graphics.blitSprite(RenderPipelines.CROSSHAIR, ATTACK_BACKGROUND, x, y, 16, 4);
            graphics.blitSprite(RenderPipelines.CROSSHAIR, ATTACK_PROGRESS, 16, 4, 0, 0, x, y, (int)(strength * 17), 4);
        }
    }
}
