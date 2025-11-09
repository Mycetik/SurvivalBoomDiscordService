package net.survivalboom.sbds.moderation.module.storage;

import jakarta.persistence.*;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;
import net.survivalboom.sbds.api.database.DataRecord;
import net.survivalboom.sbds.api.database.converters.GuildConverter;
import net.survivalboom.sbds.api.database.converters.UserConverter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@MappedSuperclass
public abstract class Punishment extends DataRecord {

    @Id
    private long id;


    @Column(nullable = false)
    @Convert(converter = GuildConverter.class)
    private Guild guild;

    @Column(nullable = false)
    @Convert(converter = UserConverter.class)
    private User user;


    @Column
    private String reason;

    @Column
    private String comment;


    @Column
    @Convert(converter = UserConverter.class)
    private User responsible;


    @Column(nullable = false)
    private Instant time;

    @Column
    private Instant end;


    public Punishment(

            @NotNull Guild guild,
            @NotNull User user,

            @Nullable String reason,
            @Nullable String comment,

            @Nullable User responsible,

            @NotNull Instant time,
            @Nullable Instant end,

            boolean allowDuplication

    ) {

        Objects.requireNonNull(guild, "guild == null");
        Objects.requireNonNull(user, "user == null");
        Objects.requireNonNull(time, "time == null");

        this.guild = guild;
        this.user = user;

        this.reason = reason;
        this.comment = comment;

        this.responsible = responsible;

        this.time = time;
        this.end = end;

        if (!allowDuplication) {
            this.id = hash(guild.getIdLong(), user.getIdLong());
        }

    }

    protected Punishment() {}


    public @NotNull Guild getGuild() {
        return guild;
    }

    public @NotNull User getUser() {
        return user;
    }

    public CacheRestAction<@Nullable Member> getMember() {
        return guild.retrieveMember(user);
    }


    @Override
    public long getId() {
        return id;
    }


    public @Nullable String getReason() {
        return reason;
    }

    public @Nullable String getComment() {
        return comment;
    }


    public @Nullable User getResponsible() {
        return responsible;
    }


    public @NotNull Instant getTime() {
        return time;
    }

    public @Nullable Instant getEnd() {
        return end;
    }

    public @Nullable Duration getDuration() {

        if (end == null) {
            return null;
        }

        return Duration.ofSeconds(end.minusSeconds(time.getEpochSecond()).getEpochSecond());

    }


    @Override
    public String toString() {
        return String.format("%s{id=%s, guild=%s, user=%s, responsible=%s, reason=%s, comment=%s, time=%s, end=%s}", getClass().getSimpleName(), id, guild, user, responsible, reason, comment, time, end);
    }

}
