package dev.vacuum.client.gui;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * VacuumModMenuIntegration — registers the Vacuum settings screen
 * with Mod Menu so users can access it from the mods list.
 */
@Environment(EnvType.CLIENT)
public class VacuumModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new VacuumOptionsScreen(parent);
    }
}
