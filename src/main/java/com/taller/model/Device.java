package com.taller.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@Table(name = "devices", indexes = {
        @Index(name = "idx_devices_client_id", columnList = "client_id"),
        @Index(name = "idx_devices_device_type_id", columnList = "device_type_id")
})
@Entity
@AllArgsConstructor
@NoArgsConstructor
@AttributeOverride(name = "id", column = @Column(name = "id_device"))
public class Device extends BasicEntity {
    @Column(name = "brand")
    private String brand;

    @Column(name = "model")
    private String model;

    @Column(name = "serial_number")
    private String serialNumber;

    @Column(name = "device_type_id")
    private String deviceTypeId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "device_type_id", insertable = false, updatable = false)
    private DeviceType deviceType;

    @Column(name = "password")
    private String password;

    @Column(name = "technical_details", columnDefinition = "TEXT")
    private String technicalDetails;

    @Column(name = "client_id")
    private String clientId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", insertable = false, updatable = false)
    private Client client;
}
