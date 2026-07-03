package net.survivalboom.sbds.api.messages.components;

import net.dv8tion.jda.api.components.Component;
import net.survivalboom.sbds.api.messages.parsers.StringParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ComponentTemplate<T extends Component> {

    //
    // PROPERTIES
    //

    // placement //

    int getRow();

    // component //

    @NotNull Class<T> getComponentClass();

    @NotNull Component.Type getType();

    // BUILD //

    @NotNull T build(@Nullable StringParser parser, @Nullable ComponentLinker linker);

}
