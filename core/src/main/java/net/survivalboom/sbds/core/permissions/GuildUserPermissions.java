package net.survivalboom.sbds.core.permissions;

import net.survivalboom.sbds.api.permissions.IGuildGroup;
import net.survivalboom.sbds.api.permissions.IGuildUserPermissions;
import net.survivalboom.sbds.api.permissions.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class GuildUserPermissions implements IGuildUserPermissions {

    private final PermissionManager permissionManager;


    private final long id;

    private final long guildId;


    private final Set<GuildGroup> groups;

    private final Set<Permission> permissions;

    private final Map<String, Permission> cachedPermissions = new HashMap<>();


    public GuildUserPermissions(@NotNull PermissionManager permissionManager, long id, long guildId, @NotNull Set<GuildGroup> groups, @NotNull Set<Permission> userPermissions) {

        this.permissionManager = permissionManager;

        this.id = id;
        this.guildId = guildId;

        this.groups = groups;
        this.permissions = userPermissions;

    }


    @Override
    public boolean hasPermission(@NotNull String permissionString, boolean allowDefault) {

        Permission permission = cachedPermissions.values().stream().filter(p -> p.permission().equals(permissionString)).findAny().orElse(null);
        if (permission == null) return allowDefault;

        return permission.value();

    }

    @Override
    public void setPermission(@NotNull String permission, boolean value) {

    }

    @Override
    public void unsetPermission(@NotNull String permission) {

    }

    public void rebuildPermissionCache() {

        Map<String, Permission> permissionCache = new HashMap<>();

        List<PredefinedGroup> predefinedGroups = new ArrayList<>(permissionManager.getPredefinedGroups());
        predefinedGroups.sort(Comparator.comparing(PredefinedGroup::getWeight));

        predefinedGroups.forEach(pg -> {
            Set<Permission> permissions = pg.getPermissions();
            permissions.forEach(permission -> addPermission(permissionCache, permission));
        });

        List<GuildGroup> groups = new ArrayList<>(this.groups);
        groups.sort(Comparator.comparing(GuildGroup::weight));

        groups.forEach(group -> {
            Set<Permission> permissions = group.getPermissions();
            permissions.forEach(permission -> addPermission(permissionCache, permission));
        });

        permissions.forEach(permission -> addPermission(permissionCache, permission));

        this.cachedPermissions.clear();
        this.cachedPermissions.putAll(permissionCache);

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



    @Override
    public long getGuildId() {
        return guildId;
    }

    @Override
    public long getUserId() {
        return id;
    }

    @Override
    public @NotNull Set<Permission> getPermissions() {
        return new HashSet<>(permissions);
    }

    @Override
    public @NotNull Set<IGuildGroup> getGroups() {
        return new HashSet<>(groups);
    }

}
