package com.taller.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@Entity
@Table(name = "repair_parts")
@AllArgsConstructor
@NoArgsConstructor
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
}
