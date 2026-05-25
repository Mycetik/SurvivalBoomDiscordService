package net.survivalboom.sbds.api.commands.argument.misc;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.CommandArgument;
import net.survivalboom.sbds.api.commands.CommandExecutionInfo;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.ArgumentExecutionContext;
import net.survivalboom.sbds.api.commands.argument.ArgumentParseException;
import net.survivalboom.sbds.api.commands.argument.ArgumentParsingContext;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;

public class SubCommandArgument extends Argument<SubCommandArgument.SubCommand> {

    private final List<Command> subcommands = new ArrayList<>();

    public SubCommandArgument(@NotNull Collection<Command> subcommands) {

        Objects.requireNonNull(subcommands, "subcommands == null");

        if (subcommands.isEmpty()) {
            throw new IllegalArgumentException("subcommands are empty!");
        }

        this.subcommands.addAll(subcommands);

    }

    public SubCommandArgument(@NotNull Command... subcommands) {
        this(List.of(subcommands));
    }

    public SubCommandArgument(@NotNull CommandBase... subcommands) {
        this(Arrays.stream(subcommands).map(CommandBase::build).toList());
    }

    @Override
    public @NotNull SubCommand parse(@NotNull Object input, @NotNull ArgumentParsingContext context) throws ArgumentParseException {

        if (input instanceof String string) {

            Command command = subcommands.stream()
                    .filter(c -> c.getName().equals(string) || c.getAliases().contains(string))
                    .findAny()
                    .orElse(null);

            if (command == null) {
                throw new ArgumentParseException("Invalid subcommand `" + string + "`");
            }

            return new SubCommand(command, string);

        }

        throw new ArgumentParseException();

    }

    @Override
    public @NotNull OptionType getOptionType() {
        return OptionType.STRING;
    }

    public @NotNull List<Command> getSubcommands() {
        return new ArrayList<>(subcommands);
    }

    public record SubCommand(@NotNull Command command, @NotNull String alias) {}

}
