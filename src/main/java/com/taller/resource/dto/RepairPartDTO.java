package com.taller.resource.dto;


import java.math.BigDecimal;

public class RepairPartDTO {
    private String id;
    private String repairId;
    private String name;
    private Integer quantity;
    private String provider;
    private BigDecimal cost;
    private BigDecimal salePrice;

    public String getId() {
        return this.id;
    }

    public String getRepairId() {
        return this.repairId;
    }

    public String getName() {
        return this.name;
    }

    public Integer getQuantity() {
        return this.quantity;
    }

    public String getProvider() {
        return this.provider;
    }

    public BigDecimal getCost() {
        return this.cost;
    }

    public BigDecimal getSalePrice() {
        return this.salePrice;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setRepairId(String repairId) {
        this.repairId = repairId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }
}
