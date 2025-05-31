package net.survivalboom.sbds.modules.test;

import net.survivalboom.sbds.api.modules.ModuleMain;
import net.survivalboom.sbds.modules.test.commands.TestCommand;
import net.survivalboom.sbds.modules.test.commands.context.TestMessageContext;

import java.util.Map;

public class TestModule extends ModuleMain {

    @Override
    public void onEnable() {

        getLogger().info("Я ЖИВИИИИЙЙЙ!!!!!!");

        checkFiles(Map.of("translations/translation_uk.yml", "translations/translation_uk.yml"));
        addModuleTranslation();

        registerSlashCommand(new TestCommand());
        registeredContextCommand(new TestMessageContext());

    }

    @Override
    public void onDisable() {
        getModule().getLogger().info("Нєєєєт!!! Міня віключілііі!!!");
    }

}