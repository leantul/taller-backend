package com.taller.model.enums.converter;

import com.taller.model.enums.RepairStatusEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;


@Converter(autoApply = true)
public class RepairStatusEnumConverter implements AttributeConverter<RepairStatusEnum, Integer> {

    @Override
    public Integer convertToDatabaseColumn(RepairStatusEnum attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getCode();
    }

    @Override
    public RepairStatusEnum convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }

        return RepairStatusEnum.fromCode(dbData);
    }
}