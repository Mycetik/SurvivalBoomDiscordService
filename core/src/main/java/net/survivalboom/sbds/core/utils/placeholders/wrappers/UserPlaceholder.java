package net.survivalboom.sbds.core.utils.placeholders.wrappers;

import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.api.utils.placeholders.IPlaceholders;
import net.survivalboom.sbds.api.utils.placeholders.Placeholders;
import org.jetbrains.annotations.NotNull;

public class UserPlaceholder implements IPlaceholders {

    private final User user;


    public UserPlaceholder(@NotNull User user) {
        this.user = user;
    }

    @Override
    public @NotNull Placeholders placeholders() {
        return Placeholders.of(
                "id", user.getId(),
                "username", user.getName(),
                "displayName", user.getGlobalName(),
                "name", user.getEffectiveName(),
                "avatar", user.getAvatarUrl(),
                "mention", user.getAsMention()
        );
    }

}
