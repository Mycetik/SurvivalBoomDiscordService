package net.survivalboom.sbds.modules.moderation.module.moderation;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.managers.channel.attribute.IPermissionContainerManager;
import net.survivalboom.sbds.api.database.guilds.IGuildRepositoryHandler;
import net.survivalboom.sbds.api.utils.TypeMap;
import net.survivalboom.sbds.modules.moderation.api.moderation.IMuteManager;
import net.survivalboom.sbds.modules.moderation.api.storage.IAuditEntry;
import net.survivalboom.sbds.modules.moderation.api.storage.IMuteData;
import net.survivalboom.sbds.modules.moderation.module.ModerationModule;
import net.survivalboom.sbds.modules.moderation.module.storage.PunishmentRepositoryHandler;
import net.survivalboom.sbds.modules.moderation.module.storage.records.Mute;
import net.survivalboom.sbds.modules.moderation.module.utils.ExpiringModerationManager;
import net.survivalboom.sbds.modules.moderation.module.utils.ExpiringQueue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.time.Duration;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MuteManager extends ExpiringModerationManager<Mute> implements IMuteManager {

    public MuteManager(@NotNull ExpiringQueue expiringQueue, @NotNull ModerationModule module) {
        super(new PunishmentRepositoryHandler<>(Mute.class, false, Mute::create, expiringQueue), expiringQueue, module);
    }

    @Override
    protected void init0() {
        module.createRepository("mutes", repository);
    }

    @Override
    public @NotNull CompletableFuture<@NotNull IMuteData> mute(

            @NotNull Guild guild,
            @NotNull User user,
            @Nullable User moderator,

            @Nullable String reason,
            @Nullable String comment,

            @Nullable Duration duration

    ) {

        checkValid();

        if (duration != null && duration.toDays() > 30) {
            throw new IllegalArgumentException("Could not mute a user for more than 30 days. [" + duration.toDays() + "]");
        }

        return punish(guild, user, moderator, reason, comment, duration)
                    .thenApply(mute -> {

                        createMuteRole(guild).thenCompose(role -> guild.addRoleToMember(user, role).reason("SBDS-MUTED").submit());

                        if (duration != null) {
                            guild.timeoutFor(user, duration).reason(reason).queue();
                        }

                        return mute;

                    });
        }

    @Override
    public @NotNull CompletableFuture<@NotNull IMuteData> mute(

            @NotNull Member member,
            @Nullable User moderator,

            @Nullable String reason,
            @Nullable String comment,

            @Nullable Duration duration

    ) {

        checkValid();

        if (duration != null && duration.toDays() > 30) {
            throw new IllegalArgumentException("Could not mute a user for more than 30 days. [" + duration.toDays() + "]");
        }

        Guild guild = member.getGuild();
        User user = member.getUser();

        return mute(guild, user, moderator, reason, comment, duration);

    }

    private @NotNull CompletableFuture<Role> createMuteRole(@NotNull Guild guild) {

        return module.getDatabase().getRepositoryHandler("sbds:guilds", IGuildRepositoryHandler.class).createGuildData(guild).thenCompose(gd -> {

            TypeMap container = gd.container().getOrCreate(GUILD_DATA_KEY);
            Long id = container.get("mute-role", Long.class);
            Role role = id != null ? guild.getRoleById(id) : null;
            if (role == null) {
                return guild.createRole().setName("SBDS-MUTED").setColor(Color.GRAY).submit().thenApply(r -> {
                    applyMuteRoleToChannels(r);
                    container.put("mute-role", r.getIdLong());
                    return r;
                });
            }

            return CompletableFuture.completedFuture(role);

        });

    }

    private void applyMuteRoleToChannels(@NotNull Role role) {

        role.getGuild().getChannels().forEach(channel -> {
            IPermissionContainerManager<?, ?> manager = (IPermissionContainerManager<?, ?>) channel.getManager();
            manager.putRolePermissionOverride(role.getIdLong(), null, EnumSet.of(Permission.MESSAGE_SEND)).queue();
        });

    }

    @Override
    public @NotNull CompletableFuture<IAuditEntry> removeMute(

            @NotNull IMuteData muteData,
            @Nullable User moderator,

            @Nullable String reason,
            @Nullable String comment

    ) {

        checkValid();

        var mute = (Mute) muteData;

        Guild guild = mute.getGuild();
        User user = mute.getUser();

        return unPunish(mute, moderator, reason, comment).thenApply(entry -> {

            createMuteRole(guild)
                    .thenCompose(role -> guild.removeRoleFromMember(user, role).submit().thenCompose(v -> guild.removeTimeout(user).submit()));

            return entry;

        });

    }


    @Override
    public @NotNull CompletableFuture<@NotNull List<IMuteData>> getCurrent(@Nullable Guild guild, @Nullable User user) {
        return getCurrent0(guild, user).thenApply(list -> list.stream().map(m -> (IMuteData) m).toList());
    }

}
