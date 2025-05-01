package net.survivalboom.sbds.api.permissions;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Permission {

    private final String permission;

    private final boolean value;


    public Permission(@NotNull String permission, boolean value) {
        this.permission = permission;
        this.value = value;
    }


    public @NotNull String permission() {
        return permission;
    }

    public boolean value() {
        return value;
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Permission permission)) return false;
        return permission.value == this.value && permission.permission.equals(this.permission);
    }

    @Override
    public int hashCode() {
        return Objects.hash(permission, value);
    }

    @Override
    public String toString() {
        return "Permission{permission=" + permission + ",value=" + value + "}";
    }


}
