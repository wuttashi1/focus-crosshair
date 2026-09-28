package dev.wutshy.focuscrosshair;

import com.mojang.blaze3d.platform.InputConstants;
import dev.wutshy.focuscrosshair.config.ConfigManager;
import dev.wutshy.focuscrosshair.config.FocusConfigScreen;
import dev.wutshy.focuscrosshair.render.CrosshairRenderer;
import dev.wutshy.focuscrosshair.render.CustomCrosshair;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public final class FocusCrosshairClient implements ClientModInitializer {
    public static final ConfigManager CONFIG = new ConfigManager(FabricLoader.getInstance().getConfigDir().resolve("focuscrosshair.json"));
    public static final CrosshairRenderer CROSSHAIR = new CrosshairRenderer();
    public static final CustomCrosshair CUSTOM = new CustomCrosshair();

    @Override
    public void onInitializeClient() {
        CONFIG.load();
        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("focuscrosshair", "general"));
        KeyMapping toggle = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.focuscrosshair.toggle",
            InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(),
            category));
        KeyMapping settings = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.focuscrosshair.settings",
            InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), category));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            CUSTOM.loadOnce();
            while (toggle.consumeClick()) {
                CONFIG.config.enabled = !CONFIG.config.enabled;
                CROSSHAIR.reset();
                CONFIG.save();
            }
            while (settings.consumeClick()) {
                if (client.gui.screen() == null) client.gui.setScreen(new FocusConfigScreen(null));
            }
            CROSSHAIR.tick(client);
        });
        HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, previous -> (graphics, delta) -> {
            if (!CONFIG.config.enabled) previous.extractRenderState(graphics, delta);
            else CROSSHAIR.extract(graphics, delta);
        });
    }
}
