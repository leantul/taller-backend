package com.taller.resource.dto;

import com.taller.model.enums.RepairStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FinanceRowDTO {
    private String repairId;
    private String orderNumber;
    private LocalDateTime date;
    private RepairStatusEnum status;
    private BigDecimal income;
    private BigDecimal partsCost;
    private BigDecimal net;

    public String getRepairId() {
        return this.repairId;
    }

    public String getOrderNumber() {
        return this.orderNumber;
    }

    public LocalDateTime getDate() {
        return this.date;
    }

    public RepairStatusEnum getStatus() {
        return this.status;
    }

    public BigDecimal getIncome() {
        return this.income;
    }

    public BigDecimal getPartsCost() {
        return this.partsCost;
    }

    public BigDecimal getNet() {
        return this.net;
    }

    public void setRepairId(String repairId) {
        this.repairId = repairId;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public void setStatus(RepairStatusEnum status) {
        this.status = status;
    }

    public void setIncome(BigDecimal income) {
        this.income = income;
    }

    public void setPartsCost(BigDecimal partsCost) {
        this.partsCost = partsCost;
    }

    public void setNet(BigDecimal net) {
        this.net = net;
    }
}
