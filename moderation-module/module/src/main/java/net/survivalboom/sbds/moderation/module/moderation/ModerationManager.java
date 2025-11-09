package net.survivalboom.sbds.moderation.module.moderation;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.api.scheduler.ISchedulerTask;
import net.survivalboom.sbds.api.utils.Manager;
import net.survivalboom.sbds.moderation.api.moderation.IModerationManager;
import net.survivalboom.sbds.moderation.module.ModerationModule;
import net.survivalboom.sbds.moderation.module.storage.Punishment;
import net.survivalboom.sbds.moderation.module.storage.PunishmentRepositoryHandler;
import net.survivalboom.sbds.moderation.module.storage.records.Ban;
import net.survivalboom.sbds.moderation.module.storage.records.Mute;
import net.survivalboom.sbds.moderation.module.storage.records.Warn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ModerationManager extends Manager implements IModerationManager {


    private ExpiringQueue expiringQueue;


    private final PunishmentRepositoryHandler<Ban> banRepository;

    private final PunishmentRepositoryHandler<Mute> muteRepository;

    private final PunishmentRepositoryHandler<Warn> warnRepository;


    public ModerationManager(@NotNull ModerationModule module) {

        this.expiringQueue = new ExpiringQueue(this, module);

        banRepository = new PunishmentRepositoryHandler<>(Ban.class, false, Ban::create, expiringQueue);
        muteRepository = new PunishmentRepositoryHandler<>(Mute.class, false, Mute::create, expiringQueue);
        warnRepository = new PunishmentRepositoryHandler<>(Warn.class, true, Warn::create, expiringQueue);

        module.createRepository("bans", banRepository);
        module.createRepository("mutes", muteRepository);
        module.createRepository("warns", warnRepository);

    }


    //
    // INIT
    //


    @Override
    protected void init0() {

        expiringQueue.init();

        banRepository.loadExpiring();
        muteRepository.loadExpiring();
        warnRepository.loadExpiring();

    }

    @Override
    protected void shutdown0() {
        expiringQueue.shutdown0();
    }


    //
    // BANS
    //


    public @NotNull CompletableFuture<Ban> ban(
            @NotNull Guild guild,
            @NotNull User user,
            @Nullable Duration duration,
            @Nullable User responsible,
            @Nullable String reason,
            @Nullable String comment
    ) {

        return punish(guild, user, duration, responsible, reason, comment, banRepository).thenCompose(ban -> guild.ban(user, 0, TimeUnit.SECONDS).reason(reason).submit().thenApply(v -> ban));

    }

    public @NotNull CompletableFuture<Void> removeBan(
            @NotNull Ban ban,
            @Nullable User responsible,
            @Nullable String reason,
            @Nullable String comment
    ) {

        return unPunish(ban, responsible, reason, comment, banRepository).thenCompose(v -> ban.getGuild().unban(ban.getUser()).reason(reason).submit());

    }

    public @NotNull CompletableFuture<@Nullable Ban> getBan(@NotNull Guild guild, @NotNull User user) {
        return banRepository.getPunishment(guild, user);
    }

    public @NotNull CompletableFuture<@Nullable Ban> getBan(@NotNull Member member) {
        return banRepository.getPunishment(member.getGuild(), member.getUser());
    }

    public @NotNull CompletableFuture<List<Ban>> getGuildBans(@NotNull Guild guild) {
        return banRepository.getGuildPunishments(guild);
    }

    public @NotNull CompletableFuture<List<Ban>> getUserBans(@NotNull User user) {
        return banRepository.getUserPunishments(user);
    }



    //
    // KICK
    //

    public @NotNull CompletableFuture<Void> kick(
            @NotNull Guild guild,
            @NotNull User user,
            @Nullable User responsible,
            @Nullable String reason,
            @Nullable String comment
    ) {
        checkValid();
        return guild.kick(user).submit();
    }

    //
    // MUTE
    //

    public @NotNull CompletableFuture<Mute> mute(
            @NotNull Guild guild,
            @NotNull User user,
            @Nullable Duration duration,
            @Nullable User responsible,
            @Nullable String reason,
            @Nullable String comment
    ) {
        checkValid();
        return muteRepository.createPunishment(guild, user, duration, responsible, reason, comment);
    }

    public @NotNull CompletableFuture<Void> removeMute(
            @NotNull Mute mute,
            @Nullable User responsible,
            @Nullable String reason,
            @Nullable String comment
    ) {
        checkValid();
        return muteRepository.removePunishment(mute);
    }

    public @NotNull CompletableFuture<@Nullable Mute> getMute(@NotNull Guild guild, @NotNull User user) {
        return muteRepository.getPunishment(guild, user);
    }

    public @NotNull CompletableFuture<@Nullable Mute> getMute(@NotNull Member member) {
        return muteRepository.getPunishment(member.getGuild(), member.getUser());
    }

    public @NotNull CompletableFuture<List<Mute>> getGuildMutes(@NotNull Guild guild) {
        return muteRepository.getGuildPunishments(guild);
    }

    public @NotNull CompletableFuture<List<Mute>> getUserMutes(@NotNull User user) {
        return muteRepository.getUserPunishments(user);
    }


    //
    // WARN
    //

    public @NotNull CompletableFuture<Warn> warn(
            @NotNull Guild guild,
            @NotNull User user,
            @Nullable Duration duration,
            @Nullable User responsible,
            @Nullable String reason,
            @Nullable String comment
    ) {
        checkValid();
        return warnRepository.createPunishment(guild, user, duration, responsible, reason, comment);
    }

    public @NotNull CompletableFuture<Void> removeWarn(
            @NotNull Warn warn,
            @Nullable User responsible,
            @Nullable String reason,
            @Nullable String comment
    ) {
        checkValid();
        return unPunish(warn, responsible, reason, comment, warnRepository);
    }



    //
    // ABSTRACT
    //

    private <V extends Punishment> @NotNull CompletableFuture<V> punish(
            @NotNull Guild guild,
            @NotNull User user,
            @Nullable Duration duration,
            @Nullable User responsible,
            @Nullable String reason,
            @Nullable String comment,
            @NotNull PunishmentRepositoryHandler<V> repository
    ) {
        checkValid();
        return repository.createPunishment(guild, user, duration, responsible, reason, comment);
    }

    private <V extends Punishment> CompletableFuture<Void> unPunish(
            @NotNull V record,
            @Nullable User responsible,
            @Nullable String reason,
            @Nullable String comment,
            @NotNull PunishmentRepositoryHandler<V> repository
    ) {
        checkValid();
        return repository.removePunishment(record);
    }


    public @NotNull CompletableFuture<Void> removePunishment(
            @NotNull Punishment punishment,
            @Nullable User responsible,
            @Nullable String reason,
            @Nullable String comment
    ) {

        return switch (punishment) {

            case Ban ban -> removeBan(ban, responsible, reason, comment);

            case Mute mute -> removeMute(mute, responsible, reason, comment);

            case Warn warn -> removeWarn(warn, responsible, reason, comment);

            default -> throw new IllegalArgumentException("Unknown punishment");

        };

    }

}
