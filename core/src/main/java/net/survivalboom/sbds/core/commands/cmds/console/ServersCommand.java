package net.survivalboom.sbds.core.commands.cmds.console;

import net.dv8tion.jda.api.entities.Guild;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.console.ConsoleCommandExecutor;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@CommandClass(name = "servers", description = "Shows a list of servers where bot was invited", usage = "servers")
public class ServersCommand extends CommandBase implements ConsoleCommandExecutor {

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {

        List<Guild> guilds = info.sbds().getBot().getGuilds();
        if (guilds.isEmpty()) {
            info.logger().info("Bot is not currently on any server.");
            return;
        }

        info.logger().info("--- Server List ---");
        for (Guild guild : guilds) {

            var owner = guild.retrieveOwner().complete();

            info.logger().info("{} ({}) - {} members. Owner: {} ({}). Joined at: {}", guild.getName(), guild.getId(), guild.getMemberCount(), owner.getEffectiveName(), owner.getId(), guild.getSelfMember().getTimeJoined());

        }
        info.logger().info("--- --- ---- --- ---");

    }

}
