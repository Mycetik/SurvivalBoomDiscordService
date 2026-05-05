package net.survivalboom.sbds.api.commands.argument.discord;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.ArgumentParseException;
import net.survivalboom.sbds.api.commands.argument.ArgumentParsingContext;
import org.jetbrains.annotations.NotNull;

public class RoleArgument extends Argument<Role> {

    @Override
    public @NotNull Role parse(@NotNull Object input, @NotNull ArgumentParsingContext context) throws ArgumentParseException {

        if (input instanceof String string) {

            Role role = context.sbds().getBot().getRoleById(string);
            if (role == null) {
                throw new ArgumentParseException("Unknown role with id `" + string + "`");
            }

            return role;

        }

        if (input instanceof OptionMapping optionMapping) {
            return optionMapping.getAsRole();
        }

        throw new ArgumentParseException();

    }

    @Override
    public @NotNull OptionType getOptionType() {
        return OptionType.ROLE;
    }

}
