package net.survivalboom.sbds.api.interaction.modal;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.interaction.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class ModalInteractionInfo extends ExecutionInfo implements MessageReplyable, HookEditable, GuildExecution {

    private final Map<String, String> map = new HashMap<>();

    private final ModalInteractionEvent event;

    public ModalInteractionInfo(@NotNull ISBDS sbds, @NotNull Logger logger, @NotNull ModalInteractionEvent event) {
        super(sbds, logger);
        this.event = event;
        event.getInteraction().getValues().forEach(v -> map.put(v.getId(), v.getAsString()));
    }

    public @Nullable String value(@NotNull String id) {
        return map.get(id);
    }

    public @NotNull Map<String, String> values() {
        return new HashMap<>(map);
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
