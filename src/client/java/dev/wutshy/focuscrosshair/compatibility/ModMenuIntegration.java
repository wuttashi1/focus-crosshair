package dev.wutshy.focuscrosshair.compatibility;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.wutshy.focuscrosshair.config.FocusConfigScreen;

public final class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() { return FocusConfigScreen::new; }
}
