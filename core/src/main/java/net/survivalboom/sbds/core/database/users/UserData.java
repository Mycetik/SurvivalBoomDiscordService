package net.survivalboom.sbds.core.database.users;

import jakarta.persistence.*;
import net.survivalboom.sbds.api.database.converters.NamespacedContainerConverter;
import net.survivalboom.sbds.api.database.converters.TranslationConverter;
import net.survivalboom.sbds.api.translations.ITranslation;
import net.survivalboom.sbds.api.database.DataRecord;
import net.survivalboom.sbds.api.database.users.IUserData;
import net.survivalboom.sbds.api.utils.NamespacedContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Entity
@Table(name = "sbds_users")
public class UserData extends DataRecord implements IUserData {

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


    protected UserData() {}

    public UserData(long id) {
        this.id = id;
        this.data = NamespacedContainer.empty();
        this.translation = null;
    }

    @Override
    public long getID() {
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
