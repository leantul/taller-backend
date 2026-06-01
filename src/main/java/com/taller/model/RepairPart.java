package com.taller.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "repair_parts")
@AttributeOverride(name = "id", column = @Column(name = "id_repair_part"))
public class RepairPart extends BasicEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repair_id", insertable = false, updatable = false)
    private Repair repair;

    @Column(name = "repair_id")
    private String repairId;

    @Column(name = "name")
    private String name;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "provider")
    private String provider;

    @Column(name = "cost")
    private BigDecimal cost;

    @Column(name = "sale_price")
    private BigDecimal salePrice;

    public RepairPart() {
    }

    public RepairPart(Repair repair, String repairId, String name, Integer quantity, String provider, BigDecimal cost, BigDecimal salePrice) {
        this.repair = repair;
        this.repairId = repairId;
        this.name = name;
        this.quantity = quantity;
        this.provider = provider;
        this.cost = cost;
        this.salePrice = salePrice;
    }

    public Repair getRepair() {
        return this.repair;
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

    public void setRepair(Repair repair) {
        this.repair = repair;
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

    public static RepairPartBuilder builder() {
        return new RepairPartBuilder();
    }

    public static class RepairPartBuilder {
        private Repair repair;
        private String repairId;
        private String name;
        private Integer quantity;
        private String provider;
        private BigDecimal cost;
        private BigDecimal salePrice;

        public RepairPartBuilder repair(Repair repair) {
            this.repair = repair;
            return this;
        }

        public RepairPartBuilder repairId(String repairId) {
            this.repairId = repairId;
            return this;
        }

        public RepairPartBuilder name(String name) {
            this.name = name;
            return this;
        }

        public RepairPartBuilder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }

        public RepairPartBuilder provider(String provider) {
            this.provider = provider;
            return this;
        }

        public RepairPartBuilder cost(BigDecimal cost) {
            this.cost = cost;
            return this;
        }

        public RepairPartBuilder salePrice(BigDecimal salePrice) {
            this.salePrice = salePrice;
            return this;
        }

        public RepairPart build() {
            return new RepairPart(repair, repairId, name, quantity, provider, cost, salePrice);
        }
    }
}
