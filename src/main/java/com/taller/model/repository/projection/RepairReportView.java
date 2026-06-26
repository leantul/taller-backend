package com.taller.model.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface RepairReportView {
    String getId();
    String getRepairId();
    String getOrderNumber();
    LocalDateTime getIssuedAt();
    String getClientName();
    String getClientLastName();
    String getClientPhone();
    String getClientEmail();
    String getDeviceTypeName();
    String getDeviceBrand();
    String getDeviceModel();
    String getDeviceSerialNumber();
    String getReportedIssue();
    String getWorkPerformed();
    String getFinalObservations();
    Boolean getShowPartPrices();
    BigDecimal getFinalAmount();
}
