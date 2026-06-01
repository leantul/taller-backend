package com.taller.resource.dto;


import java.time.LocalDateTime;

public class DeviceObservationDTO {
    private String id;
    private String deviceId;
    private String repairId;
    private String note;
    private LocalDateTime observedAt;
    private LocalDateTime followUpAt;
    private LocalDateTime resolvedAt;

    public String getId() {
        return this.id;
    }

    public String getDeviceId() {
        return this.deviceId;
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

    public void setId(String id) {
        this.id = id;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
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
}
