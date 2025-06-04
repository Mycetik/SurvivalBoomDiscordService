package net.survivalboom.sbds.modules.github.commands;

import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.primitive.IntegerArgument;
import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandArgument;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.slash.SlashCommand;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.utils.Placeholders;
import net.survivalboom.sbds.modules.github.storage.WebhookData;
import net.survivalboom.sbds.modules.github.storage.WebhookRepositoryHandler;
import org.jetbrains.annotations.NotNull;

@Command(name = "remove", description = "Remove a github webhook.", permission = "github.command.remove")
public class RemoveWebhookCommand extends CommandBase implements SlashCommand {

    private final WebhookRepositoryHandler repository;

    public RemoveWebhookCommand(@NotNull WebhookRepositoryHandler  repository) {
        this.repository = repository;
    }

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {

        int id = info.arguments().getCastOrDefault("id", Integer.class, -1);
        WebhookData webhook = repository.getWebhook(id);
        if (webhook == null || !webhook.channel(info.sbds().getBot()).getGuild().equals(info.guild())) {
            info.reply("github.command.remove.not-found").withPlaceholders(Placeholders.of("{ID}", id)).queue();
            return;
        }

        repository.deleteWebhook(webhook.id());

        info.reply("github.command.remove.success").withPlaceholders(Placeholders.of("{ID}", id, "{CHANNEL}", "<#" + webhook.channelId() + ">")).queue();

    }


    @CommandArgument(name = "id", description = "The id of the webhook.")
    public Argument<?> id() {
        return new IntegerArgument();
    }


}
