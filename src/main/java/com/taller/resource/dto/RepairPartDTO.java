package com.taller.resource.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RepairPartDTO {
    private String id;
    private String repairId;
    private String name;
    private Integer quantity;
    private String provider;
    private BigDecimal cost;
    private BigDecimal salePrice;
}
