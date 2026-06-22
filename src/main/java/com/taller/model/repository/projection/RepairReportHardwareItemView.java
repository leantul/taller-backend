package com.taller.model.repository.projection;

import java.math.BigDecimal;

public interface RepairReportHardwareItemView {
    String getId();
    String getPartName();
    Integer getQuantity();
    String getDetail();
    BigDecimal getUnitPrice();
    Boolean getIncludePrice();
}
