package net.survivalboom.sbds.api.permissions;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.survivalboom.sbds.api.database.guilds.IGuildData;
import net.survivalboom.sbds.api.database.users.IUserData;
import net.survivalboom.sbds.api.modules.IModule;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface IPermissionManager {

    boolean hasPermission(long guildId, long userId, @NotNull String permission);

    boolean hasPermission(@NotNull Guild guild, @NotNull Member member, @NotNull String permission);

    boolean hasPermission(@NotNull IGuildData guild, @NotNull IUserData user, @NotNull String permission);


    void setUserPermission(long guildId, long userId, @NotNull String permission, boolean value);

    void setUserPermission(long guildId, long userId, @NotNull Permission permission);

    void setUserPermissions(long guildId, long userId, @NotNull Set<Permission> permissions);

    void unsetUserPermission(long guildId, long userId, @NotNull Permission permission);

    void unsetUserPermission(long guildId, long userId, @NotNull String permission);

    void unsetUserPermissions(long guildId, long userId, @NotNull Set<Permission> permissions);


    void setGroupPermission(long guildId, @NotNull String group, @NotNull String permission, boolean value);

    void setGroupPermission(long guildId, @NotNull String group, @NotNull Permission permission);

    void setGroupPermissions(long guildId, @NotNull String group, @NotNull Set<Permission> permissions);

    void unsetGroupPermission(long guildId, @NotNull String group, @NotNull String permission);

    void unsetGroupPermission(long guildId, @NotNull String group, @NotNull Permission permission);

    void unsetGroupPermissions(long guildId, @NotNull String group, @NotNull Set<Permission> permissions);


    void createGroup(long guildId, @NotNull String group);

    void removeGroup(long guildId, @NotNull String group);


    void reload(@NotNull IModule module);

}
