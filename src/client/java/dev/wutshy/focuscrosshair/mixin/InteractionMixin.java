package dev.wutshy.focuscrosshair.mixin;

import dev.wutshy.focuscrosshair.FocusCrosshairClient;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.InteractionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class InteractionMixin {
    @Inject(method = {"useItemOn", "interact"}, at = @At("RETURN"), require = 0)
    private void focuscrosshair$interaction(CallbackInfoReturnable<InteractionResult> callback) {
        if (callback.getReturnValue() != null && callback.getReturnValue().consumesAction()) {
            FocusCrosshairClient.CROSSHAIR.interaction();
        }
    }
}
