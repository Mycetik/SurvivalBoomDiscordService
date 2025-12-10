package net.survivalboom.sbds.api.interaction.modal;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
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

    private final Map<String, ModalMapping> data = new HashMap<>();

    private final ModalInteractionEvent event;

    public ModalInteractionInfo(@NotNull ISBDS sbds, @NotNull Logger logger, @NotNull ModalInteractionEvent event) {
        super(sbds, logger);
        this.event = event;
        event.getValues().forEach(v -> data.put(v.getCustomId(), v));
    }

    public @Nullable ModalMapping getValue(@NotNull String id) {
        return data.get(id);
    }

    public @NotNull Map<Object, ModalMapping> getValues() {
        return new HashMap<>(data);
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
