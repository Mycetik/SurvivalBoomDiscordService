package net.survivalboom.sbds.api.commands.slash;

import net.survivalboom.sbds.api.commands.ICommandManager;

public interface ISlashCommandManager extends ICommandManager<ISlashCommandManager.IRegisteredSlashCommand, ISlashCommandManager> {

    interface IRegisteredSlashCommand extends IRegisteredCommand<IRegisteredSlashCommand, ISlashCommandManager> {

    }

}
