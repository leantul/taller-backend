package com.taller.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum RepairStatusEnum {
    POR_RECIBIR(1, "Por recibir"),
    RECIBIDA(2, "Recibida"),
    PRESUPUESTADA_ESPERANDO_RESPUESTA(3, "Presupuestada y esperando respuesta"),
    HACIENDO(4, "Haciendo"),
    ESPERANDO_RETIRO(5, "Esperando retiro"),
    RETIRADA(6, "Retirada");

    private final int code;
    private final String status;

    RepairStatusEnum(int code, String status) {
        this.code = code;
        this.status = status;
    }

    @JsonCreator
    public static RepairStatusEnum fromJson(Object rawValue) {
        if (rawValue instanceof Number numberValue) {
            return fromCode(numberValue.intValue());
        }

        if (rawValue instanceof String stringValue) {
            String normalized = stringValue.trim();
            if (normalized.matches("^\\d+$")) {
                return fromCode(Integer.parseInt(normalized));
            }

            for (RepairStatusEnum repairStatus : values()) {
                if (repairStatus.name().equalsIgnoreCase(normalized)
                        || repairStatus.getStatus().equalsIgnoreCase(normalized)) {
                    return repairStatus;
                }
            }
        }

        throw new IllegalArgumentException("No enum constant for value " + rawValue);
    }

    public static RepairStatusEnum fromCode(int code) {
        for (RepairStatusEnum repairStatus : values()) {
            if (repairStatus.getCode() == code) {
                return repairStatus;
            }
        }
        throw new IllegalArgumentException("No enum constant with code " + code);
    }

    @JsonValue
    public String getStatus() {
        return status;
    }
}
