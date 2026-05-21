package com.taller.resource.dto;

import com.taller.model.enums.RepairStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class FinanceRowDTO {
    private String repairId;
    private String orderNumber;
    private LocalDateTime date;
    private RepairStatusEnum status;
    private BigDecimal income;
    private BigDecimal partsCost;
    private BigDecimal net;
}
