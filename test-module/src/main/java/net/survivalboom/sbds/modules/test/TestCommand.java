package net.survivalboom.sbds.modules.test;

import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandArgument;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.misc.TranslationArgument;
import net.survivalboom.sbds.api.commands.slash.SlashCommand;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.database.users.IUserData;
import net.survivalboom.sbds.api.database.users.IUserRepositoryHandler;
import net.survivalboom.sbds.api.translations.ITranslation;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Command(name = "test", description = "Рисует большой жЫрный член.")
public class TestCommand extends CommandBase implements SlashCommand {

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {

        NamespacedKey key = NamespacedKey.fromString("sbds:users");
        IUserRepositoryHandler repository = info.sbds().getDatabase().getRepositoryHandler(key, IUserRepositoryHandler.class);

        Objects.requireNonNull(repository);

        IUserData userData = repository.createUser(info.user());

        ITranslation translation = info.arguments().get("translation", ITranslation.class);
        if (translation == null) {
            ITranslation current = userData.translation();
            String str = current != null ? current.getName() : "null";
            info.interaction().reply("Ваш поточний переклад: " + str).queue();
        }

        else {
            userData.translation(translation);
            info.interaction().reply("Переклад встановлено! " + translation.getName()).queue();
        }

    }

    @CommandArgument(name = "translation", description = "A translation", required = false)
    public Argument<?> translation() {
        return new TranslationArgument();
    }

}
