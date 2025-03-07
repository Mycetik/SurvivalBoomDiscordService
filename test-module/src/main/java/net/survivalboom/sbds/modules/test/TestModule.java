package net.survivalboom.sbds.modules.test;

import net.datafaker.Faker;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.survivalboom.sbds.api.events.EventHandler;
import net.survivalboom.sbds.api.events.Listener;
import net.survivalboom.sbds.api.modules.ModuleMain;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class TestModule extends ModuleMain implements Listener {

    private static final Logger log = LoggerFactory.getLogger(TestModule.class);

    private final Faker faker = new Faker(Locale.of("russian"));

    @Override
    public void onEnable() {

        getModule().getLogger().info("Я ЖИВИИИИЙЙЙ!!!!!!");

        getModule().getSbds().getEventManager().registerEvents(this, this);

        getModule().getSbds().getConsoleListener().registerCommand(this, new TestCommand());
        getModule().getSbds().getConsoleListener().registerCommand(this, new SayCommand());

        getModule().getSbds().getSlashCommandManager().registerCommand(this, new TestCommand());
        getModule().getSbds().getSlashCommandManager().registerCommand(this, new SayCommand());



    }

    @Override
    public void onDisable() {
        getModule().getLogger().info("Нєєєєт!!! Міня віключілііі!!!");
    }

    @EventHandler
    public void onMessage(@NotNull MessageReceivedEvent event) {

        if (event.getAuthor().isBot()) return;

        event.getMessage().reply(faker.address().country()).queue();

    }

}