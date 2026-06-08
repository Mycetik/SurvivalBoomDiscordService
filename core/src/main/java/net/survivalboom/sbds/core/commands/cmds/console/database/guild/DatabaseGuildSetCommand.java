package net.survivalboom.sbds.core.commands.cmds.console.database.guild;

import net.dv8tion.jda.api.entities.Guild;
import net.survivalboom.sbds.api.commands.argument.primitive.StringArgument;
import net.survivalboom.sbds.api.commands.argument.primitive.StringToObjectArgument;
import net.survivalboom.sbds.api.commands.argument.sbds.NamespacedKeyArgument;
import net.survivalboom.sbds.api.commands.base.ArgumentMethod;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.console.ConsoleCommandExecutor;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.database.guilds.IGuildData;
import net.survivalboom.sbds.api.database.guilds.IGuildDataManager;
import net.survivalboom.sbds.api.utils.CommonUtils;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.api.utils.container.INamespacedDataContainer;
import org.jetbrains.annotations.NotNull;

@CommandClass(name = "set", description = "Set a value inside guild data in the database")
public class DatabaseGuildSetCommand extends CommandBase implements ConsoleCommandExecutor {

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) throws Throwable {

        Guild guild = info.arguments().getCast("guild", Guild.class).orElseThrow();
        NamespacedKey key = info.arguments().getCast("key", NamespacedKey.class).orElseThrow();
        String path = info.arguments().getCast("path", String.class).orElseThrow();
        Object data = info.arguments().getCast("data", Object.class).orElseThrow();

        IGuildDataManager manager = info.sbds().getGuildDataManager();

        info.logger().info("Retrieving guild data of `{}`...", guild.getName());

        IGuildData guildData = manager.obtain(guild).join();
        INamespacedDataContainer container = guildData.container();

        info.logger().info("Applying changes...");

        String[] path0 = CommonUtils.splitString(path, "\\.");
        container.obtainNode(key).node((Object[]) path0).set(data);

        guildData.save();

        info.logger().info("Set `{}:{}` to `{}`", key, path, data);

    }

    @ArgumentMethod(index = 1)
    public NamespacedKeyArgument key() {
        return new NamespacedKeyArgument();
    }

    @ArgumentMethod(index = 2)
    public StringArgument path() {
        return new StringArgument();
    }

    @ArgumentMethod(index = 3)
    public StringToObjectArgument data() {
        return new StringToObjectArgument();
    }

}
