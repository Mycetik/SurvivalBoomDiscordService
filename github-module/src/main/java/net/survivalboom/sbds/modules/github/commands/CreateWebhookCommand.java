package net.survivalboom.sbds.modules.github.commands;

import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.discord.channel.TextChannelArgument;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.base.ArgumentMethod;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.slash.SlashCommandExecutor;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.utils.placeholders.Placeholders;
import net.survivalboom.sbds.modules.github.storage.WebhookData;
import net.survivalboom.sbds.modules.github.storage.WebhookRepositoryHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@CommandClass(name = "add", description = "Create a github webhook.", permission = "github.command.create")
public class CreateWebhookCommand extends CommandBase implements SlashCommandExecutor {

    private final WebhookRepositoryHandler repository;

    public CreateWebhookCommand(@NotNull WebhookRepositoryHandler  repository) {
        this.repository = repository;
    }

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {

        GuildMessageChannel channel = info.arguments().get("channel", GuildMessageChannel.class);

        Objects.requireNonNull(channel);

        WebhookData webhook = repository.createWebhook(channel.getIdLong());

        info.reply("github.command.create").withPlaceholders(Placeholders.of("{ID}", webhook.id(), "{CHANNEL}", channel.getAsMention())).queue();

    }


    @ArgumentMethod(name = "channel")
    public Argument<?> channel() {
        return new TextChannelArgument();
    }

}
