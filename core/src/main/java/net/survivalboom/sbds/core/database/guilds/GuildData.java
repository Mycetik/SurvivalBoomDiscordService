package net.survivalboom.sbds.core.database.guilds;

import jakarta.persistence.*;
import net.survivalboom.sbds.api.database.DataRecord;
import net.survivalboom.sbds.api.database.converters.NamespacedContainerConverter;
import net.survivalboom.sbds.api.database.converters.TranslationConverter;
import net.survivalboom.sbds.api.database.guilds.IGuildData;
import net.survivalboom.sbds.api.utils.NamespacedContainer;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jetbrains.annotations.NotNull;

@Entity
@Table(name = "sbds_guilds")
@DynamicUpdate
public class GuildData extends DataRecord implements IGuildData {

    @Id
    @Column(nullable = false)
    private long guildId;

    @Column(columnDefinition = "jsonb", nullable = false)
    @Convert(converter = NamespacedContainerConverter.class)
    public NamespacedContainer data;


    protected GuildData() {}

    public GuildData(long id) {
        this.guildId = id;
        this.data = NamespacedContainer.empty();
    }

    @Override
    public long getId() {
        return guildId;
    }

    public @NotNull NamespacedContainer container() {
        return data;
    }


}
