package net.survivalboom.sbds.api.permissions;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface IGuildUserPermissions {

    boolean hasPermission(@NotNull String permission, boolean allowDefault);


    void setPermission(@NotNull String permission, boolean value);

    void unsetPermission(@NotNull String permission);


    long getGuildId();

    long getUserId();

    @NotNull Set<Permission> getPermissions();

    @NotNull Set<IGuildGroup> getGroups();

}
