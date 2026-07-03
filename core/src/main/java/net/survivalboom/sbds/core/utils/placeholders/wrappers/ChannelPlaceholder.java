package net.survivalboom.sbds.core.utils.placeholders.wrappers;

import net.dv8tion.jda.api.entities.channel.Channel;
import net.survivalboom.sbds.api.utils.placeholders.IPlaceholders;
import net.survivalboom.sbds.api.utils.placeholders.Placeholders;
import org.jetbrains.annotations.NotNull;

public class ChannelPlaceholder implements IPlaceholders {

    private final Channel channel;

    public ChannelPlaceholder(@NotNull Channel channel) {
        this.channel = channel;
    }

    @Override
    public @NotNull Placeholders placeholders() {
        return Placeholders.of(
                " ", channel.getAsMention(),
                "id", channel.getId(),
                "name", channel.getName(),
                "mention", channel.getAsMention()
        );
    }

}
