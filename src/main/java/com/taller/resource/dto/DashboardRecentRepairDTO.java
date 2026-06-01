package com.taller.resource.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DashboardRecentRepairDTO {
    private String repairId;
    private String date;
    private String client;
    private BigDecimal price;
}
