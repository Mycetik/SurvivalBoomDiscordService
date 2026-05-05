package net.survivalboom.sbds.api.commands.string;

import net.survivalboom.sbds.api.commands.ICommandManager;

public interface IStringCommandManager extends ICommandManager<IStringCommandManager.IRegisteredStringCommand, IStringCommandManager> {

    interface IRegisteredStringCommand extends ICommandManager.IRegisteredCommand<IRegisteredStringCommand, IStringCommandManager> {

    }

}
