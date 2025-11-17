package net.survivalboom.sbds.modules.chatbot.utils.functions;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.github.sashirestela.openai.common.function.Functional;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.survivalboom.sbds.modules.chatbot.ChatBotModule;

import java.util.Objects;

@JsonClassDescription("Відповісти на повідомлення користувача")
public class ReplyMessageFunction implements Functional {

    @JsonPropertyDescription("ID каналу")
    @JsonProperty(required = true)
    public long channelId;

    @JsonPropertyDescription("ID повідомлення")
    @JsonProperty(required = true)
    public long messageId;

    @JsonPropertyDescription("Вміст відповіді")
    @JsonProperty(required = true)
    public String content;

    @Override
    public Object execute() {

        var module = ChatBotModule.getInstance();
        var channel = module.getChatBot().getBot().getChannelById(GuildMessageChannel.class, channelId);
        Objects.requireNonNull(channel, "channel == null");

        var msg = channel.retrieveMessageById(messageId).complete();

        msg.reply(content).queue();

        return true;

    }

}
