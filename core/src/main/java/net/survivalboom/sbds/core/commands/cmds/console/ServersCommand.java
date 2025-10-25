package net.survivalboom.sbds.core.commands.cmds.console;

import net.dv8tion.jda.api.entities.Guild;
import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.console.ConsoleCommand;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

@Command(name = "servers", description = "Shows a list of servers where bot was invited", usage = "servers")
public class ServersCommand extends CommandBase implements ConsoleCommand {

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) throws Throwable {

        List<Guild> guilds = info.sbds().getBot().getGuilds();
        if (guilds.isEmpty()) {
            info.logger().info("Bot is not currently on any server.");
            return;
        }

        info.logger().info("--- Server List ---");
        for (Guild guild : guilds) {

            var owner = guild.getOwner();
            Objects.requireNonNull(owner, "owner == null; " + guild.getId());

            info.logger().info("{} ({}) - {} members. Owner: {} ({}). Joined at: {}", guild.getName(), guild.getId(), guild.getMemberCount(), owner.getEffectiveName(), owner.getId(), guild.getSelfMember().getTimeJoined());

        }
        info.logger().info("--- --- ---- --- ---");

    }

}
