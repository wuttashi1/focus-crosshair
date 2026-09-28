package dev.wutshy.focuscrosshair.mixin;

import dev.wutshy.focuscrosshair.FocusCrosshairClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class AttackMixin {
    @Inject(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"), require = 0)
    private void focuscrosshair$attack(CallbackInfoReturnable<Boolean> callback) {
        FocusCrosshairClient.CROSSHAIR.attack();
    }
}
