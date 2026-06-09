package com.taller.resource.dto;

import lombok.Getter;
import lombok.Setter;

import com.taller.model.enums.RepairStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class NotificationDTO {
    private String id;
    private String title;
    private String message;
    private Boolean readed;
    private LocalDateTime eventDate;
    private String type;
    private String entityId;
    private String repairId;
    private String deviceId;
    private String clientId;
    private String clientName;
    private String clientLastName;
    private String clientPhone;
    private String clientEmail;
    private String deviceTypeName;
    private String deviceBrand;
    private String deviceModel;
    private String deviceSerialNumber;
    private String orderNumber;
    private String repairDescription;
    private RepairStatusEnum status;
    private LocalDateTime receiveDateTime;
    private LocalDateTime returnDateTime;
    private BigDecimal quotedAmount;
    private BigDecimal price;
    private String quoteNotes;
    private List<RepairPartDTO> parts;
    private String observationId;
    private String observationNote;
    private LocalDateTime observationObservedAt;
    private LocalDateTime observationFollowUpAt;
}
