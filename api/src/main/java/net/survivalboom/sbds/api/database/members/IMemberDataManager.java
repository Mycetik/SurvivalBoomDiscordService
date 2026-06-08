package net.survivalboom.sbds.api.database.members;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.utils.CommonUtils;
import net.survivalboom.sbds.api.utils.valid.IManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public interface IMemberDataManager extends IManager {

    @NotNull ISBDS getSbds();

    // CREATE //

    @NotNull CompletableFuture<@NotNull IMemberData> create(long guildId, long userId);

    default @NotNull CompletableFuture<@NotNull IMemberData> create(@NotNull Member member) {
        return create(member.getGuild().getIdLong(), member.getIdLong());
    }

    default @NotNull CompletableFuture<@NotNull IMemberData> create(@NotNull Guild guild, @NotNull User user) {
        return create(guild.getIdLong(), user.getIdLong());
    }

    // DELETE //

    @NotNull CompletableFuture<Void> delete(long id);

    default @NotNull CompletableFuture<Void> delete(long guildId, long userId) {
        return delete(CommonUtils.longHash(guildId, userId));
    }

    default @NotNull CompletableFuture<Void> delete(IMemberData data) {
        return delete(data.getId());
    }

    default @NotNull CompletableFuture<Void> delete(@NotNull Member member) {
        return delete(member.getGuild().getIdLong(), member.getIdLong());
    }

    default @NotNull CompletableFuture<Void> delete(@NotNull Guild guild, @NotNull User user) {
        return delete(guild.getIdLong(), user.getIdLong());
    }

    // GET //

    @NotNull CompletableFuture<@Nullable IMemberData> get(long id);

    default @NotNull CompletableFuture<@Nullable IMemberData> get(long guildId, long userID) {
        return get(CommonUtils.longHash(guildId, userID));
    }

    default @NotNull CompletableFuture<@Nullable IMemberData> get(@NotNull Member member) {
        return get(member.getGuild().getIdLong(), member.getIdLong());
    }

    default @NotNull CompletableFuture<@Nullable IMemberData> get(@NotNull Guild guild, @NotNull User user) {
        return get(guild.getIdLong(), user.getIdLong());
    }

    // OBTAIN //

    @NotNull CompletableFuture<@NotNull IMemberData> obtain(long guildId, long userId);

    default @NotNull CompletableFuture<@NotNull IMemberData> obtain(@NotNull Member member) {
        return obtain(member.getGuild().getIdLong(), member.getIdLong());
    }

    default @NotNull CompletableFuture<@NotNull IMemberData> obtain(@NotNull Guild guild, @NotNull User user) {
        return obtain(guild.getIdLong(), user.getIdLong());
    }

    // SAVE //

    void save(@NotNull IMemberData memberData);

}
