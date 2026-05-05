package net.survivalboom.sbds.api.interaction;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.interactions.Interaction;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.messages.IMessages;
import org.jetbrains.annotations.NotNull;

public interface InteractionHolder<interaction extends Interaction> {

    @NotNull ISBDS sbds();

    @NotNull IMessages messages();


    @NotNull interaction interaction();

    default Guild guild() {
        return interaction().getGuild();
    }

    default @NotNull User user() {
        return interaction().getUser();
    }

    default Member member() {
        return interaction().getMember();
    }

    default Channel channel() {
        return interaction().getChannel();
    }

}
