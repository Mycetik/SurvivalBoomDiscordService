package net.survivalboom.sbds.api.permissions;

import net.dv8tion.jda.api.entities.Member;
import net.survivalboom.sbds.api.modules.IModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface IPermissionManager {

    //
    // PERMISSIONS
    //

    boolean hasPermission(long guildId, long userId, @NotNull String permission, boolean defaultAllow);

    boolean hasPermission(@NotNull Member member, @NotNull String permission, boolean defaultAllow);

    @NotNull IGuildUserPermissions createUserPermissions(long guildId, long userId);

    @Nullable IGuildUserPermissions getUserPermissions(long guildId, long userId);

    //
    // GROUPS
    //

    @NotNull Set<IGuildGroup> getGuildGroups(long guildId);

    @Nullable IGuildGroup getGuildGroup(long guildId, @NotNull String group);

    @NotNull IGuildGroup createGroup(long guildId, @NotNull String group);

    void removeGroup(long guildId, @NotNull String group);

    //
    // PRE-DEFINED GROUPS
    //

    void registerPredefinedGroup(@NotNull IModule module, @NotNull String name);

    void unregisterPredefinedGroup(@NotNull IModule module, @NotNull String name);

    //
    // PRE-DEFINED PERMISSIONS
    //

    void addPredefinedPermission(@NotNull IModule module, @NotNull String group, @NotNull Permission permission);

    void removePredefinedPermission(@NotNull IModule module, @NotNull String group, @NotNull String permission);

    void removePredefinedPermission(@NotNull IModule module, @NotNull String group, @NotNull Permission permission);

    //
    // MISC
    //

    void reload(@NotNull IModule module);

}
