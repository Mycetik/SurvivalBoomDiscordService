package net.survivalboom.sbds.modules.test;

import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.misc.TranslationArgument;
import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandArgument;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.slash.SlashCommand;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.database.users.IUserData;
import net.survivalboom.sbds.api.database.users.IUserRepositoryHandler;
import net.survivalboom.sbds.api.translations.ITranslation;
import org.jetbrains.annotations.NotNull;

@Command(name = "translation")
public class TranslationCommand extends CommandBase implements SlashCommand {

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {
        try {

            ITranslation translation = info.arguments().getCastOrNull("translation", ITranslation.class);
            IUserRepositoryHandler userRepositoryHandler = info.sbds().getDatabase().getRepositoryHandler("sbds:users", IUserRepositoryHandler.class);
            IUserData userData = userRepositoryHandler.createUser(info.user().getIdLong());
            userData.translation(translation);
            userData.save();
            info.reply("Translation updated successfully");

        } catch (Exception e) {
            info.reply("Something went wrong when executing command: " + e.getMessage());
        }
    }

    @CommandArgument(name = "translation")
    public Argument<?> translation() {
        return new TranslationArgument();
    }
}
