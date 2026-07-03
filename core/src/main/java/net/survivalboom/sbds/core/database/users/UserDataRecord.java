package net.survivalboom.sbds.core.database.users;

import jakarta.persistence.*;
import net.survivalboom.sbds.api.database.converters.NamespacedContainerConverter;
import net.survivalboom.sbds.api.database.converters.TranslationConverter;
import net.survivalboom.sbds.api.translations.ITranslation;
import net.survivalboom.sbds.api.database.DataRecord;
import net.survivalboom.sbds.api.utils.container.NamespacedDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Entity
@Table(name = "sbds_users")
public class UserDataRecord extends DataRecord {

    @Id
    @Column(nullable = false)
    private long userId;

    @Column(columnDefinition = "jsonb", nullable = false)
    @Convert(converter = NamespacedContainerConverter.class)
    private NamespacedDataContainer data;

    @Column
    @Convert(converter = TranslationConverter.class)
    @Nullable
    private ITranslation translation;


    protected UserDataRecord() {}

    public UserDataRecord(long userId) {
        this.userId = userId;
        this.data = new NamespacedDataContainer();
        this.translation = null;
    }

    public long getUserId() {
        return userId;
    }

    public @NotNull NamespacedDataContainer getContainer() {
        return data;
    }

    public @Nullable ITranslation getTranslation() {
        return translation;
    }

    public void setTranslation(@Nullable ITranslation translation) {
        this.translation = translation;
    }

}
