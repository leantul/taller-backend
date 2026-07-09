package com.taller.model;

import com.taller.model.enums.RepairStatusEnum;
import com.taller.model.enums.converter.RepairStatusEnumConverter;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Entity
@Table(name = "repair_status_history")
@AllArgsConstructor
@NoArgsConstructor
@AttributeOverride(name = "id", column = @Column(name = "id_repair_status_history"))
public class RepairStatusHistory extends BasicEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repair_id", insertable = false, updatable = false)
    private Repair repair;

    @Column(name = "repair_id")
    private String repairId;

    @Column(name = "status")
    @Convert(converter = RepairStatusEnumConverter.class)
    private RepairStatusEnum status;

    @Column(name = "changed_at")
    private LocalDateTime changedAt;
}
