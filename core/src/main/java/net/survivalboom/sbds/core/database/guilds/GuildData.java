package net.survivalboom.sbds.core.database.guilds;

import jakarta.persistence.*;
import net.survivalboom.sbds.api.database.DataRecord;
import net.survivalboom.sbds.api.database.guilds.IGuildData;
import net.survivalboom.sbds.api.utils.TypeMap;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "sbds_guilds")
public class GuildData extends DataRecord implements IGuildData {

    @Id
    @Column(nullable = false)
    private long guildId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> data;


    protected GuildData() {}

    public GuildData(long id) {
        this.guildId = id;
        this.data = new HashMap<>();
    }

    @Override
    public long getId() {
        return guildId;
    }

    @Override
    public @NotNull TypeMap data() {
        return TypeMap.ofMap(data, true);
    }

}
