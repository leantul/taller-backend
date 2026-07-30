package com.taller.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@Table(name = "device_password_history", indexes = {
        @Index(name = "idx_device_password_history_device_id", columnList = "device_id")
})
@Entity
@AllArgsConstructor
@NoArgsConstructor
@AttributeOverride(name = "id", column = @Column(name = "id_device_password_history", length = 64))
public class DevicePasswordHistory extends BasicEntity {

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(name = "password_value", nullable = false, length = 255)
    private String passwordValue;

    @Column(name = "is_current", nullable = false)
    private Boolean isCurrent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", referencedColumnName = "id_device", insertable = false, updatable = false)
    private Device device;
}
