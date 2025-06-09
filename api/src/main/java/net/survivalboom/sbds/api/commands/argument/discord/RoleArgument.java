package net.survivalboom.sbds.api.commands.argument.discord;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.survivalboom.sbds.api.commands.argument.ArgumentParseException;
import net.survivalboom.sbds.api.commands.argument.SimpleArgument;
import org.jetbrains.annotations.NotNull;

public class RoleArgument extends SimpleArgument<Role> {

    @NotNull
    @Override
    protected Role parse0(@NotNull Object input, @NotNull ArgumentResources resources) throws ArgumentParseException {

        if (input instanceof String string) {

            Role role = resources.sbds().getBot().getRoleById(string);
            if (role == null) throw new ArgumentParseException("Unknown role with id `" + string + "`");

            return role;

        }

        if (input instanceof OptionMapping optionMapping) {
            return optionMapping.getAsRole();
        }

        throw new ArgumentParseException();

    }

    @NotNull
    @Override
    public OptionType getOptionType() {
        return null;
    }

}
