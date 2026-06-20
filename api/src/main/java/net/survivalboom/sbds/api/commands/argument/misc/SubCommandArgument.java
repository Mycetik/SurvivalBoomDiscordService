package net.survivalboom.sbds.api.commands.argument.misc;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.ArgumentParseException;
import net.survivalboom.sbds.api.commands.argument.ArgumentParsingContext;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SubCommandArgument extends Argument<SubCommandArgument.SubCommand> {

    private final Map<String, Command> subcommands = new HashMap<>();

    private final Map<String, Command> subcommandsByAliases = new HashMap<>();

    public SubCommandArgument(@NotNull Collection<Command> subcommands) {

        Objects.requireNonNull(subcommands, "subcommands == null");

        if (subcommands.isEmpty()) {
            throw new IllegalArgumentException("subcommands are empty!");
        }

        for (Command command : subcommands) {

            String name = command.getName();
            if (this.subcommandsByAliases.containsKey(name)) {
                throw new IllegalArgumentException("Duplicate subcommand `" + name + "in " + subcommands);
            }

            this.subcommandsByAliases.put(name, command);
            this.subcommands.put(name, command);

            for (String alias : command.getAliases()) {

                if (this.subcommandsByAliases.containsKey(alias)) {
                    throw new IllegalArgumentException("Duplicate subcommand `" + alias + "in " + subcommands);
                }

                this.subcommandsByAliases.put(alias, command);

            }

        }

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

            Command command = subcommandsByAliases.get(string);
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

    public @Nullable Command getSubCommand(@NotNull String alias) {
        return subcommandsByAliases.get(alias);
    }

    public @NotNull Map<String, Command> getSubCommandsAliases() {
        return new HashMap<>(subcommandsByAliases);
    }

    public @NotNull List<Command> getSubcommands() {
        return new ArrayList<>(subcommands.values());
    }

    public record SubCommand(@NotNull Command command, @NotNull String alias) {}

}
