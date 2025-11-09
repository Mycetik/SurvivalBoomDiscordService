package net.survivalboom.sbds.api.database.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import net.dv8tion.jda.api.entities.Guild;
import net.survivalboom.sbds.api.SbdsProvider;
import org.jetbrains.annotations.Nullable;

@Converter(autoApply = true)
public class GuildConverter implements AttributeConverter<Guild, Long> {

    @Override
    public @Nullable Long convertToDatabaseColumn(@Nullable Guild guild) {

        if (guild == null) {
            return null;
        }

        return guild.getIdLong();

    }

    @Override
    public @Nullable Guild convertToEntityAttribute(@Nullable Long id) {

        if (id == null) {
            return null;
        }

        return SbdsProvider.getInstance().getBot().getGuildById(id);

    }

}
