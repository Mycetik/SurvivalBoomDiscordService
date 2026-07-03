package net.survivalboom.sbds.api.commands.console;


import net.survivalboom.sbds.api.commands.ICommandManager;

public interface IConsoleListener extends ICommandManager<IConsoleListener.IRegisteredConsoleCommand, IConsoleListener> {

    interface IRegisteredConsoleCommand extends IRegisteredCommand<IRegisteredConsoleCommand, IConsoleListener> {

    }

}
