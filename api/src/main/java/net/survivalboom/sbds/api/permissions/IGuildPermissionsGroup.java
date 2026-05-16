package net.survivalboom.sbds.api.permissions;

import net.dv8tion.jda.api.entities.Guild;
import net.survivalboom.sbds.api.utils.valid.IValid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public interface IGuildPermissionsGroup extends IValid {

    @NotNull IPermissionManager getManager();

    long getId();

    @NotNull Guild getGuild();

    @NotNull String getName();

    int getWeight();

    //
    // PERMISSIONS
    //

    boolean hasPermission(@NotNull String permission, boolean defaultAllow);

    default boolean hasPermission(@NotNull Permission permission) {
        return hasPermission(permission.permission(), !permission.value());
    }


    default boolean hasGroup(@NotNull String group) {
        return hasPermission("group." + group, false);
    }


    void setPermission(@NotNull Permission permission);

    default void setPermission(@NotNull String permission, boolean value) {
        setPermission(new Permission(permission, value));
    }


    void setPermissions(@Nullable Map<String, @Nullable Permission> permissions, boolean override);

    default void setPermissions(@Nullable Collection<Permission> permissions, boolean override) {

        if (permissions == null) {
            setPermissions((Map<String, Permission>) null, override);
            return;
        }

        Map<String, Permission> map = permissions.stream().collect(Collectors.toMap(Permission::permission, p -> p));
        setPermissions(map, override);

    }


    void removePermission(@NotNull String permission);

    default void removePermission(@NotNull Permission permission) {
        removePermission(permission.permission());
    }


    @NotNull Map<String, Permission> getPermissions();

}
