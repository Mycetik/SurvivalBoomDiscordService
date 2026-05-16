package net.survivalboom.sbds.core.permissions;

import net.dv8tion.jda.api.entities.Member;
import net.survivalboom.sbds.api.permissions.IMemberPermissions;
import net.survivalboom.sbds.api.permissions.IPermissionManager;
import net.survivalboom.sbds.api.permissions.Permission;
import net.survivalboom.sbds.api.utils.valid.Valid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MemberPermissions extends Valid implements IMemberPermissions {

    private final PermissionManager manager;

    private final Member member;

    private final Map<String, Permission> permissionMap = new HashMap<>();


    public MemberPermissions(@NotNull Member member, @NotNull PermissionManager manager) {
        this.member = member;
        this.manager = manager;
    }

    @Override
    public @NotNull IPermissionManager getManager() {
        return manager;
    }

    @Override
    public @NotNull Member getMember() {
        return member;
    }

    //
    // PERMISSIONS
    //

    @Override
    public boolean hasPermission(@NotNull String permission, boolean defaultAllow) {

        checkValid();
        Objects.requireNonNull(permission, "permission == null");

        Permission perm = permissionMap.get(permission);
        if (perm == null) {
            return defaultAllow;
        }

        return perm.value();

    }


    @Override
    public void setPermission(@NotNull Permission permission) {

        Objects.requireNonNull(permission, "permission == null");
        checkValid();

        permissionMap.put(permission.permission(), permission);

    }

    @Override
    public void removePermission(@NotNull String permission) {
        checkValid();
        permissionMap.remove(permission);
    }


    @Override
    public void setPermissions(@Nullable Map<String, @Nullable Permission> permissions, boolean override) {

        checkValid();

        if (permissions == null) {
            permissionMap.clear();
            return;
        }

        for (var entry : permissions.entrySet()) {

            String key = entry.getKey();
            Permission perm = entry.getValue();

            if (perm != null && override) {
                permissionMap.put(key, perm);
            }

            else if (perm == null && override) {
                permissionMap.remove(key);
            }

        }

    }

    @Override
    public @NotNull Map<String, Permission> getPermissions() {
        checkValid();
        return new HashMap<>(permissionMap);
    }


    @Override
    protected void setValid(boolean v) {

        if (v) {
            return;
        }

        permissionMap.clear();

    }

}
