package net.survivalboom.sbds.core.permissions;

import net.dv8tion.jda.api.entities.Guild;
import net.survivalboom.sbds.api.permissions.*;
import net.survivalboom.sbds.api.utils.valid.Valid;
import net.survivalboom.sbds.core.permissions.records.GuildPermissionsGroupRecord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GuildPermissionsGroup extends Valid implements IGuildPermissionsGroup {

    private final PermissionManager manager;

    private final Guild guild;

    private final GuildPermissionsGroupRecord record;

    private final Map<String, Permission> permissionMap = new HashMap<>();


    public GuildPermissionsGroup(@NotNull GuildPermissionsGroupRecord record, @NotNull PermissionManager manager) {

        this.record = record;
        this.manager = manager;

        this.guild = manager.getSbds().getBot().getGuildById(record.getGuildId());

    }


    @Override
    public @NotNull IPermissionManager getManager() {
        return manager;
    }

    @Override
    public long getId() {
        return record.getId();
    }

    @Override
    public @NotNull Guild getGuild() {
        return guild;
    }

    @Override
    public @NotNull String getName() {
        return record.getGroupName();
    }

    @Override
    public int getWeight() {
        return record.getWeight();
    }

    public @NotNull GuildPermissionsGroupRecord getRecord() {
        return record;
    }

    //
    // PERMISSIONS
    //

    @Override
    public boolean hasPermission(@NotNull String permission, boolean defaultAllow) {

        Objects.requireNonNull(permission, "permission == null");
        checkValid();

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
        return new HashMap<>(permissionMap);
    }

}
