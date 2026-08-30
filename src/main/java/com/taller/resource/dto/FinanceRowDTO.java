package com.taller.resource.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class FinanceRowDTO {
    private String repairId;
    private String clientName;
    private LocalDateTime date;
    private BigDecimal income;
    private BigDecimal partsSale;
    private BigDecimal net;
}
