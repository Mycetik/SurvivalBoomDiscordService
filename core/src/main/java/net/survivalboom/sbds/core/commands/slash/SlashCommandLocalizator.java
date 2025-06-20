package net.survivalboom.sbds.core.commands.slash;

import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import net.survivalboom.sbds.api.commands.ArgumentScope;
import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.CommandArgument;
import net.survivalboom.sbds.api.translations.CommandTranslationScope;
import net.survivalboom.sbds.core.messages.Message;
import net.survivalboom.sbds.core.translations.Translation;
import net.survivalboom.sbds.core.translations.TranslationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SlashCommandLocalizator {

    private static final Logger log = LoggerFactory.getLogger(SlashCommandLocalizator.class.getName());

    private static final Pattern OPTION_PATTERN = Pattern.compile("^[a-zа-яіїєґ]+$");

    private final TranslationManager translationManager;


    public SlashCommandLocalizator(@NotNull TranslationManager translationManager) {
        this.translationManager = translationManager;
    }


    // Chat GPT рулить!
    private @Nullable TranslationResult getLocalizationKey(@NotNull Command rootCommand, @NotNull String request) {

        String[] parts = request.split("\\.");
        if (parts.length < 2) return null;

        Command current = rootCommand;
        int index = 0;

        // Пройдём по цепочке команд и сабкоманд
        while (index < parts.length) {
            String part = parts[index];

            // Дошли до options — значит, дальше аргументы
            if (part.equals("options")) break;

            if (!part.equals(current.getName())) {
                Command next = current.subcommands().stream()
                        .filter(c -> c.getName().equals(part))
                        .findAny().orElse(null);
                if (next == null) break;

                current = next;
            }

            index++;
        }

        // Если нет options после команд — это description команды
        if (index == parts.length - 1 && parts[index].equals("description")) {
            return current.translationKey() != null ? of(current.translationKey() + ".description", request) : null;
        }

        // Обработка options
        if (index < parts.length && parts[index].equals("options")) {

            index++; // переходим к аргументу
            if (index >= parts.length) return null;

            String argName = parts[index++];
            CommandArgument argument = current.arguments().stream()
                    .filter(a -> a.name().equals(argName))
                    .findAny().orElse(null);

            if (argument == null || argument.translationKey() == null) return null;

            // Проверка на choices
            if (index < parts.length && parts[index].equals("choices")) {
                index++;
                if (index >= parts.length) return null;

                String choiceName = parts[index];
                return of(argument.translationKey() + ".choices." + choiceName, request);
            }

            // Обычный name/description аргумента
            if (index < parts.length) {
                String type = parts[index];
                return of(argument.translationKey() + "." + type, request);
            }

            return null;

        }

        return null;
    }


    public @NotNull LocalizationFunction createLocalizationFunction(@NotNull Command command) {

        return key -> {

            Map<DiscordLocale, String> map = new HashMap<>();

            TranslationResult result = getLocalizationKey(command, key);
            if (result != null) {

                String translationKey = result.key();

                for (Translation translation : translationManager.getTranslations0()) {

                    DiscordLocale locale = translation.discordLocale();
                    Message message = translation.getMessage(translationKey);

                    if (message == null) {
                        log.warn("No translation found for `{}` in `{}` ({}).", translationKey, translation.getName(), key);
                        continue;
                    }

                    String str = message.buildString(null);
                    if (result.scope == CommandTranslationScope.OPTION_NAME && !checkOptionRegex(str)) {
                        log.error("Invalid option name `{}` ({}) from `{}`. Must match the regex `^[a-zа-я]+$` and be between 1 and 32.", str, translationKey, translation.getName());
                        continue;
                    }

                    if (str.isEmpty() || str.length() > 100) {
                        log.error("Invalid translation `{}` ({}) from `{}`. Must be between 1 and 100.", str, translationKey, translation.getName());
                        continue;
                    }

                    map.put(locale, str);

                }

            }

            return map;

        };

    }

    private static TranslationResult of(@NotNull String sbdsTranslationKey, @NotNull String key) {

        boolean name = key.endsWith("name");
        boolean choice = key.contains("choices");
        boolean option = !choice && key.contains("options");

        CommandTranslationScope scope;
        if (choice) {
            scope = CommandTranslationScope.CHOICE_NAME;
        }

        else if (option) {
            scope = name ? CommandTranslationScope.OPTION_NAME : CommandTranslationScope.OPTION_DESCRIPTION;
        }

        else {
            scope = name ? CommandTranslationScope.COMMAND_NAME : CommandTranslationScope.COMMAND_DESCRIPTION;
        }

        return new TranslationResult(sbdsTranslationKey, scope);

    }

    private static boolean checkOptionRegex(String str) {
        return OPTION_PATTERN.matcher(str).matches() && !str.isEmpty() && str.length() <= 31;
    }

    record TranslationResult(@NotNull String key, @NotNull CommandTranslationScope scope) {}

}
