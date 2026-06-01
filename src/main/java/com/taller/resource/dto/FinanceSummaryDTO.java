package com.taller.resource.dto;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class FinanceSummaryDTO {
    private LocalDate from;
    private LocalDate to;
    private int repairCount;
    private BigDecimal totalIncome;
    private BigDecimal totalPartsCost;
    private BigDecimal totalLabor;
    private BigDecimal totalQuoted;
    private BigDecimal netIncome;
    private BigDecimal averageNet;
    private long deliveredCount;
    private List<DashboardSeriesItemDTO> monthlyNet;
    private List<FinanceRowDTO> rows;

    public LocalDate getFrom() {
        return this.from;
    }

    public LocalDate getTo() {
        return this.to;
    }

    public int getRepairCount() {
        return this.repairCount;
    }

    public BigDecimal getTotalIncome() {
        return this.totalIncome;
    }

    public BigDecimal getTotalPartsCost() {
        return this.totalPartsCost;
    }

    public BigDecimal getTotalLabor() {
        return this.totalLabor;
    }

    public BigDecimal getTotalQuoted() {
        return this.totalQuoted;
    }

    public BigDecimal getNetIncome() {
        return this.netIncome;
    }

    public BigDecimal getAverageNet() {
        return this.averageNet;
    }

    public long getDeliveredCount() {
        return this.deliveredCount;
    }

    public List<DashboardSeriesItemDTO> getMonthlyNet() {
        return this.monthlyNet;
    }

    public List<FinanceRowDTO> getRows() {
        return this.rows;
    }

    public void setFrom(LocalDate from) {
        this.from = from;
    }

    public void setTo(LocalDate to) {
        this.to = to;
    }

    public void setRepairCount(int repairCount) {
        this.repairCount = repairCount;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public void setTotalPartsCost(BigDecimal totalPartsCost) {
        this.totalPartsCost = totalPartsCost;
    }

    public void setTotalLabor(BigDecimal totalLabor) {
        this.totalLabor = totalLabor;
    }

    public void setTotalQuoted(BigDecimal totalQuoted) {
        this.totalQuoted = totalQuoted;
    }

    public void setNetIncome(BigDecimal netIncome) {
        this.netIncome = netIncome;
    }

    public void setAverageNet(BigDecimal averageNet) {
        this.averageNet = averageNet;
    }

    public void setDeliveredCount(long deliveredCount) {
        this.deliveredCount = deliveredCount;
    }

    public void setMonthlyNet(List<DashboardSeriesItemDTO> monthlyNet) {
        this.monthlyNet = monthlyNet;
    }

    public void setRows(List<FinanceRowDTO> rows) {
        this.rows = rows;
    }
}
