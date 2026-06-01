package com.taller.resource.dto;

import com.taller.model.enums.CurrencyEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RepairPaymentDTO {
    private String id;
    private String repairId;
    private BigDecimal amount;
    private CurrencyEnum currency;
    private LocalDateTime paymentDate;
    private String notes;

    public String getId() {
        return this.id;
    }

    public String getRepairId() {
        return this.repairId;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public CurrencyEnum getCurrency() {
        return this.currency;
    }

    public LocalDateTime getPaymentDate() {
        return this.paymentDate;
    }

    public String getNotes() {
        return this.notes;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setRepairId(String repairId) {
        this.repairId = repairId;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setCurrency(CurrencyEnum currency) {
        this.currency = currency;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
