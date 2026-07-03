package net.survivalboom.sbds.api.commands.context;

import net.survivalboom.sbds.api.commands.ICommandManager;

public interface IContextCommandManager extends ICommandManager<IContextCommandManager.IRegisteredContextCommand, IContextCommandManager> {

    interface IRegisteredContextCommand extends ICommandManager.IRegisteredInteractionCommand<IRegisteredContextCommand, IContextCommandManager> {

    }

}
