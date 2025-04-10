package net.survivalboom.sbds.core.database.users;

import jakarta.persistence.*;
import net.survivalboom.sbds.api.database.converters.TranslationConverter;
import net.survivalboom.sbds.api.translations.ITranslation;
import net.survivalboom.sbds.api.database.DataRecord;
import net.survivalboom.sbds.api.database.users.IUserData;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> data;

    @Column
    @Convert(converter = TranslationConverter.class)
    @Nullable
    private ITranslation translation;


    protected UserData() {}

    public UserData(long id) {
        this.id = id;
        this.data = new HashMap<>();
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
    public @Nullable ITranslation translation() {
        return translation;
    }

}
