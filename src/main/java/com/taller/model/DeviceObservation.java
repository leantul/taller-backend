package com.taller.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Entity
@Table(name = "device_observations", indexes = {
        @Index(name = "idx_device_observations_device_id", columnList = "device_id"),
        @Index(name = "idx_device_observations_repair_id", columnList = "repair_id"),
        @Index(name = "idx_device_observations_pending_follow_up", columnList = "resolved_at, follow_up_at")
})
@AllArgsConstructor
@NoArgsConstructor
@AttributeOverride(name = "id", column = @Column(name = "id_device_observation"))
public class DeviceObservation extends BasicEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", insertable = false, updatable = false)
    private Device device;

    @Column(name = "device_id")
    private String deviceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repair_id", insertable = false, updatable = false)
    private Repair repair;

    @Column(name = "repair_id")
    private String repairId;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "observed_at")
    private LocalDateTime observedAt;

    @Column(name = "follow_up_at")
    private LocalDateTime followUpAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}
