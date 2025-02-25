package net.survivalboom.sbds.api.commands.argument;

import org.jetbrains.annotations.NotNull;

public abstract class SimpleArgument<T> extends Argument<T> {

    @Override
    public int split(@NotNull String input) {

        for (int i = 0; i < input.length(); i++) {

            char c = input.charAt(i);
            if (!Character.isSpaceChar(c)) continue;

            return i;

        }

        return input.length();

    }

}
