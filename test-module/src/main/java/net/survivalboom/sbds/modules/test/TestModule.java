package net.survivalboom.sbds.modules.test;

import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.survivalboom.sbds.api.interaction.modal.IModalInteractionManager;
import net.survivalboom.sbds.api.interaction.modal.ModalTemplate;
import net.survivalboom.sbds.api.modules.ModuleMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestModule extends ModuleMain {

    private static final Logger log = LoggerFactory.getLogger(TestModule.class);

    @Override
    public void onEnable() {

        getModule().getLogger().info("Я ЖИВИИИИЙЙЙ!!!!!!");
        getModule().getSbds().getConsoleListener().registerCommand(this, new SayCommand());

        IModalInteractionManager.IRegisteredModal modal = getSbds().getModalInteractionManager().registerModal(this, "sex", ModalTemplate.builder().addInput("lox", "Вас ебали?", "ЛООООХ", TextInputStyle.SHORT).build());

        getModule().getSbds().getSlashCommandManager().registerCommand(this, new TestCommand(modal));
        getModule().getSbds().getSlashCommandManager().registerCommand(this, new SayCommand());

        getModule().getSbds().getSlashCommandManager().registerCommand(this, new TranslationCommand());

    }

    @Override
    public void onDisable() {
        getModule().getLogger().info("Нєєєєт!!! Міня віключілііі!!!");
    }

}