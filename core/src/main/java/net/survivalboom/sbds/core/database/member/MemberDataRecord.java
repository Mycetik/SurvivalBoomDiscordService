package net.survivalboom.sbds.core.database.member;

import jakarta.persistence.*;
import net.survivalboom.sbds.api.database.DataRecord;
import net.survivalboom.sbds.api.database.converters.NamespacedContainerConverter;
import net.survivalboom.sbds.api.utils.CommonUtils;
import net.survivalboom.sbds.api.utils.container.NamespacedDataContainer;
import org.jetbrains.annotations.NotNull;

@Entity
@Table(name = "sbds_members")
public class MemberDataRecord extends DataRecord {

    @Id
    @Column(nullable = false)
    private long id;

    @Column(nullable = false)
    private long guildId;

    @Column(nullable = false)
    private long userId;

    @Column(nullable = false)
    @Convert(converter = NamespacedContainerConverter.class)
    private NamespacedDataContainer data;


    protected MemberDataRecord() {}

    public MemberDataRecord(long guildId, long userId) {
        this.guildId = guildId;
        this.userId = userId;
        this.id = CommonUtils.longHash(guildId, userId);
        this.data = new NamespacedDataContainer();
    }

    public long getId() {
        return id;
    }

    public long getGuildId() {
        return guildId;
    }

    public long getUserId() {
        return userId;
    }

    public @NotNull NamespacedDataContainer getContainer() {
        return data;
    }


}
