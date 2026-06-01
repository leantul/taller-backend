package com.taller.model;

import com.taller.model.enums.CurrencyEnum;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "repair_payments")
@AttributeOverride(name = "id", column = @Column(name = "id_repair_payment"))
public class RepairPayment extends BasicEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repair_id", insertable = false, updatable = false)
    private Repair repair;

    @Column(name = "repair_id")
    private String repairId;

    @Column(name = "amount")
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency")
    private CurrencyEnum currency;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "notes")
    private String notes;

    public RepairPayment() {
    }

    public RepairPayment(Repair repair, String repairId, BigDecimal amount, CurrencyEnum currency, LocalDateTime paymentDate, String notes) {
        this.repair = repair;
        this.repairId = repairId;
        this.amount = amount;
        this.currency = currency;
        this.paymentDate = paymentDate;
        this.notes = notes;
    }

    public Repair getRepair() {
        return this.repair;
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

    public void setRepair(Repair repair) {
        this.repair = repair;
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

    public static RepairPaymentBuilder builder() {
        return new RepairPaymentBuilder();
    }

    public static class RepairPaymentBuilder {
        private Repair repair;
        private String repairId;
        private BigDecimal amount;
        private CurrencyEnum currency;
        private LocalDateTime paymentDate;
        private String notes;

        public RepairPaymentBuilder repair(Repair repair) {
            this.repair = repair;
            return this;
        }

        public RepairPaymentBuilder repairId(String repairId) {
            this.repairId = repairId;
            return this;
        }

        public RepairPaymentBuilder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public RepairPaymentBuilder currency(CurrencyEnum currency) {
            this.currency = currency;
            return this;
        }

        public RepairPaymentBuilder paymentDate(LocalDateTime paymentDate) {
            this.paymentDate = paymentDate;
            return this;
        }

        public RepairPaymentBuilder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public RepairPayment build() {
            return new RepairPayment(repair, repairId, amount, currency, paymentDate, notes);
        }
    }
}
