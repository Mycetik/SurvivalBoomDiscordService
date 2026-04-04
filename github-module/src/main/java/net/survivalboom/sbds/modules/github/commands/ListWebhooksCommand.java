package net.survivalboom.sbds.modules.github.commands;

import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.discord.channel.TextChannelArgument;
import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandArgument;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.slash.SlashCommand;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.translations.IMessage;
import net.survivalboom.sbds.api.messages.IMessages;
import net.survivalboom.sbds.api.utils.Placeholders;
import net.survivalboom.sbds.modules.github.storage.WebhookData;
import net.survivalboom.sbds.modules.github.storage.WebhookRepositoryHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

@Command(name = "list", description = "Get a list of created github webhooks in the channel.", permission = "github.command.list")
public class ListWebhooksCommand extends CommandBase implements SlashCommand {

    private final WebhookRepositoryHandler repository;

    public ListWebhooksCommand(@NotNull WebhookRepositoryHandler  repository) {
        this.repository = repository;
    }

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {

        GuildMessageChannel channel = info.arguments().get("channel", GuildMessageChannel.class);
        Objects.requireNonNull(channel);

        List<WebhookData> webhooks = repository.getWebhooksInChannel(channel.getIdLong());
        if (webhooks.isEmpty()) {
            info.reply("github.command.list.no-results").withPlaceholders(Placeholders.of("{CHANNEL}", channel.getAsMention())).queue();
            return;
        }

        StringBuilder builder = new StringBuilder();
        for (WebhookData webhook : webhooks) {

            Placeholders placeholders = Placeholders.of("{ID}", webhook.id());
            builder.append(webhookMessage(info.messages(), placeholders, channel)).append("\n");

        }

        info.reply("github.command.list.success").withPlaceholders(Placeholders.of("{CHANNEL}", channel.getAsMention(), "{WEBHOOKS}", builder)).queue();

    }


    @CommandArgument(name = "channel")
    public Argument<?> channel() {
        return new TextChannelArgument();
    }


    private @NotNull String webhookMessage(@NotNull IMessages messages, @NotNull Placeholders placeholders, @NotNull GuildMessageChannel channel) {
        IMessage message = messages.getMessage("github.command.list.webhook-format", channel.getGuild(), true);
        if (message == null) return "";
        return message.buildString(placeholders);
    }

}
