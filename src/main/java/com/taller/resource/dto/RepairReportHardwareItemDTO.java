package com.taller.resource.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepairReportHardwareItemDTO {
    private String id;
    private String partName;
    private Integer quantity;
    private String detail;
    private BigDecimal unitPrice;
    private Boolean includePrice;
}
