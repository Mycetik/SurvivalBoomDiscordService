package net.survivalboom.sbds.api.commands.argument.misc;

import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.survivalboom.sbds.api.commands.argument.ArgumentParseException;
import net.survivalboom.sbds.api.commands.argument.ArgumentResources;
import net.survivalboom.sbds.api.commands.argument.SimpleArgument;
import net.survivalboom.sbds.api.modules.IModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModuleArgument extends SimpleArgument<IModule> {

    private final Boolean enabled;

    public ModuleArgument(@Nullable Boolean enabled) {
        this.enabled = enabled;
    }

    public ModuleArgument() {
        this.enabled = null;
    }

    @Override
    protected @NotNull IModule parse0(@NotNull Object input, @NotNull ArgumentResources resources) throws ArgumentParseException {

        if (input instanceof String s) {

            IModule module = resources.sbds().getModuleManager().getModule(s);
            if (module == null) throw new ArgumentParseException("Invalid module `" + s + "`");

            if (enabled != null && module.isEnabled() != enabled) throw new ArgumentParseException("Invalid module `" + s + "`");

            return module;

        }

        else if (input instanceof OptionMapping mapping) {

            String s = mapping.getAsString();

            IModule module = resources.sbds().getModuleManager().getModule(s);
            if (module == null) throw new ArgumentParseException("Invalid module `" + s + "`");

            if (enabled != null && module.isEnabled() != enabled) throw new ArgumentParseException("Invalid module `" + s + "`");

            return module;

        }

        throw new ArgumentParseException("Invalid object `" + input + "`");

    }

    @Override
    public @NotNull OptionType getOptionType() {
        return OptionType.STRING;
    }

}
