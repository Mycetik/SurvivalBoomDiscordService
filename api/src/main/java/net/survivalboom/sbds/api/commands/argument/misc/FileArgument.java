package net.survivalboom.sbds.api.commands.argument.misc;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.commands.argument.ArgumentParseException;
import net.survivalboom.sbds.api.commands.argument.SimpleArgument;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;
import java.util.function.Function;

public class FileArgument extends SimpleArgument<File> {

    private final File dir;

    private final Function<ISBDS, File> function;

    private final boolean mustExist;

    public FileArgument(@NotNull File dir, boolean mustExist) {
        Objects.requireNonNull(dir, "dir == null");
        this.dir = dir;
        this.mustExist = mustExist;
        this.function = null;
    }

    public FileArgument(@NotNull Function<ISBDS, File> function, boolean mustExist) {
        Objects.requireNonNull(function, "function == null");
        this.function = function;
        this.mustExist = mustExist;
        this.dir = null;
    }

    private @NotNull File getDir(@NotNull ISBDS sbds) {

        if (dir != null) return dir;
        if (function != null) return function.apply(sbds);

        throw new NullPointerException();

    }


    @Override
    protected @NotNull File parse0(@NotNull Object input, @NotNull ArgumentResources resources) throws ArgumentParseException {

        if (input instanceof String string) {

            File file = new File(getDir(resources.sbds()), string);
            if (!file.exists() && mustExist) throw new ArgumentParseException("File `" + file.getName() + "` does not exist");

            return file;

        }

        throw new ArgumentParseException();
    }

    @Override
    public @NotNull OptionType getOptionType() {
        return OptionType.STRING;
    }
}
