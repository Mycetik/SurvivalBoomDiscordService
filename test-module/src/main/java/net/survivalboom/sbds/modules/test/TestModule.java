package net.survivalboom.sbds.modules.test;

import net.survivalboom.sbds.api.modules.ModuleMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestModule extends ModuleMain {

    private static final Logger log = LoggerFactory.getLogger(TestModule.class);

    @Override
    public void onEnable() {

        getModule().getLogger().info("Я ЖИВИИИИЙЙЙ!!!!!!");
        getModule().getSbds().getConsoleListener().registerCommand(this, new SayCommand());

        getModule().getSbds().getSlashCommandManager().registerCommand(this, new TestCommand(this));
        getModule().getSbds().getSlashCommandManager().registerCommand(this, new SayCommand());

        getModule().getSbds().getSlashCommandManager().registerCommand(this, new TranslationCommand());

    }

    @Override
    public void onDisable() {
        getModule().getLogger().info("Нєєєєт!!! Міня віключілііі!!!");
    }

}