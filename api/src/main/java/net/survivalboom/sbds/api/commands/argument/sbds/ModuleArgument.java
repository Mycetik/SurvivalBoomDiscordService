package net.survivalboom.sbds.api.commands.argument.sbds;

import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.ArgumentParseException;
import net.survivalboom.sbds.api.commands.argument.ArgumentParsingContext;
import net.survivalboom.sbds.api.modules.IModule;
import org.jetbrains.annotations.NotNull;

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

        if (input instanceof String s) {

            IModule module = context.sbds().getModuleManager().getModule(s);
            if (module == null) {
                throw new ArgumentParseException("Invalid module `" + s + "`");
            }

            if (enabled != null && module.isEnabled() != enabled) {
                throw new ArgumentParseException("Invalid module `" + s + "`");
            }

            return module;

        }

        else if (input instanceof OptionMapping mapping) {

            String s = mapping.getAsString();

            IModule module = context.sbds().getModuleManager().getModule(s);
            if (module == null) {
                throw new ArgumentParseException("Invalid module `" + s + "`");
            }

            if (enabled != null && module.isEnabled() != enabled) {
                throw new ArgumentParseException("Invalid module `" + s + "`");
            }

            return module;

        }

        throw new ArgumentParseException("Invalid object `" + input + "`");

    }

    @Override
    public @NotNull OptionType getOptionType() {
        return OptionType.STRING;
    }

}
