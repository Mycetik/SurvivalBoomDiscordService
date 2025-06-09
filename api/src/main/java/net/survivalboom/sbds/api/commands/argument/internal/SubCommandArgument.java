package net.survivalboom.sbds.api.commands.argument.internal;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.argument.ArgumentParseException;
import net.survivalboom.sbds.api.commands.argument.SimpleArgument;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class SubCommandArgument extends SimpleArgument<SubCommandArgument.SubCommand> {

    private final List<Command> subcommands = new ArrayList<>();

    public SubCommandArgument(@NotNull Collection<Command> subcommands) {
        this.subcommands.addAll(subcommands);
    }

    @Override
    protected @NotNull SubCommand parse0(@NotNull Object input, @NotNull ArgumentResources resources) throws ArgumentParseException {

        if (input instanceof String string) {

            Optional<Command> optional = subcommands.stream().filter(c -> c.getName().equals(string) || c.aliases().contains(string)).findAny();
            if (optional.isEmpty()) throw new ArgumentParseException("Invalid subcommand `" + string + "`");

            return new SubCommand(optional.get(), string);

        }

        throw new ArgumentParseException();

    }

    @Override
    public @NotNull OptionType getOptionType() {
        return OptionType.STRING;
    }


    public record SubCommand(@NotNull Command command, @NotNull String alias) {}

}
