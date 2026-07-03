package net.survivalboom.sbds.core.commands.cmds.console.guildconfig;

import net.dv8tion.jda.api.entities.Guild;
import net.survivalboom.sbds.api.commands.argument.sbds.GuildConfigArgument;
import net.survivalboom.sbds.api.commands.base.ArgumentMethod;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.console.ConsoleCommandExecutor;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.database.guildconfig.GuildConfigField;
import net.survivalboom.sbds.api.database.guildconfig.IGuildConfig;
import net.survivalboom.sbds.api.database.guildconfig.IGuildConfigTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CommandClass(name = "read", description = "Read a guild configuration")
public class GuildConfigReadCommand extends CommandBase implements ConsoleCommandExecutor {

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) throws Throwable {

        Guild guild = info.arguments().getCast("guild", Guild.class).orElseThrow();
        IGuildConfigTemplate template = info.arguments().getCast("cfg", IGuildConfigTemplate.class).orElse(null);

        if (template != null) {

            String keyStr = template.getKey();

            IGuildConfig config = template.obtainConfig(guild);

            info.logger().info("Retrieving guild config `{}` of `{}`...", keyStr, guild.getName());

            var map = config.getValues().join();

            info.logger().info("--- --- < Guild Config > --- ---");
            info.logger().info("Guild: {}", guild.getName());
            info.logger().info("Key: {}", keyStr);
            info.logger().info(" ");

            for (var entry : map.entrySet()) {

                GuildConfigField field = entry.getKey();
                Object value = entry.getValue();
                Object defaultValue = field.defaultValue();

                info.logger().info("> {} -> {} (Default: {}, Internal: {})", field.key(), value, defaultValue, field.internal());

            }

            info.logger().info("---- ----- ----  ---- ----- ----");

        }

        else {

            info.logger().info("Retrieving guild configs for `{}`...", guild.getName());
            List<IGuildConfig> configs = info.sbds().getGuildConfigManager().getGuildConfigs(guild);
            if (configs.isEmpty()) {
                info.logger().error("No configs found.");
                return;
            }

            Map<String, Map<GuildConfigField, Object>> map = new HashMap<>();
            for (var config : configs) {
                var map2 = config.getValues().join();
                map.put(config.getKey(), map2);
            }

            info.logger().info("--- --- < Guild Config > --- ---");
            info.logger().info("Guild: {}", guild.getName());
            info.logger().info(" ");

            for (var entry : map.entrySet()) {

                info.logger().info("> {}", entry.getKey());

                for (var entry2 : entry.getValue().entrySet()) {

                    GuildConfigField field = entry2.getKey();
                    Object value = entry2.getValue();
                    Object defaultValue = field.defaultValue();

                    info.logger().info("* {} -> {} (Default: {}, Internal: {})", field.key(), value, defaultValue, field.internal());

                }

                info.logger().info(" ");

            }

            info.logger().info("---- ----- ----  ---- ----- ----");

        }

    }

    @ArgumentMethod(index = 1, required = false)
    public GuildConfigArgument cfg() {
        return new GuildConfigArgument();
    }

}
