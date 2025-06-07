package net.survivalboom.sbds.api.interaction.modal;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.interaction.InteractionInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class ModalInteractionInfo extends InteractionInfo<ModalInteractionEvent> {

    private final Map<String, String> map = new HashMap<>();

    public ModalInteractionInfo(@NotNull ISBDS sbds, @NotNull Logger logger, @NotNull ModalInteractionEvent event) {
        super(event, sbds, logger);
        event.getInteraction().getValues().forEach(v -> map.put(v.getId(), v.getAsString()));
    }

    public @Nullable String value(@NotNull String id) {
        return map.get(id);
    }

    public @NotNull Map<String, String> values() {
        return new HashMap<>(map);
    }

}
