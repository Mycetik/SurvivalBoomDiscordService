package net.survivalboom.sbds.modules.chatbot.utils.functions;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.github.sashirestela.openai.common.function.Functional;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.survivalboom.sbds.api.messages.MessageBuilder;
import net.survivalboom.sbds.api.utils.CommonUtils;
import net.survivalboom.sbds.modules.chatbot.ChatBotModule;

import java.time.Duration;
import java.util.Objects;

@JsonClassDescription("Видати попередження користувачу")
public class WarnFunction implements Functional {

    @JsonPropertyDescription("ID каналу у якому сталось порушення")
    @JsonProperty(required = true)
    private long channelId;

    @JsonPropertyDescription("ID користувача")
    @JsonProperty(required = true)
    private long userId;

    @JsonPropertyDescription("Причина для покарання")
    @JsonProperty(required = true)
    private String reason;

    @JsonPropertyDescription("Коментар до покарання")
    @JsonProperty(required = true)
    private String comment;

    @JsonPropertyDescription("Час дії покарання. Максимум 30 днів. Формат: d - дні, h - години, m - хвилини. Приклад: 1d 5h 30m")
    @JsonProperty(required = true)
    private String durationRaw;


    @Override
    public Object execute() {

        var module = ChatBotModule.getInstance();
        var channel = module.getSbds().getBot().getChannelById(GuildMessageChannel.class, channelId);
        Objects.requireNonNull(channel, "channel == null");

        var user = module.getSbds().getBot().retrieveUserById(userId).complete();
        var guild = channel.getGuild();

        Duration duration = CommonUtils.getDurationFromStr(durationRaw);
        Objects.requireNonNull(duration, "duration == null");

        var botUser = module.getChatBot().getBot().getSelfUser();

        var mute = module.getModerationModule().getWarnManager().warn(guild, user, botUser, reason, comment, duration).join();

        var msg = MessageBuilder.create(module.getSbds().getMessages(), "moderation.command.warn.success", guild)
                .withPlaceholders(
                        "{user}", user.getAsMention(),
                        "{reason}", reason,
                        "{comment}", comment,
                        "{duration}", CommonUtils.durationToString(duration),
                        "{moderator}", botUser.getAsMention()
                )
                .build();

        channel.sendMessage(msg).queue();

        module.getLogger().info("[{}] {} added warn {}.", guild.getName(), botUser.getEffectiveName(), mute);

        return true;

    }

}
