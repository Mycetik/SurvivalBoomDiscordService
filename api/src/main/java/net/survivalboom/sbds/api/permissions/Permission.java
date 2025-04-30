package net.survivalboom.sbds.api.permissions;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Permission {

    private final String permission;

    private boolean value;


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

    public void value(boolean v) {
        this.value = v;
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Permission permission)) return false;
        return this.permission.equals(permission.permission) && this.value == permission.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(permission, value);
    }

}
