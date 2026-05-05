package net.survivalboom.sbds.api.database.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import net.survivalboom.sbds.api.SbdsProvider;
import net.survivalboom.sbds.api.translations.ITranslation;

@Converter(autoApply = true)
public class TranslationConverter implements AttributeConverter<ITranslation, String> {

    @Override
    public String convertToDatabaseColumn(ITranslation attribute) {

        if (attribute == null) {
            return null;
        }

        return attribute.getRegistration().key().toString();

    }

    @Override
    public ITranslation convertToEntityAttribute(String dbData) {
        return SbdsProvider.getInstance().getTranslationManager().getTranslation(dbData);
    }

}
