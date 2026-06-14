package net.survivalboom.sbds.modules.test;

import net.survivalboom.sbds.api.modules.ModuleMain;
import net.survivalboom.sbds.modules.test.commands.*;
import net.survivalboom.sbds.modules.test.events.EventListenerTest;

public class TestModule extends ModuleMain {

    @Override
    public void onEnable() {

        addModuleTranslations2("translation_uk.yml");

        registerCommand(new BanPrototypeCommand());
        registerCommand(new EphemeralCommand());
        registerCommand(new LongRespondingCommand());

        registerCommand(new TestMessageContext());
        registerCommand(new TestUserContext());

        registerCommand(new TestModalCommand());

        registerEvents(new EventListenerTest(this));

        getLogger().info("Бугага! Мєня включілі! Вам всім кабздєц!");

    }

    @Override
    public void onDisable() {
        getModule().getLogger().info("Нєєєєт!!! Міня віключілііі!!!");
    }

}