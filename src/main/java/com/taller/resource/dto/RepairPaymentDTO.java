package com.taller.resource.dto;

import com.taller.model.enums.CurrencyEnum;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class RepairPaymentDTO {
    private String id;
    private String repairId;
    private BigDecimal amount;
    private CurrencyEnum currency;
    private LocalDateTime paymentDate;
    private String notes;
}
