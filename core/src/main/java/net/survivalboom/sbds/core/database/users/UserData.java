package net.survivalboom.sbds.core.database.users;

import jakarta.persistence.*;
import net.survivalboom.sbds.api.database.converters.NamespacedContainerConverter;
import net.survivalboom.sbds.api.database.converters.TranslationConverter;
import net.survivalboom.sbds.api.translations.ITranslation;
import net.survivalboom.sbds.api.database.DataRecord;
import net.survivalboom.sbds.api.database.users.IUserData;
import net.survivalboom.sbds.api.utils.NamespacedContainer;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.core.database.guilds.GuildData;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

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
    public void translation(@Nullable ITranslation translation) {
        this.translation = translation;
    }

    @Override
    public @NotNull NamespacedContainer container() {
        return data;
    }

    @Override
    public @Nullable ITranslation translation() {
        return translation;
    }

}
