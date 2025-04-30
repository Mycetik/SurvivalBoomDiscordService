package net.survivalboom.sbds.core.database.permissions.group_permission;

import jakarta.persistence.*;
import net.survivalboom.sbds.api.database.DataRecord;
import net.survivalboom.sbds.api.permissions.Permission;
import org.jetbrains.annotations.NotNull;

@Entity
@Table(name = "sbds_group_permissions")
public class GroupPermissionData extends DataRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private long guildId;

    @Column
    private String group;

    @Column(nullable = false)
    private String permission;

    @Column(nullable = false)
    private boolean value;


    public @NotNull Permission toPermission() {
        return new Permission(permission, value);
    }

}
