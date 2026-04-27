package com.taller.resource.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DashboardDTO {
    private BigDecimal totalRecaudacion;
    private BigDecimal totalCostos;
    private BigDecimal totalGanancia;
}
