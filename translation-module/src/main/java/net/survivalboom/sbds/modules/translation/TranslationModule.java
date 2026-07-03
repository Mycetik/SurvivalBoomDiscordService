package net.survivalboom.sbds.modules.translation;

import net.survivalboom.sbds.api.modules.ModuleMain;
import net.survivalboom.sbds.modules.translation.commands.TranslationCommand;
import net.survivalboom.sbds.modules.translation.listeners.SlashCommandListener;

public class TranslationModule extends ModuleMain {

    @Override
    public void onEnable() {

        addModuleTranslations2(
                "translation_uk.yml",
                "translation_en.yml",
                "translation_ru.yml"
        );

        registerCommand(new TranslationCommand());
        registerEvents(new SlashCommandListener(this));

    }

}
