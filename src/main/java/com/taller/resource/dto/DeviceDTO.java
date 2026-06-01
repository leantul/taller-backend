package com.taller.resource.dto;

import com.taller.model.enums.DeviceTypeEnum;

import java.util.List;

public class DeviceDTO {
    private String id;
    private String brand;
    private String model;
    private String serialNumber;
    private DeviceTypeEnum deviceType;
    private String currentPassword;
    private String accessories;
    private String aestheticCondition;
    private String clientId;
    private List<DevicePasswordHistoryDTO> passwordHistory;
    private List<DeviceObservationDTO> observations;

    public String getId() {
        return this.id;
    }

    public String getBrand() {
        return this.brand;
    }

    public String getModel() {
        return this.model;
    }

    public String getSerialNumber() {
        return this.serialNumber;
    }

    public DeviceTypeEnum getDeviceType() {
        return this.deviceType;
    }

    public String getCurrentPassword() {
        return this.currentPassword;
    }

    public String getAccessories() {
        return this.accessories;
    }

    public String getAestheticCondition() {
        return this.aestheticCondition;
    }

    public String getClientId() {
        return this.clientId;
    }

    public List<DevicePasswordHistoryDTO> getPasswordHistory() {
        return this.passwordHistory;
    }

    public List<DeviceObservationDTO> getObservations() {
        return this.observations;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void setDeviceType(DeviceTypeEnum deviceType) {
        this.deviceType = deviceType;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public void setAccessories(String accessories) {
        this.accessories = accessories;
    }

    public void setAestheticCondition(String aestheticCondition) {
        this.aestheticCondition = aestheticCondition;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setPasswordHistory(List<DevicePasswordHistoryDTO> passwordHistory) {
        this.passwordHistory = passwordHistory;
    }

    public void setObservations(List<DeviceObservationDTO> observations) {
        this.observations = observations;
    }
}
