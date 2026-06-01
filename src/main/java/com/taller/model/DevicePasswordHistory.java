package com.taller.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Table(name = "device_password_history")
@Entity
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

    public DevicePasswordHistory() {
    }

    public DevicePasswordHistory(String deviceId, String passwordValue, Boolean isCurrent, Device device) {
        this.deviceId = deviceId;
        this.passwordValue = passwordValue;
        this.isCurrent = isCurrent;
        this.device = device;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public String getPasswordValue() {
        return this.passwordValue;
    }

    public Boolean getIsCurrent() {
        return this.isCurrent;
    }

    public Device getDevice() {
        return this.device;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setPasswordValue(String passwordValue) {
        this.passwordValue = passwordValue;
    }

    public void setIsCurrent(Boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public static DevicePasswordHistoryBuilder builder() {
        return new DevicePasswordHistoryBuilder();
    }

    public static class DevicePasswordHistoryBuilder {
        private String deviceId;
        private String passwordValue;
        private Boolean isCurrent;
        private Device device;

        public DevicePasswordHistoryBuilder deviceId(String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        public DevicePasswordHistoryBuilder passwordValue(String passwordValue) {
            this.passwordValue = passwordValue;
            return this;
        }

        public DevicePasswordHistoryBuilder isCurrent(Boolean isCurrent) {
            this.isCurrent = isCurrent;
            return this;
        }

        public DevicePasswordHistoryBuilder device(Device device) {
            this.device = device;
            return this;
        }

        public DevicePasswordHistory build() {
            return new DevicePasswordHistory(deviceId, passwordValue, isCurrent, device);
        }
    }
}
