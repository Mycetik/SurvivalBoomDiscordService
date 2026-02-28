package net.survivalboom.sbds.api.interaction.dropdown.string;

import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.interaction.ComponentInteractionInfo;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.List;

public class StringDropdownInteractionInfo extends ComponentInteractionInfo<StringSelectInteractionEvent> {

    public StringDropdownInteractionInfo(@NotNull StringSelectInteractionEvent event, @NotNull ISBDS sbds, @NotNull Logger logger) {
        super(event, sbds, logger);
    }

    public @NotNull List<SelectOption> getSelectedOptions() {
        return event().getSelectedOptions();
    }

}
