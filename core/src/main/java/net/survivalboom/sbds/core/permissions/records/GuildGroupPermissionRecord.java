package net.survivalboom.sbds.core.permissions.records;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import net.survivalboom.sbds.api.database.DataRecord;
import net.survivalboom.sbds.api.utils.CommonUtils;
import org.jetbrains.annotations.NotNull;

@Entity
@Table(name = "sbds_guild_group_permissions")
public class GuildGroupPermissionRecord extends DataRecord {

    @Id
    @Column(nullable = false)
    private long hashId;

    @Column(nullable = false)
    private long guildId;

    @Column(nullable = false)
    private String groupName;

    @Column(nullable = false)
    private String permission;

    @Column(nullable = false)
    private boolean value;


    public GuildGroupPermissionRecord(long guildId, @NotNull String groupName, @NotNull String permission, boolean value) {

        this.guildId = guildId;
        this.permission = permission;
        this.value = value;

        this.hashId = CommonUtils.longHash(guildId, groupName, permission);

    }

    public long getHashId() {
        return hashId;
    }

    public long getGuildId() {
        return guildId;
    }

    public @NotNull String getGroupName() {
        return groupName;
    }

    public @NotNull String getPermission() {
        return permission;
    }


    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

}
