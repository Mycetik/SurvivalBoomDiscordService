package net.survivalboom.sbds.core.permissions.records;

import jakarta.persistence.*;
import net.survivalboom.sbds.api.database.DataRecord;
import net.survivalboom.sbds.api.utils.CommonUtils;
import org.jetbrains.annotations.NotNull;

@Entity
@Table(name = "sbds_guild_permission_groups")
public class GuildPermissionsGroupRecord extends DataRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private long guildId;

    @Column(nullable = false)
    private String groupName;

    @Column(nullable = false)
    private int weight;


    public GuildPermissionsGroupRecord(long guildId, @NotNull String groupName, int weight) {
        this.guildId = guildId;
        this.groupName = groupName;
        this.weight = weight;
    }

    public GuildPermissionsGroupRecord() {

    }

    public long getId() {
        return id;
    }


    public long getGuildId() {
        return guildId;
    }

    public @NotNull String getGroupName() {
        return groupName;
    }

    public int getWeight() {
        return weight;
    }

}
