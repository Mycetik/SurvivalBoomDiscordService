package net.survivalboom.sbds.modules.guildconfig;

import net.survivalboom.sbds.api.modules.ModuleMain;
import net.survivalboom.sbds.modules.guildconfig.commands.ConfigCommand;

public class GuildConfigModule extends ModuleMain {

    @Override
    public void onEnable() {

        addModuleTranslations2(
                "translation_uk.yml",
                "translation_ru.yml",
                "translation_en.yml"
        );

        registerCommand(new ConfigCommand());

    }

}
