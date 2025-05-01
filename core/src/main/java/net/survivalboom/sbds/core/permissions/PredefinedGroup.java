package net.survivalboom.sbds.core.permissions;

import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PredefinedGroup {

    private final Set<Permission> permissionSet = new HashSet<>();

    private final int weight;

    private final IModule module;

    public PredefinedGroup(@Nullable IModule module, int weight) {
        this.module = module;
        this.weight = weight;
    }


    public void addPermission(@NotNull Permission permission) {
        permissionSet.add(permission);
    }

    public void removePermission(@NotNull Permission permission) {
        permissionSet.remove(permission);
    }

    public Set<Permission> getPermissions() {
        return new HashSet<>(permissionSet);
    }

    public int getWeight() {
        return weight;
    }

    public @Nullable IModule getModule() {
        return module;
    }

}
