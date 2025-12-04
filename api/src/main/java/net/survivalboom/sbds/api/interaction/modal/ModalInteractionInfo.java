package net.survivalboom.sbds.api.interaction.modal;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Mentions;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.interaction.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;

public class ModalInteractionInfo extends ExecutionInfo implements MessageReplyable, HookEditable, GuildExecution {

    private final Map<String, ModalMapping> mappingMap = new HashMap<>();
    private final Map<String, String> stringValues = new HashMap<>();

    private final ModalInteractionEvent event;

    public ModalInteractionInfo(@NotNull ISBDS sbds, @NotNull Logger logger, @NotNull ModalInteractionEvent event) {
        super(sbds, logger);
        this.event = event;
        event.getValues().forEach(v -> {
            mappingMap.put(v.getCustomId(), v);
            extractStringValue(v).ifPresent(value -> stringValues.put(v.getCustomId(), value));
        });
    }

    private Optional<String> extractStringValue(ModalMapping mapping) {
        return switch (mapping.getType()) {
            case TEXT_INPUT -> Optional.ofNullable(mapping.getAsString());
            case STRING_SELECT, ROLE_SELECT, USER_SELECT, MENTIONABLE_SELECT, CHANNEL_SELECT -> {
                List<String> values = mapping.getAsStringList();
                yield values == null || values.isEmpty() ? Optional.empty() : Optional.of(String.join(", ", values));
            }
            default -> Optional.empty();
        };
    }

    public @Nullable String value(@NotNull String id) {
        return stringValues.get(id);
    }

    public @NotNull Map<String, String> values() {
        return new HashMap<>(stringValues);
    }

    public @Nullable List<String> valueList(@NotNull String id) {
        ModalMapping mapping = mappingMap.get(id);
        return mapping != null ? mapping.getAsStringList() : null;
    }

    public @Nullable Mentions mentions(@NotNull String id) {
        ModalMapping mapping = mappingMap.get(id);
        return mapping != null ? mapping.getAsMentions() : null;
    }

    public @Nullable List<Message.Attachment> attachments(@NotNull String id) {
        ModalMapping mapping = mappingMap.get(id);
        return mapping != null ? mapping.getAsAttachmentList() : null;
    }

    public @NotNull Map<String, ModalMapping> mappings() {
        return new HashMap<>(mappingMap);
    }

    @Override
    public Guild guild() {
        return event.getGuild();
    }

    @Override
    public Member member() {
        return event.getMember();
    }

    @Override
    public @NotNull InteractionHook hook() {
        return event.getHook();
    }

    @Override
    public @NotNull IReplyCallback replyCallback() {
        return event;
    }

    @Override
    public @NotNull User user() {
        return event.getUser();
    }

}
