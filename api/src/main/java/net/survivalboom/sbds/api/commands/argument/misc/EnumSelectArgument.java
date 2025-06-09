package net.survivalboom.sbds.api.commands.argument.misc;

import net.survivalboom.sbds.api.commands.argument.internal.AbstractSelectArgument;
import org.jetbrains.annotations.NotNull;

public class EnumSelectArgument<E extends Enum<E>> extends AbstractSelectArgument<Enum<E>> {

    public EnumSelectArgument(@NotNull Class<E> clazz) {
        super(clazz.getEnumConstants());
    }

}
