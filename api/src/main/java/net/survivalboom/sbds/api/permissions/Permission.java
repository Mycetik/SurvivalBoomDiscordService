package net.survivalboom.sbds.api.permissions;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record Permission(String permission, boolean value) {

    public Permission(@NotNull String permission, boolean value) {
        this.permission = permission;
        this.value = value;
    }


    @Override
    public @NotNull String permission() {
        return permission;
    }


    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof Permission(String permission1, boolean value1))) {
            return false;
        }

        return value1 == this.value && permission1.equals(this.permission);

    }

    @Override
    public String toString() {
        return "Permission{permission=" + permission + ",value=" + value + "}";
    }


}
