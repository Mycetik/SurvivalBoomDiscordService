package net.survivalboom.sbds.modules.test.commands;

import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.slash.SlashCommandExecutor;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.messages.components.templates.LabelTemplate;
import net.survivalboom.sbds.api.messages.components.templates.TextInputTemplate;
import org.jetbrains.annotations.NotNull;

@CommandClass(name = "test-modal", description = "Test modal functionality in SBDS", deferReply = false)
public class TestModalCommand extends CommandBase implements SlashCommandExecutor {

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {

        info.replyModal(builder ->
            builder
                .setTitle("$[testmodule.command.test-modal.modal.title]")
                .addComponent(new LabelTemplate(
                        "$[testmodule.command.test-modal.modal.label]",
                        null,
                        1,
                        TextInputTemplate.builder()
                                .setName("text")
                                .setStyle(TextInputStyle.PARAGRAPH)
                                .setPlaceholder("$[testmodule.command.test-modal.modal.text]")
                                .build()
                ))
        ).onSuccess(ctx ->
            ctx.reply("testmodule.command.test-modal.success")
                    .withPlaceholders("text", ctx.field("text").orElseThrow().getAsString())
                    .queue()
        )
        .withTimeout(30000)
        .queue();

    }

}
