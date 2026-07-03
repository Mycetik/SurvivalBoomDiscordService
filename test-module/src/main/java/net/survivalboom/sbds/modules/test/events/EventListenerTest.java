package net.survivalboom.sbds.modules.test.events;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.survivalboom.sbds.api.events.EventHandler;
import net.survivalboom.sbds.api.events.EventListener;
import net.survivalboom.sbds.modules.test.TestModule;

public class EventListenerTest implements EventListener {

    private final TestModule module;

    public EventListenerTest(TestModule module) {
        this.module = module;
    }

    @EventHandler
    public void onMessage(MessageReceivedEvent event) {

        if (event.getAuthor().isBot()) {
            return;
        }

        if (!event.getMessage().getContentRaw().contains("test")) {
            return;
        }

        event.getMessage().addReaction(Emoji.fromUnicode("✅")).queue();

        module.getSbds().getMessages().reply(event.getMessage(), "testmodule.event.on_message", event.getAuthor()).queue();

    }

}
