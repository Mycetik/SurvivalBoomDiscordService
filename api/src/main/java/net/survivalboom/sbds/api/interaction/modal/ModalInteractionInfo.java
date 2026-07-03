package net.survivalboom.sbds.api.interaction.modal;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.interaction.*;
import net.survivalboom.sbds.api.utils.typemap.TypeMap;
import org.apache.commons.collections4.map.UnmodifiableMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ModalInteractionInfo extends InteractionExecutionInfo<ModalInteractionEvent> {

    private final Map<String, ModalMapping> fields;

    public ModalInteractionInfo(
            @NotNull ISBDS sbds,
            @NotNull ModalInteractionEvent event
    ) {
        super(event, true, sbds);
        fields = event.getValues().stream().collect(Collectors.toUnmodifiableMap(ModalMapping::getCustomId, m -> m));
    }

    public @NotNull Map<String, ModalMapping> fields() {
        return fields;
    }

    public @NotNull Optional<ModalMapping> field(@NotNull String key) {
        return Optional.ofNullable(fields.get(key));
    }

}
