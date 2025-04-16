package net.survivalboom.sbds.api.database.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import jakarta.persistence.PersistenceException;
import net.survivalboom.sbds.api.utils.NamespacedContainer;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.api.utils.TypeMap;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Converter(autoApply = true)
public class NamespacedContainerConverter implements AttributeConverter<NamespacedContainer, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(NamespacedContainer container) {

        if (container == null) {
            return null;
        }

        // превращаем в Map<String, Map<String, Object>>
        Map<String, Map<String, Object>> raw = new HashMap<>();
        container.map().forEach((nsKey, typeMap) ->
                raw.put(nsKey.toString(), typeMap.map())
        );

        try {
            return OBJECT_MAPPER.writeValueAsString(raw);
        }

        catch (JsonProcessingException e) {
            throw new PersistenceException("Не удалось сериализовать NamespacedContainer в JSON", e);
        }

    }

    @Override
    public NamespacedContainer convertToEntityAttribute(String dbData) {

        if (dbData == null || dbData.isBlank()) {
            return NamespacedContainer.empty();
        }

        try {

            // читаем как Map<String, Map<String, Object>>
            Map<String, Map<String, Object>> raw = OBJECT_MAPPER.readValue(
                    dbData, new TypeReference<>() {}
            );

            NamespacedContainer container = NamespacedContainer.empty();
            for (Map.Entry<String, Map<String, Object>> entry : raw.entrySet()) {
                NamespacedKey key = NamespacedKey.fromString(entry.getKey());
                // создаём/берём TypeMap и заполняем его
                TypeMap tm = container.getOrCreate(key);
                tm.putAll(entry.getValue());
            }

            return container;

        }

        catch (IOException e) {
            throw new PersistenceException("Не удалось десериализовать JSON в NamespacedContainer", e);
        }

    }
}
