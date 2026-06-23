package net.survivalboom.sbds.core.utils.placeholders.wrappers;

import net.survivalboom.sbds.api.database.guildconfig.GuildConfigField;
import net.survivalboom.sbds.api.utils.placeholders.IPlaceholders;
import net.survivalboom.sbds.api.utils.placeholders.Placeholders;
import org.jetbrains.annotations.NotNull;

public class GuildConfigFieldPlaceholder implements IPlaceholders {

    private final GuildConfigField field;

    public GuildConfigFieldPlaceholder(GuildConfigField field) {
        this.field = field;
    }

    @Override
    public @NotNull Placeholders placeholders() {
        return Placeholders.of(
                "", field.key(),
                "key", field.key(),
                "type", field.type(),
                "default", field.defaultValue()
        );
    }

}
