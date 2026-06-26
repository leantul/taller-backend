package com.taller.model.repository.projection;

import java.math.BigDecimal;

public interface DeliveryReportSourceView {
    String getRepairId();
    String getOrderNumber();
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
    BigDecimal getFinalAmount();
}
