package com.taller.resource.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class FinanceSummaryDTO {
    private LocalDate from;
    private LocalDate to;
    private int repairCount;
    private BigDecimal totalIncome;
    private BigDecimal totalPartsCost;
    private BigDecimal totalLabor;
    private BigDecimal totalQuoted;
    private long zeroFinalAmountCount;
    private long positiveFinalAmountCount;
    private BigDecimal netIncome;
    private BigDecimal averageNet;
    private long deliveredCount;
    private List<DashboardSeriesItemDTO> monthlyNet;
    private List<FinanceRowDTO> rows;
}
