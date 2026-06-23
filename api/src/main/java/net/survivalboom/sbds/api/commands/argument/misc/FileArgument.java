package net.survivalboom.sbds.api.commands.argument.misc;

import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.ArgumentAutoCompleteContext;
import net.survivalboom.sbds.api.commands.argument.ArgumentParseException;
import net.survivalboom.sbds.api.commands.argument.ArgumentParsingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FileArgument extends Argument<File> {

    private final File dir;

    private final boolean mustExist;

    public FileArgument(@NotNull File dir, boolean mustExist) {
        Objects.requireNonNull(dir, "dir == null");
        this.dir = dir;
        this.mustExist = mustExist;
    }


    @Override
    public @NotNull File parse(@NotNull Object input, @NotNull ArgumentParsingContext context) throws ArgumentParseException {

        if (input instanceof String string) {

            File file = new File(dir, string);
            if (!file.exists() && mustExist) {
                throw new ArgumentParseException("File `" + file.getName() + "` does not exist");
            }

            return file;

        }

        throw new ArgumentParseException();

    }

    @Override
    public @Nullable List<Command.Choice> onArgumentAutoComplete(@NotNull ArgumentAutoCompleteContext context) {

        if (dir.exists() || !dir.isDirectory()) {
            return null;
        }

        return Arrays.stream(dir.listFiles())
                .map(file -> new Command.Choice(file.getName(), file.getName()))
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
