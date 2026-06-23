package net.survivalboom.sbds.modules.translation.commands;

import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.commands.ArgumentScope;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.discord.UserArgument;
import net.survivalboom.sbds.api.commands.argument.misc.select.StringSelectArgument;
import net.survivalboom.sbds.api.commands.argument.sbds.TranslationArgument;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.base.ArgumentMethod;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.console.ConsoleCommandExecutor;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.commands.slash.SlashCommandExecutor;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.database.users.IUserData;
import net.survivalboom.sbds.api.database.users.IUserDataManager;
import net.survivalboom.sbds.api.translations.ITranslation;
import net.survivalboom.sbds.api.translations.ITranslationManager;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@CommandClass(name = "translation", description = "Manage SBDS translation", translationKey = "translation.command.translation")
public class TranslationCommand extends CommandBase implements SlashCommandExecutor, ConsoleCommandExecutor {

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {

        ITranslation translation = info.arguments().getCast("translation", ITranslation.class).orElse(null);

        IUserDataManager userDataManager = info.sbds().getUserDataManager();
        IUserData userData = userDataManager.get(info.user()).join();

        if (translation == null) {

            ITranslation currentTranslation = userData != null ? userData.getTranslation() : null;

            info.reply("translation.command.translation.show")
                    .withPlaceholders("translation", Objects.requireNonNullElse(currentTranslation, "$[values.none]"))
                    .queue();

            return;

        }

        if (userData == null) {
            userData = userDataManager.obtain(info.user()).join();
        }

        userData.setTranslation(translation);
        userData.save();

        info.reply("translation.command.translation.set")
                .withPlaceholders("translation", translation)
                .queue();

    }

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {

        User user = info.arguments().getCast("user", User.class).orElse(null);
        Objects.requireNonNull(user, "user == null");

        IUserDataManager userDataManager = info.sbds().getUserDataManager();
        IUserData userData = userDataManager.get(user).join();

        ITranslation translation = info.arguments().getCast("translation0", ITranslation.class).orElse(null);
        if (translation == null) {

            ITranslation currentTranslation = userData != null ? userData.getTranslation() : null;

            if (currentTranslation != null) {
                info.logger().info("Current translation for `{}` is `{}`.", user.getEffectiveName(), currentTranslation.getName());
            }

            else {
                info.logger().info("Translation for `{}` is not set.", user.getEffectiveName());
            }

            return;

        }

        if (userData == null) {
            info.logger().info("Creating user `{}` in the database...", user.getEffectiveName());
            userData = userDataManager.obtain(user).join();
        }

        userData.setTranslation(translation);
        userData.save();

        info.logger().info("Successfully set translation for `{}` to `{}`.", user.getEffectiveName(), translation.getName());

    }

    @ArgumentMethod(description = "A translation", scope = ArgumentScope.CONSOLE, required = false)
    public Argument<?> translation0() {
        return new TranslationArgument();
    }

    @ArgumentMethod(description = "A user", index = 1, scope = ArgumentScope.CONSOLE)
    public Argument<?> user() {
        return new UserArgument();
    }

    @ArgumentMethod(description = "A translation", scope = ArgumentScope.SLASH, required = false)
    public Argument<?> translation() {
        return new TranslationArgument();
    }

}
