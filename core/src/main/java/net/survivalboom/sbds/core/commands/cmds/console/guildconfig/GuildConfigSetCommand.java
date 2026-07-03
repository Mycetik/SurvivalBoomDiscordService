package net.survivalboom.sbds.core.commands.cmds.console.guildconfig;

import net.dv8tion.jda.api.entities.Guild;
import net.survivalboom.sbds.api.commands.argument.primitive.StringArgument;
import net.survivalboom.sbds.api.commands.argument.primitive.StringToObjectArgument;
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

@CommandClass(name = "set", description = "Set an option value in the guild configuration")
public class GuildConfigSetCommand extends CommandBase implements ConsoleCommandExecutor {

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) throws Throwable {

        Guild guild = info.arguments().getCast("guild", Guild.class).orElseThrow();
        IGuildConfigTemplate template = info.arguments().getCast("cfg", IGuildConfigTemplate.class).orElseThrow();
        String path = info.arguments().getCast("path", String.class).orElseThrow();
        Object value = info.arguments().getCast("value", Object.class).orElse(null);

        if (value instanceof String string && string.equals("null")) {
            value = null;
        }

        String keyStr = template.getKey();

        info.logger().info("Retrieving guild config `{}` of `{}`...", keyStr, guild.getName());

        IGuildConfig config = template.obtainConfig(guild);

        GuildConfigField field = config.getTemplate().getField(path);
        if (field == null) {
            info.logger().error("There is no config field `{}:{}`.", keyStr, path);
            return;
        }

        if (value != null && !field.type().isAssignableFrom(value.getClass())) {
            info.logger().error("Cannot cast `{}` to `{}` for field `{}:{}`.", value.getClass(), field.type(), keyStr, path);
            return;
        }

        config.set(path, value);

        info.logger().info("Set `{}:{}` to `{}`", keyStr, path, value);

    }

    @ArgumentMethod(index = 1)
    public GuildConfigArgument cfg() {
        return new GuildConfigArgument();
    }

    @ArgumentMethod(index = 2)
    public StringArgument path() {
        return new StringArgument();
    }

    @ArgumentMethod(index = 3)
    public StringToObjectArgument value() {
        return new StringToObjectArgument();
    }

}
