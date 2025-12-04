package net.survivalboom.sbds.modules.test.commands;

import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.Message;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.slash.SlashCommand;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.interaction.modal.ModalTemplate;
import net.survivalboom.sbds.api.modules.IModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Command(name = "modal", description = "Modal API test.")
public class ModalTestCommand extends CommandBase implements SlashCommand {

    @Override
    protected void init(@NotNull ISBDS sbds, @Nullable IModule module) {

        Objects.requireNonNull(module);

        ModalTemplate template = ModalTemplate.builder()
                .setTitle("$[test.modal.title]")
                .addTextDisplay("$[test.modal.text]")
                .addInput("short", "$[test.modal.input.short.title]", "$[test.modal.input.short.placeholder]", TextInputStyle.SHORT, 1, 100, true)
                .addInput("long", "$[test.modal.input.long.title]", "$[test.modal.input.long.placeholder]", TextInputStyle.PARAGRAPH, 10, 1000, false)
                .addEntitySelect("role-select", "$[test.modal.select.role]", EntitySelectMenu.SelectTarget.ROLE, 1, 1, "$[test.modal.select.role.placeholder]")
                .addAttachmentUpload("upload", "$[test.modal.upload]")
                .build();

        sbds.getModalInteractionManager().registerModal(module, "test", template);

    }

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {

        info.reply("test.modal.msg")
                .buttonCallback("modal", button -> button.replyModal("testmodule:test")
                        .onSuccess(modal -> {
                            Map<String, String> values = modal.values();
                            List<String> roles = modal.valueList("role-select");
                            List<Message.Attachment> attachments = modal.attachments("upload");
                            modal.replyRaw("Values: " + values + "\nRoles: " + roles + "\nAttachments: " + attachments)
                                    .setEphemeral(true)
                                    .queue();
                        }).queue(), 120000)
                .queue();

    }

}
