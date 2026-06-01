package com.taller.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "device_observations")
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

    public DeviceObservation() {
    }

    public DeviceObservation(Device device, String deviceId, Repair repair, String repairId, String note, LocalDateTime observedAt, LocalDateTime followUpAt, LocalDateTime resolvedAt) {
        this.device = device;
        this.deviceId = deviceId;
        this.repair = repair;
        this.repairId = repairId;
        this.note = note;
        this.observedAt = observedAt;
        this.followUpAt = followUpAt;
        this.resolvedAt = resolvedAt;
    }

    public Device getDevice() {
        return this.device;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public Repair getRepair() {
        return this.repair;
    }

    public String getRepairId() {
        return this.repairId;
    }

    public String getNote() {
        return this.note;
    }

    public LocalDateTime getObservedAt() {
        return this.observedAt;
    }

    public LocalDateTime getFollowUpAt() {
        return this.followUpAt;
    }

    public LocalDateTime getResolvedAt() {
        return this.resolvedAt;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setRepair(Repair repair) {
        this.repair = repair;
    }

    public void setRepairId(String repairId) {
        this.repairId = repairId;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setObservedAt(LocalDateTime observedAt) {
        this.observedAt = observedAt;
    }

    public void setFollowUpAt(LocalDateTime followUpAt) {
        this.followUpAt = followUpAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public static DeviceObservationBuilder builder() {
        return new DeviceObservationBuilder();
    }

    public static class DeviceObservationBuilder {
        private Device device;
        private String deviceId;
        private Repair repair;
        private String repairId;
        private String note;
        private LocalDateTime observedAt;
        private LocalDateTime followUpAt;
        private LocalDateTime resolvedAt;

        public DeviceObservationBuilder device(Device device) {
            this.device = device;
            return this;
        }

        public DeviceObservationBuilder deviceId(String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        public DeviceObservationBuilder repair(Repair repair) {
            this.repair = repair;
            return this;
        }

        public DeviceObservationBuilder repairId(String repairId) {
            this.repairId = repairId;
            return this;
        }

        public DeviceObservationBuilder note(String note) {
            this.note = note;
            return this;
        }

        public DeviceObservationBuilder observedAt(LocalDateTime observedAt) {
            this.observedAt = observedAt;
            return this;
        }

        public DeviceObservationBuilder followUpAt(LocalDateTime followUpAt) {
            this.followUpAt = followUpAt;
            return this;
        }

        public DeviceObservationBuilder resolvedAt(LocalDateTime resolvedAt) {
            this.resolvedAt = resolvedAt;
            return this;
        }

        public DeviceObservation build() {
            return new DeviceObservation(device, deviceId, repair, repairId, note, observedAt, followUpAt, resolvedAt);
        }
    }
}
