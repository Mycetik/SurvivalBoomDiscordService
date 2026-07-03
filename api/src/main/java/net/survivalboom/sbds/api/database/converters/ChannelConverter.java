package net.survivalboom.sbds.api.database.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.survivalboom.sbds.api.SbdsProvider;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Objects;

@Converter(autoApply = true)
public class ChannelConverter implements AttributeConverter<Channel, Long>, TypeSerializer<Channel> {

    @Override
    public Long convertToDatabaseColumn(Channel channel) {
        return channel.getIdLong();
    }

    @Override
    public Channel convertToEntityAttribute(Long id) {

        Channel channel = SbdsProvider.getInstance().getBot().getChannelById(Channel.class, id);
        Objects.requireNonNull(channel, "Unknown channel with id `" + id + "`");

        return channel;

    }

    @Override
    public Channel deserialize(@NotNull Type type, @NotNull ConfigurationNode node) throws SerializationException {

        long id = node.getLong();
        if (id == 0) {
            throw new SerializationException("Invalid id of channel `" + node.getString() + "`");
        }

        Channel channel = SbdsProvider.getInstance().getBot().getChannelById(Channel.class, id);
        if (channel == null) {
            throw new SerializationException("Unknown channel with id `" + id + "`");
        }

        return channel;

    }

    @Override
    public void serialize(@NotNull Type type, @Nullable Channel obj, @NotNull ConfigurationNode node) throws SerializationException {

        if (obj == null) {
            node.set(null);
        }

        else {
            node.set(obj.getIdLong());
        }

    }

}
