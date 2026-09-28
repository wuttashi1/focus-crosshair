package dev.wutshy.focuscrosshair.mixin;

import dev.wutshy.focuscrosshair.FocusCrosshairClient;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class DamageMixin {
    @Inject(method = "handleDamageEvent", at = @At("TAIL"), require = 0)
    private void focuscrosshair$damage(ClientboundDamageEventPacket packet, CallbackInfo callback) {
        FocusCrosshairClient.CROSSHAIR.damage(packet);
    }
}
