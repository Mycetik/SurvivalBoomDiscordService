package net.survivalboom.sbds.api.interaction;

import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.survivalboom.sbds.api.ISBDS;
import org.jetbrains.annotations.NotNull;

public class ComponentInteractionInfo<
        event extends GenericComponentInteractionCreateEvent,
        reg extends IComponentInteractionManager.IRegisteredComponent<event, reg>
> extends InteractionExecutionInfo<event> implements InteractionHolder {

    private final reg reg;

    public ComponentInteractionInfo(
            @NotNull event event,
            @NotNull reg reg,
            @NotNull ISBDS sbds
    ) {
        super(event, reg.isEphemeral(), sbds);
        this.reg = reg;
    }

    public @NotNull reg component() {
        return reg;
    }

}
