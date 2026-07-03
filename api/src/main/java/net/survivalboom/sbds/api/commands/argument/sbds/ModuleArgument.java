package net.survivalboom.sbds.api.commands.argument.sbds;

import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.ArgumentAutoCompleteContext;
import net.survivalboom.sbds.api.commands.argument.ArgumentParseException;
import net.survivalboom.sbds.api.commands.argument.ArgumentParsingContext;
import net.survivalboom.sbds.api.modules.IModule;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ModuleArgument extends Argument<IModule> {

    private final Boolean enabled;

    public ModuleArgument(boolean mustBeEnabled) {
        this.enabled = mustBeEnabled;
    }

    public ModuleArgument() {
        this.enabled = null;
    }

    @Override
    public @NotNull IModule parse(@NotNull Object input, @NotNull ArgumentParsingContext context) throws ArgumentParseException {

        String moduleName;
        if (input instanceof String s) {
            moduleName = s;
        }

        else if (input instanceof OptionMapping mapping) {
            moduleName = mapping.getAsString();
        }

        else {
            throw new ArgumentParseException("Invalid object `" + input + "`");
        }

        IModule module = context.sbds().getModuleManager().getModule(moduleName.toLowerCase());
        if (module == null) {
            throw new ArgumentParseException("Invalid module `" + moduleName + "`");
        }

        if (enabled != null && module.isEnabled() != enabled) {
            throw new ArgumentParseException("Invalid module `" + moduleName + "`");
        }

        return module;

    }

    @Override
    public List<Command.Choice> onArgumentAutoComplete(@NotNull ArgumentAutoCompleteContext context) {
        return context.sbds().getModuleManager().getModules()
                .stream()
                .map(module -> new Command.Choice(module.getName(), module.getId()))
                .toList();
    }

    @Override
    public @NotNull OptionType getOptionType() {
        return OptionType.STRING;
    }

    @Override
    public boolean isAutoComplete() {
        return true;
    }

}
