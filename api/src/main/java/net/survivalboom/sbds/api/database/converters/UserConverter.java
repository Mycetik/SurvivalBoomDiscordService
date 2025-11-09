package net.survivalboom.sbds.api.database.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.api.SbdsProvider;
import org.jetbrains.annotations.Nullable;

@Converter(autoApply = true)
public class UserConverter implements AttributeConverter<User, Long> {

    @Override
    public @Nullable Long convertToDatabaseColumn(@Nullable User user) {

        if (user == null) {
            return null;
        }

        return user.getIdLong();

    }

    @Override
    public @Nullable User convertToEntityAttribute(@Nullable Long id) {

        if (id == null) {
            return null;
        }

        return SbdsProvider.getInstance().getBot().retrieveUserById(id).complete();

    }

}
