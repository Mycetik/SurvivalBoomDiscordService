package net.survivalboom.sbds.api.commands.argument.sbds;

import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.ArgumentParseException;
import net.survivalboom.sbds.api.commands.argument.ArgumentParsingContext;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NamespacedKeyArgument extends Argument<NamespacedKey> {

    private final @Nullable String namespace;

    public NamespacedKeyArgument(@Nullable String namespace) {
        this.namespace = namespace;
    }

    public NamespacedKeyArgument() {
        this.namespace = null;
    }


    @Override
    public @NotNull NamespacedKey parse(@NotNull Object input, @NotNull ArgumentParsingContext context) throws ArgumentParseException {

        String string;
        if (input instanceof String s) {
            string = s;
        }

        else if (input instanceof OptionMapping mapping) {
            string = mapping.getAsString();
        }

        else {
            throw new ArgumentParseException("Invalid object `" + input + "`");
        }

        try {

            if (namespace != null) {
                return NamespacedKey.create(namespace, string);
            }

            else {
                return NamespacedKey.fromString(string);
            }

        }

        catch (IllegalArgumentException e) {
            throw new ArgumentParseException("Invalid namespaced key `" + string + "`: " + e.getMessage());
        }

    }


    @Override
    public @NotNull OptionType getOptionType() {
        return OptionType.STRING;
    }

}
