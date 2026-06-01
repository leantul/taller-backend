package com.taller.resource.dto;


import java.time.LocalDateTime;

public class DevicePasswordHistoryDTO {
    private String id;
    private String value;
    private Boolean isCurrent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getId() {
        return this.id;
    }

    public String getValue() {
        return this.value;
    }

    public Boolean getIsCurrent() {
        return this.isCurrent;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setIsCurrent(Boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
