package com.taller.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum RepairStatusEnum {
    RECIBIDA(1, "Recibida"),
    EN_PROCESO(2, "En proceso"),
    PRESUPUESTADA_ESPERANDO_RESPUESTA(3, "Presupuestada y esperando respuesta"),
    DOING(4, "Doing"),
    ESPERANDO_RETIRO(5, "Esperando retiro"),
    RETIRADA(6, "Retirada");

    private final int code;
    private final String status;

    RepairStatusEnum(int code, String status) {
        this.code = code;
        this.status = status;
    }

    @JsonCreator
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
