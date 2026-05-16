package net.survivalboom.sbds.core.permissions.records;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import net.survivalboom.sbds.api.database.DataRecord;
import net.survivalboom.sbds.api.utils.CommonUtils;
import org.jetbrains.annotations.NotNull;

@Entity
@Table(name = "sbds_guild_user_permissions")
public class GuildUserPermissionRecord extends DataRecord {

    @Id
    @Column(nullable = false)
    private long hashId;

    @Column(nullable = false)
    private long guildId;

    @Column(nullable = false)
    private long userId;

    @Column(nullable = false)
    private String permission;

    @Column(nullable = false)
    private boolean value;


    protected GuildUserPermissionRecord() {}

    public GuildUserPermissionRecord(long guildId, long userId, @NotNull String permission, boolean value) {

        this.guildId = guildId;
        this.permission = permission;
        this.value = value;

        this.hashId = CommonUtils.longHash(guildId, userId, permission);

    }

    public long getId() {
        return hashId;
    }

    public long getGuildId() {
        return guildId;
    }

    public long getUserId() {
        return userId;
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
