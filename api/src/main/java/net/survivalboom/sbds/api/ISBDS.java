package net.survivalboom.sbds.api;

import net.dv8tion.jda.api.JDA;
import net.survivalboom.sbds.api.commands.slash.ISlashCommandManager;
import net.survivalboom.sbds.api.commands.console.IConsoleListener;
import net.survivalboom.sbds.api.database.IDatabase;
import net.survivalboom.sbds.api.events.IEventManager;
import net.survivalboom.sbds.api.libraries.ILibrariesManager;
import net.survivalboom.sbds.api.messages.IMessages;
import net.survivalboom.sbds.api.modules.IModuleManager;
import net.survivalboom.sbds.api.monitoring.ISystemMonitor;
import net.survivalboom.sbds.api.permissions.IPermissionManager;
import net.survivalboom.sbds.api.scheduler.IScheduler;
import net.survivalboom.sbds.api.translations.ITranslationManager;
import org.bspfsystems.yamlconfiguration.configuration.Configuration;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;

public interface ISBDS {

    @NotNull String getVersion();


    @NotNull IScheduler getScheduler();

    @NotNull ISystemMonitor getSystemMonitor();


    @NotNull IModuleManager getModuleManager();

    @NotNull ILibrariesManager getLibrariesManager();


    @NotNull IEventManager getEventManager();


    @NotNull IPermissionManager getPermissionManager();

    @NotNull IConsoleListener getConsoleListener();

    @NotNull ISlashCommandManager getSlashCommandManager();


    @NotNull IDatabase getDatabase();

    @NotNull ITranslationManager getTranslationManager();

    @NotNull IMessages getMessages();


    @NotNull File getWorkingDir();

    @NotNull Configuration getConfiguration();

    @NotNull Logger getLogger();

    @NotNull JDA getBot();



    void shutdown();

}
