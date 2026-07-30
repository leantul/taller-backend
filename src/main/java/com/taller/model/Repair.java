package com.taller.model;

import com.taller.model.enums.RepairStatusEnum;
import com.taller.model.enums.converter.RepairStatusEnumConverter;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@Table(name = "repairs", indexes = {
        @Index(name = "idx_repairs_client_id", columnList = "id_client"),
        @Index(name = "idx_repairs_device_id", columnList = "id_device"),
        @Index(name = "idx_repairs_status_return_date", columnList = "status, return_date_time"),
        @Index(name = "idx_repairs_receive_date", columnList = "receive_date_time")
})
@Entity
@AllArgsConstructor
@NoArgsConstructor
@AttributeOverride(name = "id", column = @Column(name = "id_repair"))
public class Repair extends BasicEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_device", insertable = false, updatable = false)
    private Device device;

    @Column(name = "id_device")
    private String idDevice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_client", insertable = false, updatable = false)
    private Client client;

    @Column(name = "id_client")
    private String idClient;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "order_number", unique = true)
    private String orderNumber;

    @Column(name = "status")
    @Convert(converter = RepairStatusEnumConverter.class)
    private RepairStatusEnum status;

    @Column(name = "receive_date_time")
    private LocalDateTime receiveDateTime;

    @Column(name = "return_date_time")
    private LocalDateTime returnDateTime;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "labor_amount", nullable = false)
    private BigDecimal laborAmount;

    @Column(name = "extra_amount")
    private BigDecimal extraAmount;

    @Column(name = "quoted_amount")
    private BigDecimal quotedAmount;

    @Column(name = "quote_notes", columnDefinition = "TEXT")
    private String quoteNotes;

    @Column(name = "repair_notes", columnDefinition = "TEXT")
    private String repairNotes;

    @Column(name = "approved")
    private Boolean approved;

    @Column(name = "rejected")
    private Boolean rejected;

    @Column(name = "ready_notified_at")
    private LocalDateTime readyNotifiedAt;

    @OneToMany(mappedBy = "repair", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RepairPart> parts = new ArrayList<>();

    @OneToMany(mappedBy = "repair", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RepairPayment> payments = new ArrayList<>();
}
