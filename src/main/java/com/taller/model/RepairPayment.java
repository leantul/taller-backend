package com.taller.model;

import com.taller.model.enums.CurrencyEnum;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Entity
@Table(name = "repair_payments", indexes = {
        @Index(name = "idx_repair_payments_repair_id", columnList = "repair_id"),
        @Index(name = "idx_repair_payments_payment_date", columnList = "payment_date"),
        @Index(name = "idx_repair_payments_repair_date", columnList = "repair_id,payment_date")
})
@AllArgsConstructor
@NoArgsConstructor
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
}
