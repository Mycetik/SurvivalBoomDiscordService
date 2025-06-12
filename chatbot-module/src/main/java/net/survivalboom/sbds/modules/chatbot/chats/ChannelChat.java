package net.survivalboom.sbds.modules.chatbot.chats;

import com.openai.models.chat.completions.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReference;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ChannelChat {

    private final ChatManager manager;

    private final TextChannel channel;

    private final JDA bot;

    private final List<Message> messages = new ArrayList<>();


    public ChannelChat(@NotNull ChatManager chatManager, @NotNull TextChannel channel, @NotNull JDA bot) {
        this.manager = chatManager;
        this.channel = channel;
        this.bot = bot;
    }


    public @NotNull TextChannel getChannel() {
        return channel;
    }


    public @Nullable Message getLastMessage() {
        if (messages.isEmpty()) return null;
        return messages.getLast();
    }

    public void putMessage(@NotNull Message message) {
        this.messages.add(message);
    }

    public void putMessages(@NotNull List<Message> messages) {
        this.messages.addAll(messages);
    }


    public @NotNull List<ChatCompletionMessageParam> generateMessages(@NotNull String prompt) {
        List<ChatCompletionMessageParam> messageParams = new ArrayList<>(messages.stream().map(this::generateMessage).toList());
        messageParams.addFirst(ChatCompletionMessageParam.ofSystem(ChatCompletionSystemMessageParam.builder().content(prompt).build()));
        return messageParams;
    }

    private @NotNull ChatCompletionMessageParam generateMessage(@NotNull Message message) {

        User user = message.getAuthor();
        boolean assistant = manager.getModule().getSbds().getBot().getSelfUser().equals(user);

        String msg = createMessageContent(message);

        ChatCompletionMessageParam chatCompletion;
        if (assistant) chatCompletion = ChatCompletionMessageParam.ofAssistant(ChatCompletionAssistantMessageParam.builder().content(msg).build());
        else chatCompletion = ChatCompletionMessageParam.ofUser(ChatCompletionUserMessageParam.builder().content(msg).build());

        return chatCompletion;

    }

    private @NotNull String createMessageContent(@NotNull Message message) {

        User user = message.getAuthor();

        MessageReference messageReference = message.getMessageReference();
        Message reference = messageReference == null ? null : messageReference.getMessage();

        String userName = user.getEffectiveName();
        String mention = user.getAsMention();
        String content = message.getContentRaw();

        if (user.equals(bot.getSelfUser())) {
            return content;
        }

        else if (reference != null) {
            return String.format("%s (%s): [Ответ на сообщение %s]: %s", userName, mention, reference.getContentRaw(), content);
        }

        else {
            return String.format("%s (%s): %s", userName, mention, content);
        }

    }


}
