package net.survivalboom.sbds.core.utils.placeholders.wrappers;

import net.dv8tion.jda.api.entities.Guild;
import net.survivalboom.sbds.api.utils.placeholders.IPlaceholders;
import net.survivalboom.sbds.api.utils.placeholders.Placeholders;
import org.jetbrains.annotations.NotNull;

public class GuildPlaceholder implements IPlaceholders {

    private final Guild guild;


    public GuildPlaceholder(@NotNull Guild guild) {
        this.guild = guild;
    }

    @Override
    public @NotNull Placeholders placeholders() {
        return Placeholders.of(
                " ", guild.getName(),
                "id", guild.getId(),
                "name", guild.getName()
        );
    }

}
