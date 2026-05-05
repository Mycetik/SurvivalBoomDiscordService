package net.survivalboom.sbds.api.database.converters;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import jakarta.persistence.PersistenceException;
import net.survivalboom.sbds.api.utils.container.INamespacedDataContainer;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.api.utils.container.NamespacedDataContainer;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.jackson.JacksonConfigurationLoader;

import java.io.IOException;
import java.util.Map;

@Converter(autoApply = true)
public class NamespacedContainerConverter implements AttributeConverter<INamespacedDataContainer, String> {

    @Override
    public String convertToDatabaseColumn(INamespacedDataContainer container) {

        if (container == null) {
            return null;
        }

        ConfigurationNode rootNode = JacksonConfigurationLoader.builder().build().createNode();

        for (var entry : container.getAsMap().entrySet()) {

            var key = entry.getKey();
            var node = entry.getValue();

            rootNode.node(key).mergeFrom(node);

        }

        String out;
        try {
            out = JacksonConfigurationLoader.builder().buildAndSaveString(rootNode);
        }

        catch (ConfigurateException e) {
            throw new RuntimeException("Something went wrong! Failed to save NamespacedDataContainer to JSON! This may be an internal error, or you just messed up with objects");
        }

        return out;

    }

    @Override
    public INamespacedDataContainer convertToEntityAttribute(String dbData) {

        if (dbData == null || dbData.isBlank()) {
            return new NamespacedDataContainer();
        }

        ConfigurationNode rootNode;
        try {
            
            rootNode = JacksonConfigurationLoader.builder().buildAndLoadString(dbData);

            NamespacedDataContainer container = new NamespacedDataContainer();
            for (var entry : rootNode.childrenMap().entrySet()) {

                String key = (String) entry.getKey();
                ConfigurationNode node = entry.getValue();

                NamespacedKey namespacedKey = NamespacedKey.fromString(key);

                container.obtainNode(namespacedKey).mergeFrom(node);

            }

            return container;

        }
        
        catch (Exception e) {
            throw new RuntimeException("Something went wrong! Failed to load data. Looks like you fucked up with the JSON format. Raw data `" + dbData + "`");
        }

    }

}
