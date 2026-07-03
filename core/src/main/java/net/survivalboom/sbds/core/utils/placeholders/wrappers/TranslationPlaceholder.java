package net.survivalboom.sbds.core.utils.placeholders.wrappers;

import net.survivalboom.sbds.api.translations.ITranslation;
import net.survivalboom.sbds.api.utils.placeholders.IPlaceholders;
import net.survivalboom.sbds.api.utils.placeholders.Placeholders;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class TranslationPlaceholder implements IPlaceholders {

    private final ITranslation translation;

    public TranslationPlaceholder(@NotNull ITranslation translation) {
        this.translation = translation;
    }


    @Override
    public @NotNull Placeholders placeholders() {
        return Placeholders.of(
                " ", Objects.requireNonNullElse(translation.getDisplayName(), translation.getName()),
                "name", Objects.requireNonNullElse(translation.getDisplayName(), translation.getName()),
                "key", translation.getName(),
                "locale", translation.getDiscordLocale(),
                "icon", translation.getIconEmoji()
        );
    }
}
