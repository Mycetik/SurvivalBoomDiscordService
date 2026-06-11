package net.survivalboom.sbds.modules.guildconfig.commands;

import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.slash.SlashCommandExecutor;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.interaction.InteractionHolder;
import net.survivalboom.sbds.api.messages.parsers.TextParser;
import net.survivalboom.sbds.api.messages.template.EmbedMessageTemplate;
import net.survivalboom.sbds.api.messages.template.IMessageTemplate;
import net.survivalboom.sbds.api.messages.template.TextMessageTemplate;
import org.jetbrains.annotations.NotNull;

@CommandClass(name = "list", description = "Shows all of the available options to configure for this guild", translationKey = "guildconfig.command.config.list", permission = "guildconfig.command.config.list", defaultPermission = true)
public class ConfigListCommand extends CommandBase implements SlashCommandExecutor {

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {

        EmbedMessageTemplate rootTemplate = getEmbed("guildconfig.command.config.list.root", info);

        var configs = info.sbds().getGuildConfigManager().getGuildConfigs(info.guild());

        TextParser parser = TextParser.builder()
                .addPlaceholders(
                        "configs", configs.size(),
                        "modules", info.sbds().getModuleManager().getModules().size()
                )
                .build();

        var msgData = rootTemplate.createMessageData(parser.createStringParser(info.messages()), null);

        for (var config : configs) {

            var module = config.getRegistration().module();

            var builder = TextParser.builder()
                    .addPlaceholders("module", config.getRegistration().module());

            if (module != null) {
                builder.addPlaceholder("module", module);
            }

            else {
                builder.addPlaceholders(
                        "module.name", "SBDS",
                        "module.version", info.sbds().getVersion()
                );
            }

            TextParser parser0 = builder.build();

            var msg = getEmbed("guildconfig.command.config.list.config-root", info).getEmbeds().getFirst().build(parser0.createStringParser(info.messages()));

            for (var entry : config.getValues().join().entrySet()) {

                var field = entry.getKey();
                var value = entry.getValue();

                TextParser parser1 = TextParser.builder()
                        .addPlaceholders(
                                "option.key", field.key(),
                                "option.translated", field.translationKey() != null ? info.sbds().getMessages().parseTranslations("$[" + field.translationKey() + "]", info.user()) : null,
                                "option.type", field.type(),
                                "option.value", value,
                                "option.default", field.defaultValue()
                        )
                        .build();

                var fieldMsgName = getStrMsg("guildconfig.command.config.list.config-field-format.name", info, parser1);
                var fieldMsgValue = getStrMsg("guildconfig.command.config.list.config-field-format.value", info, parser1);

                msg.addField(fieldMsgName, fieldMsgValue, true);

            }

            msgData.addEmbeds(msg.build());

        }

        info.replyRaw(msgData.build()).queue();

    }

    private @NotNull EmbedMessageTemplate getEmbed(@NotNull String key, @NotNull InteractionHolder info) {

        IMessageTemplate template = info.messages().getMessage(key, info, true);
        if (!(template instanceof EmbedMessageTemplate embedMessageTemplate)) {
            return EmbedMessageTemplate.builder().addEmbed(b -> b.setBody(key, null, null)).build();
        }

        return embedMessageTemplate;

    }

    private @NotNull String getStrMsg(@NotNull String key, @NotNull InteractionHolder info, @NotNull TextParser parser) {

        IMessageTemplate template = info.messages().getMessage(key, info, true);
        if (!(template instanceof TextMessageTemplate template1)) {
            return key;
        }

        return info.messages().parse(template1.getContent(), parser);

    }

}
