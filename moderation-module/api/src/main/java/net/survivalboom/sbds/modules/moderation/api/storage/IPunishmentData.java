package net.survivalboom.sbds.modules.moderation.api.storage;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;
import net.survivalboom.sbds.modules.moderation.api.moderation.PunishmentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;

public interface IPunishmentData {


    long getId();

    @NotNull PunishmentType getType();


    @NotNull Guild getGuild();

    @NotNull User getUser();

    @NotNull CacheRestAction<@Nullable Member> getMember();


    @Nullable User getModerator();


    @Nullable String getReason();

    @Nullable String getComment();


    @NotNull Instant getTime();

    @Nullable Instant getEnd();

    @Nullable Duration getDuration();

}
