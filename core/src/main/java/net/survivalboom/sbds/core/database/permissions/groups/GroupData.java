package net.survivalboom.sbds.core.database.permissions.groups;

import jakarta.persistence.*;
import net.survivalboom.sbds.api.database.DataRecord;
import org.jetbrains.annotations.NotNull;

@Entity
@Table(name = "sbds_permission_groups")
public class GroupData extends DataRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private long guildId;

    @Column(nullable = false)
    private String name;


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
