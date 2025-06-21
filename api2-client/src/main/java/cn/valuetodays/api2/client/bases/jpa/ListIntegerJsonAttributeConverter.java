package cn.valuetodays.api2.client.bases.jpa;

import java.util.List;

import cn.vt.util.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * .
 *
 * @author lei.liu
 * @since 2025-06-01
 */
@Converter
public class ListIntegerJsonAttributeConverter implements AttributeConverter<List<Integer>, String> {
    @Override
    public String convertToDatabaseColumn(List<Integer> attribute) {
        return JsonUtils.toJsonString(attribute);
    }

    @Override
    public List<Integer> convertToEntityAttribute(String dbData) {
        return JsonUtils.fromJson(dbData, new TypeReference<>() {
        });
    }
}
