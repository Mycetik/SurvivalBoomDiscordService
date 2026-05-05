package net.survivalboom.sbds.api.messages.components;

import net.dv8tion.jda.api.components.Component;
import org.jetbrains.annotations.Nullable;

public interface MessageInteractableComponentTemplate<T extends Component> extends ComponentTemplate<T> {

    @Nullable String getName();

    boolean isStatic();

}
