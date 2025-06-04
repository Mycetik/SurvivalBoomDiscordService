package net.survivalboom.sbds.modules.github.commands;

import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.slash.SlashCommand;
import net.survivalboom.sbds.modules.github.storage.WebhookRepositoryHandler;
import org.jetbrains.annotations.NotNull;

@Command(name = "github")
public class GithubCommand extends CommandBase implements SlashCommand {

    public GithubCommand(@NotNull WebhookRepositoryHandler repository) {
        addSubCommand(new CreateWebhookCommand(repository));
        addSubCommand(new RemoveWebhookCommand(repository));
        addSubCommand(new ListWebhooksCommand(repository));
    }

}
