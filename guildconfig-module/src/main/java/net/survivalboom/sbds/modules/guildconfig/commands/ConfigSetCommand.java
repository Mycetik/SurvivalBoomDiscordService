package net.survivalboom.sbds.modules.guildconfig.commands;

import net.survivalboom.sbds.api.commands.argument.discord.UserArgument;
import net.survivalboom.sbds.api.commands.argument.discord.channel.TextChannelArgument;
import net.survivalboom.sbds.api.commands.argument.discord.channel.VoiceChannelArgument;
import net.survivalboom.sbds.api.commands.argument.primitive.BooleanArgument;
import net.survivalboom.sbds.api.commands.argument.primitive.IntegerArgument;
import net.survivalboom.sbds.api.commands.argument.primitive.StringArgument;
import net.survivalboom.sbds.api.commands.argument.sbds.GuildConfigArgument;
import net.survivalboom.sbds.api.commands.base.ArgumentMethod;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.slash.SlashCommandExecutor;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.database.guildconfig.GuildConfigField;
import net.survivalboom.sbds.api.database.guildconfig.IGuildConfig;
import net.survivalboom.sbds.api.database.guildconfig.IGuildConfigTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@CommandClass(name = "set", description = "Set an option value fro this guild", translationKey = "guildconfig.command.config.set", permission = "guilconfig.command.config.set")
public class ConfigSetCommand extends CommandBase implements SlashCommandExecutor {

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {

        IGuildConfigTemplate template = info.arguments().getCast("config", IGuildConfigTemplate.class).orElseThrow();
        String key = info.arguments().getCast("key", String.class).orElseThrow();

        var args = new ArrayList<>(info.arguments().values());
        args.remove(template);
        args.remove(key);

        if (args.isEmpty()) {
            info.reply("guildconfig.command.config.set.empty").queue();
            return;
        }

        IGuildConfig config = template.obtainConfig(info.guild());

        GuildConfigField field = config.getField(key);
        if (field == null) {
            info.reply("guildconfig.command.config.set.invalid-key")
                    .withPlaceholders("option", template.getKey() + ":" + key)
                    .queue();
            return;
        }

        Object value = args.getFirst();

        if (value instanceof String string && string.equals("null")) {
            value = null;
        }

        if (field.isValueAllowed(value)) {
            info.reply("guildconfig.command.config.set.invalid-value")
                    .withPlaceholders("value", value, "option", template.getKey() + ":" + key)
                    .queue();
            return;
        }

        config.set(key, value);

        info.reply("guildconfig.command.config.set.success")
                .withPlaceholders(
                        "option.key", field.key(),
                        "option.translated", field.translationKey() != null ? info.sbds().getMessages().parseTranslations("$[" + field.translationKey() + "]", info.user()) : null,
                        "option.type", field.type(),
                        "option.value", value,
                        "option.default", field.defaultValue(),
                        "value", value
                )
                .queue();

    }

    //
    // ARGUMENTS
    //

    @ArgumentMethod
    public GuildConfigArgument config() {
        return new GuildConfigArgument();
    }

    @ArgumentMethod(index = 1)
    public StringArgument key() {
        return new StringArgument();
    }

    @ArgumentMethod(index = 2, required = false)
    public TextChannelArgument textchannel() {
        return new TextChannelArgument();
    }

    @ArgumentMethod(index = 2, required = false)
    public VoiceChannelArgument voicechannel() {
        return new VoiceChannelArgument();
    }

    @ArgumentMethod(index = 2, required = false)
    public UserArgument user() {
        return new UserArgument();
    }

    @ArgumentMethod(index = 2, required = false)
    public StringArgument string() {
        return new StringArgument();
    }

    @ArgumentMethod(index = 2, required = false)
    public IntegerArgument integer() {
        return new IntegerArgument();
    }

    @ArgumentMethod(index = 2, required = false)
    public BooleanArgument bool() {
        return new BooleanArgument();
    }

}
