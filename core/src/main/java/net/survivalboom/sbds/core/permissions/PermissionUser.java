package net.survivalboom.sbds.core.permissions;

import net.survivalboom.sbds.api.permissions.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PermissionUser {

    private final long userId;

    private final long guildId;


    private final Set<Permission> userPermissions = new HashSet<>();

    private final Set<Group> groups = new HashSet<>();

    private final Map<String, Permission> permissionCache = new HashMap<>();


    public PermissionUser(long guildId, long userId) {
        this.guildId = guildId;
        this.userId = userId;
    }


    public @NotNull Set<Permission> getUserPermissions() {
        return new HashSet<>(userPermissions);
    }

    public @NotNull Set<Group> getGroups() {
        return new HashSet<>(groups);
    }


    public boolean hasPermission(@NotNull String permission) {
        return permissionCache.containsKey(permission);
    }

    public void addUserPermission(@NotNull Permission permission) {
        this.userPermissions.add(permission);
    }

    public void unsetUserPermission(@NotNull String permission) {
        this.userPermissions.removeIf(p -> p.permission().equals(permission));
    }


    public void addGroups(@NotNull Set<Group> groups) {
        this.groups.addAll(groups);
    }

    public void rebuildPermissionCache() {

        Map<String, Permission> permissionCache = new HashMap<>();

        List<Group> groups = new ArrayList<>(this.groups);
        groups.sort(Comparator.comparing(Group::weight));

        groups.forEach(group -> {
            Set<Permission> permissions = group.permissions();
            permissions.forEach(permission -> addPermission(permissionCache, permission));
        });

        userPermissions.forEach(permission -> addPermission(permissionCache, permission));

        this.permissionCache.clear();
        this.permissionCache.putAll(permissionCache);

    }


    private void addPermission(@NotNull Map<String, Permission> permissionCache, @NotNull Permission permission) {

        String name = permission.permission();
        boolean value = permission.value();

        Permission permissionWithThatName = permissionCache.get(name);
        if (permissionWithThatName == null) {
            permissionCache.put(name, permission);
            return;
        }

        boolean permissionWithThatNameValue = permission.value();
        if (!permissionWithThatNameValue && value) return;

        permissionCache.put(name, permission);

    }

}
