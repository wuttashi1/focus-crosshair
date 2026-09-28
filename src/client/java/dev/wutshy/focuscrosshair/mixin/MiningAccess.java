package dev.wutshy.focuscrosshair.mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MultiPlayerGameMode.class)
public interface MiningAccess {
    @Accessor("destroyProgress") float focuscrosshair$progress();
    @Accessor("destroyBlockPos") BlockPos focuscrosshair$position();
}
