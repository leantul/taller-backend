package com.taller.model.repository.projection;

import com.taller.model.enums.RepairStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface RepairListView {
    String getId();
    String getIdDevice();
    String getIdClient();
    String getDescription();
    String getOrderNumber();
    RepairStatusEnum getStatus();
    LocalDateTime getReceiveDateTime();
    LocalDateTime getReturnDateTime();
    BigDecimal getPrice();
    BigDecimal getLaborAmount();
    BigDecimal getExtraAmount();
    BigDecimal getQuotedAmount();
    String getQuoteNotes();
    Boolean getApproved();
    Boolean getRejected();
    LocalDateTime getReadyNotifiedAt();
    String getClientName();
    String getClientLastName();
    String getClientPhone();
    String getDeviceTypeName();
    String getDeviceBrand();
    String getDeviceModel();
}
