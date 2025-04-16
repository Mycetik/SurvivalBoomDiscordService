package net.survivalboom.sbds.api.database.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import jakarta.persistence.PersistenceException;
import net.survivalboom.sbds.api.utils.TypeMap;

import java.io.IOException;
import java.util.Map;

@Converter(autoApply = true)
public class TypeMapConverter implements AttributeConverter<TypeMap, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(TypeMap attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            // Сериализуем внутренний map в JSON
            return OBJECT_MAPPER.writeValueAsString(attribute.map());
        } catch (JsonProcessingException e) {
            throw new PersistenceException("Не удалось сериализовать TypeMap в JSON", e);
        }
    }

    @Override
    public TypeMap convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            // создаём пустой TypeMap, разрешающий модификацию
            return TypeMap.empty(true);
        }
        try {
            // Десериализуем JSON в Map<String, Object>
            Map<String, Object> map = OBJECT_MAPPER.readValue(dbData, new TypeReference<>() {});
            // Возвращаем копию, разрешающую модификацию
            return TypeMap.copyMap(map, true);
        } catch (IOException e) {
            throw new PersistenceException("Не удалось десериализовать JSON в TypeMap", e);
        }
    }

}
