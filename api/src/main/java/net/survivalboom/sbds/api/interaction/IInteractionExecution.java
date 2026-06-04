package net.survivalboom.sbds.api.interaction;

import net.dv8tion.jda.api.interactions.Interaction;
import org.jetbrains.annotations.NotNull;

public interface IInteractionExecution<T extends Interaction> extends IBasicInteractionExecution, CanModal {

    @NotNull T interaction();

}
