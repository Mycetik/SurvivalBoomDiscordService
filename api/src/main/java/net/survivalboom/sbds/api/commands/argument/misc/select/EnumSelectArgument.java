package net.survivalboom.sbds.api.commands.argument.misc.select;

import org.jetbrains.annotations.NotNull;

public class EnumSelectArgument<E extends Enum<E>> extends AbstractSelectArgument<Enum<E>> {

    public EnumSelectArgument(@NotNull Class<E> clazz) {
        super(clazz.getEnumConstants());
    }

}
