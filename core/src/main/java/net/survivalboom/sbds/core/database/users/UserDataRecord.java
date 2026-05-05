package net.survivalboom.sbds.core.database.users;

import jakarta.persistence.*;
import net.survivalboom.sbds.api.database.converters.NamespacedContainerConverter;
import net.survivalboom.sbds.api.database.converters.TranslationConverter;
import net.survivalboom.sbds.api.translations.ITranslation;
import net.survivalboom.sbds.api.database.DataRecord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Entity
@Table(name = "sbds_users")
public class UserDataRecord extends DataRecord {

    @Id
    @Column(nullable = false)
    private long id;

    @Column(columnDefinition = "jsonb", nullable = false)
    @Convert(converter = NamespacedContainerConverter.class)
    private NamespacedContainer data;

    @Column
    @Convert(converter = TranslationConverter.class)
    @Nullable
    private ITranslation translation;


    protected UserDataRecord() {}

    public UserDataRecord(long id) {
        this.id = id;
        this.data = NamespacedContainer.empty();
        this.translation = null;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof Long l) {
            return l.equals(id);
        }

        return false;

    }

    @Override
    public void setTranslation(@Nullable ITranslation translation) {
        this.translation = translation;
    }

    @Override
    public @NotNull NamespacedContainer container() {
        return data;
    }

    @Override
    public @Nullable ITranslation getTranslation() {
        return translation;
    }


    @Override
    public long getId() {
        return id;
    }

}
