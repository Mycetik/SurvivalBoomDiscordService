package net.survivalboom.sbds.core.database.permissions;

import jakarta.persistence.*;
import net.survivalboom.sbds.api.database.DataRecord;
import org.jetbrains.annotations.NotNull;

@Entity
@Table(name = "sbds_pg")
public class GroupRecord extends DataRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private long guildId;

    @Column(nullable = false)
    private String name;


    public GroupRecord() {}

    public GroupRecord(long guildId, @NotNull String name) {
        this.guildId = guildId;
        this.name = name;
    }


    public @NotNull String name() {
        return name;
    }

    public long guildId() {
        return guildId;
    }

    public long id() {
        return id;
    }

}
