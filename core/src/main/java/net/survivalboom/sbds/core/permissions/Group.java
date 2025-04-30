package net.survivalboom.sbds.core.permissions;

import net.survivalboom.sbds.api.permissions.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Group {

    private final String name;


    private final long id;

    private final long guildId;


    private final Set<Permission> permissions;

    private int weigth;


    public Group(@NotNull String name, long id, long guildId, @NotNull Set<Permission> permissions) {
        this.name = name;
        this.id = id;
        this.guildId = guildId;
        this.permissions = permissions;
    }


    public @NotNull String name() {
        return name;
    }

    public @NotNull Set<Permission> permissions() {
        return new HashSet<>(permissions);
    }

    public void addPermission(@NotNull Permission permission) {
        permissions.add(permission);
    }

    public void removePermission(@NotNull Permission permission) {
        permissions.remove(permission);
    }


    public long guildId() {
        return guildId;
    }

    public long id() {
        return id;
    }


    public void weight(int v) {
        this.weigth = v;
    }

    public int weight() {
        return weigth;
    }


    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof Group group)) return false;

        return group.id == this.id && group.guildId == this.guildId && group.name.equals(this.name) && group.permissions.equals(this.permissions);

    }

    @Override
    public int hashCode() {
        return Objects.hash(id, guildId);
    }

}
