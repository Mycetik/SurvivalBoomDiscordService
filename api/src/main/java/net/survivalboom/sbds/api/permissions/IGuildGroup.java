package net.survivalboom.sbds.api.permissions;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface IGuildGroup {

    void setPermission(@NotNull Permission permission);

    void setPermission(@NotNull String permission, boolean value);

    void unsetPermission(@NotNull String permission);


    long getId();

    long getGuildId();


    @NotNull String getName();

    int weight();

    void weight(int v);


    @NotNull Set<Permission> getPermissions();


}
