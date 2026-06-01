package com.taller.resource.dto;


import java.math.BigDecimal;

public class DashboardDTO {
    private BigDecimal totalRecaudacion;
    private BigDecimal totalCostos;
    private BigDecimal totalGanancia;

    public DashboardDTO(BigDecimal totalRecaudacion, BigDecimal totalCostos, BigDecimal totalGanancia) {
        this.totalRecaudacion = totalRecaudacion;
        this.totalCostos = totalCostos;
        this.totalGanancia = totalGanancia;
    }

    public BigDecimal getTotalRecaudacion() {
        return this.totalRecaudacion;
    }

    public BigDecimal getTotalCostos() {
        return this.totalCostos;
    }

    public BigDecimal getTotalGanancia() {
        return this.totalGanancia;
    }

    public static DashboardDTOBuilder builder() {
        return new DashboardDTOBuilder();
    }

    public static class DashboardDTOBuilder {
        private BigDecimal totalRecaudacion;
        private BigDecimal totalCostos;
        private BigDecimal totalGanancia;

        public DashboardDTOBuilder totalRecaudacion(BigDecimal totalRecaudacion) {
            this.totalRecaudacion = totalRecaudacion;
            return this;
        }

        public DashboardDTOBuilder totalCostos(BigDecimal totalCostos) {
            this.totalCostos = totalCostos;
            return this;
        }

        public DashboardDTOBuilder totalGanancia(BigDecimal totalGanancia) {
            this.totalGanancia = totalGanancia;
            return this;
        }

        public DashboardDTO build() {
            return new DashboardDTO(totalRecaudacion, totalCostos, totalGanancia);
        }
    }
}
