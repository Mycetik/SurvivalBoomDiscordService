package net.survivalboom.sbds.modules.test;

import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.primitive.StringArgument;
import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandArgument;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.slash.SlashCommand;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.database.guilds.IGuildData;
import net.survivalboom.sbds.api.database.guilds.IGuildRepositoryHandler;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.api.utils.Placeholders;
import net.survivalboom.sbds.api.utils.TypeMap;
import org.jetbrains.annotations.NotNull;

@Command(name = "test", description = "Рисует большой жЫрный член.", permission = "testmodule.command.testcommand", defaultPermission = false)
public class TestCommand extends CommandBase implements SlashCommand {

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {

        IGuildRepositoryHandler repository = info.sbds().getDatabase().getRepositoryHandler("sbds:guilds", IGuildRepositoryHandler.class);
        IGuildData guildData = repository.createGuildData(info.guild());

        TypeMap map = guildData.container().getOrCreate(NamespacedKey.fromModule(info, "test"));

        String key = info.arguments().get("key", String.class);
        String value = info.arguments().get("value", String.class);

        if (value == null || key == null) {
            info.reply(map + " | " + guildData.container()).queue();
            return;
        }

        map.put(key, value);
        guildData.save();

        info.reply("Set `{A}` to `{B}`.", Placeholders.of("{A}", key, "{B}", value)).queue();

    }

    @CommandArgument(name = "key", required = false)
    public Argument<?> key() {
        return new StringArgument();
    }

    @CommandArgument(name = "value", required = false)
    public Argument<?> value() {
        return new StringArgument();
    }

}
