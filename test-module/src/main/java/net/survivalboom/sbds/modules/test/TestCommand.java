package net.survivalboom.sbds.modules.test;

import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.primitive.StringArgument;
import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandArgument;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.slash.SlashCommand;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.interaction.modal.IModalInteractionManager;
import net.survivalboom.sbds.api.interaction.modal.ModalTemplate;
import net.survivalboom.sbds.api.modules.ModuleMain;
import org.jetbrains.annotations.NotNull;

@Command(name = "test", description = "Рисует большой жЫрный член.", permission = "testmodule.command.testcommand", defaultPermission = true)
public class TestCommand extends CommandBase implements SlashCommand {

    private final IModalInteractionManager.IRegisteredModal modal;

    public TestCommand(ModuleMain moduleMain) {

        ModalTemplate modal = ModalTemplate.builder()
                .setTitle("Уведіть номер вашої картки.")
                .addInput("number", "Номер картки", "0000 0000 0000 0000", TextInputStyle.SHORT)
                .addInput("expiry", "Термін дії", "3/12/2016", TextInputStyle.SHORT)
                .addInput("cvv2", "CVV2", "678", TextInputStyle.SHORT)
                .build();

        this.modal = moduleMain.getSbds().getModalInteractionManager().registerModal(moduleMain, "test", modal);

    }

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {

        modal.open(info.interaction(), null).thenAccept(modal -> {
            modal.reply(modal.values().toString()).queue();
        });

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
