package vn.edu.shiningenglish.shiningenglishapi.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tools.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.Map;

@Converter
public class JsonStringMapConverter implements AttributeConverter<Map<String, String>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, String> attribute) {
        if (attribute == null || attribute.isEmpty()) return "{}";
        try {
            return MAPPER.writeValueAsString(attribute);
        } catch (Exception e) {
            return "{}";
        }
    }

    @Override
    public Map<String, String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return Collections.emptyMap();
        try {
            return MAPPER.readValue(dbData, MAPPER.getTypeFactory().constructMapType(Map.class, String.class, String.class));
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}
