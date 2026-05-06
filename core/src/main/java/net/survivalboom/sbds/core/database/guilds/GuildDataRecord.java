package net.survivalboom.sbds.core.database.guilds;

import jakarta.persistence.*;
import net.survivalboom.sbds.api.database.DataRecord;
import net.survivalboom.sbds.api.database.converters.NamespacedContainerConverter;
import net.survivalboom.sbds.api.database.converters.TranslationConverter;
import net.survivalboom.sbds.api.translations.ITranslation;
import net.survivalboom.sbds.api.utils.container.NamespacedDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Entity
@Table(name = "sbds_guilds")
public class GuildDataRecord extends DataRecord {

    @Id
    @Column(nullable = false)
    private long guildId;

    @Column(columnDefinition = "jsonb", nullable = false)
    @Convert(converter = NamespacedContainerConverter.class)
    private NamespacedDataContainer data;

    @Column
    @Convert(converter = TranslationConverter.class)
    private ITranslation translation;


    protected GuildDataRecord() {}

    public GuildDataRecord(long id) {
        this.guildId = id;
        this.data = new NamespacedDataContainer();
    }


    public long getGuildId() {
        return guildId;
    }

    public @NotNull NamespacedDataContainer getData() {
        return data;
    }


    public @NotNull ITranslation getTranslation() {
        return translation;
    }

    public void setTranslation(@Nullable ITranslation translation) {
        this.translation = translation;
    }


}
