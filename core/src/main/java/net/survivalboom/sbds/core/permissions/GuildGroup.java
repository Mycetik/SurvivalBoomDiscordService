package net.survivalboom.sbds.core.permissions;

import net.survivalboom.sbds.api.permissions.IGuildGroup;
import net.survivalboom.sbds.api.permissions.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class GuildGroup implements IGuildGroup {

    private final PermissionManager permissionManager;

    private final long id;

    private final long guildId;

    private final String name;

    private int weight;

    private final @NotNull Set<Permission> permissions;

    public GuildGroup(long id, long guildId, @NotNull String name, @NotNull Set<Permission> permissions, @NotNull PermissionManager permissionManager) {
        this.id = id;
        this.guildId = guildId;
        this.name = name;
        this.permissions = permissions;
        this.permissionManager = permissionManager;
    }


    @Override
    public void setPermission(@NotNull Permission permission) {
        permissions.add(permission);
        permissionManager.purgeUserCache(guildId);
    }

    @Override
    public void setPermission(@NotNull String permission, boolean value) {
        permissions.add(new Permission(permission, value));
        permissionManager.purgeUserCache(guildId);
    }

    @Override
    public void unsetPermission(@NotNull String permission) {
        permissions.removeIf(p -> p.permission().equals(permission));
        permissionManager.purgeUserCache(guildId);
    }


    @Override
    public long getId() {
        return id;
    }

    @Override
    public long getGuildId() {
        return guildId;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public int weight() {
        return weight;
    }

    @Override
    public void weight(int v) {
        this.weight = v;
    }

    @Override
    public @NotNull Set<Permission> getPermissions() {
        return new HashSet<>(permissions);
    }

}
