package net.survivalboom.sbds.modules.test;

import net.survivalboom.sbds.api.modules.ModuleMain;
import net.survivalboom.sbds.modules.test.commands.TestCommand;
import net.survivalboom.sbds.modules.test.commands.context.TestMessageContext;
import net.survivalboom.sbds.modules.test.commands.context.TestUserContext;

import java.util.Map;

public class TestModule extends ModuleMain {

    @Override
    public void onEnable() {

        checkFiles(Map.of("translations/translation_uk.yml", "translations/translation_uk.yml"));
        addModuleTranslations();

        registerSlashCommand(new TestCommand());

        registeredContextCommand(new TestMessageContext());
        registeredContextCommand(new TestUserContext());

        getLogger().info("Модуль успішно запущено!");

    }

    @Override
    public void onDisable() {
        getModule().getLogger().info("Нєєєєт!!! Міня віключілііі!!!");
    }

}