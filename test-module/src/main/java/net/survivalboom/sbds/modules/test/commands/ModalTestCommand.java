package net.survivalboom.sbds.modules.test.commands;

import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.modals.Modal;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.slash.SlashCommand;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.interaction.component.FileUploadComponent;
import net.survivalboom.sbds.api.interaction.component.TextDisplayComponent;
import net.survivalboom.sbds.api.interaction.component.TextInputComponent;
import net.survivalboom.sbds.api.interaction.component.dropdown.entity.EntityDropdownTemplate;
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

                .addComponent(
                        TextDisplayComponent.builder()
                                .setName("text")
                                .setText("$[test.modal.text]")
                                .build()
                )

                .addComponent(TextInputComponent.builder()
                        .setName("short")
                        .setStyle(TextInputStyle.SHORT)
                        .setTitle("$[test.modal.input.short.title]")
                        .setDescription("$[test.modal.input.short.placeholder]")
                        .setMinLength(1)
                        .setMaxLength(16)
                        .build()
                )

                .addComponent(TextInputComponent.builder()
                        .setName("long")
                        .setStyle(TextInputStyle.PARAGRAPH)
                        .setTitle("$[test.modal.input.long.title]")
                        .setDescription("$[test.modal.input.long.placeholder]")
                        .setMinLength(10)
                        .setMaxLength(100)
                        .build()
                )

                .addComponent(EntityDropdownTemplate.builder()
                        .setName("role")
                        .setTitle("$[test.modal.select.role]")
                        .setDescription("$[test.modal.select.role.placeholder]")
                        .setTarget(EntitySelectMenu.SelectTarget.ROLE)
                        .setMinCount(1)
                        .setMaxCount(1)
                        .build()
                )

                .addComponent(FileUploadComponent.builder()
                        .setName("uplodad")
                        .setTitle("$[test.modal.upload]")
                        .setRequired(false)
                        .build()
                )
                .build();

        sbds.getModalInteractionManager().registerModal(module, "test", template);

    }

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {

        info.reply("test.modal.msg")
                .buttonCallback("modal", button -> button.replyModal("testmodule:test")
                        .onSuccess(modal -> {

                            Map<String, ModalMapping> values = modal.getValues();

                            String longTxt = modal.getValueNotNull("long").getAsString();
                            String shortTxt = modal.getValueNotNull("short").getAsString();

                            List<Role> roles = modal.getValueNotNull("role").getAsMentions().getRoles();

                            ModalMapping attachmentsMapping = modal.getValue("upload");
                            List<Message.Attachment> attachments = attachmentsMapping != null ? attachmentsMapping.getAsAttachmentList() : null;

                            modal.replyRaw(String.format("""
                            
                            Long: %s
                            Short: %s
                            
                            Values: %s
                            Roles: %s
                            Attachments: %s
                            
                            """, longTxt, shortTxt, values, roles, attachments)).setEphemeral(true).queue();
                        }).queue(), 120000)

                .queue();

    }

}
