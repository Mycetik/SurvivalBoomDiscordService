package net.survivalboom.sbds.api;

import net.dv8tion.jda.api.JDA;
import net.survivalboom.sbds.api.commands.slash.ISlashCommandManager;
import net.survivalboom.sbds.api.commands.console.IConsoleListener;
import net.survivalboom.sbds.api.database.IDatabase;
import net.survivalboom.sbds.api.events.IEventManager;
import net.survivalboom.sbds.api.interaction.button.IButtonInteractionManager;
import net.survivalboom.sbds.api.commands.context.IContextCommandManager;
import net.survivalboom.sbds.api.interaction.dropdown.entity.IEntityDropdownInteractionManager;
import net.survivalboom.sbds.api.interaction.dropdown.string.IStringDropdownInteractionManager;
import net.survivalboom.sbds.api.interaction.modal.IModalInteractionManager;
import net.survivalboom.sbds.api.libraries.ILibrariesManager;
import net.survivalboom.sbds.api.messages.IMessages;
import net.survivalboom.sbds.api.modules.IModuleManager;
import net.survivalboom.sbds.api.monitoring.ISystemMonitor;
import net.survivalboom.sbds.api.permissions.IPermissionManager;
import net.survivalboom.sbds.api.registrations.IRegistrationRegistry;
import net.survivalboom.sbds.api.scheduler.IScheduler;
import net.survivalboom.sbds.api.service.IServiceProvider;
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

    @NotNull IRegistrationRegistry getRegistrationRegistry();

    @NotNull IServiceProvider getServiceProvider();

    @NotNull ILibrariesManager getLibrariesManager();


    @NotNull IEventManager getEventManager();


    @NotNull IPermissionManager getPermissionManager();

    @NotNull IConsoleListener getConsoleListener();

    @NotNull ISlashCommandManager getSlashCommandManager();

    @NotNull IContextCommandManager getContextCommandManager();


    @NotNull IModalInteractionManager getModalInteractionManager();

    @NotNull IStringDropdownInteractionManager getStringDropdownInteractionManager();

    @NotNull IEntityDropdownInteractionManager getEntityDropdownInteractionManager();

    @NotNull IButtonInteractionManager getButtonInteractionManager();


    @NotNull IDatabase getDatabase();

    @NotNull ITranslationManager getTranslationManager();

    @NotNull IMessages getMessages();


    @NotNull File getWorkingDir();

    @NotNull Configuration getConfiguration();

    @NotNull Logger getLogger();

    @NotNull JDA getBot();


    boolean isReady();

    boolean isStarted();


    void shutdown();

}
