package com.taller.model;

import com.taller.model.enums.DeviceTypeEnum;
import jakarta.persistence.*;

@Table(name = "devices")
@Entity
@AttributeOverride(name = "id", column = @Column(name = "id_device"))
public class Device extends BasicEntity {
    @Column(name = "brand")
    private String brand;

    @Column(name = "model")
    private String model;

    @Column(name = "serial_number")
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type")
    private DeviceTypeEnum deviceType;

    @Column(name = "password")
    private String password;

    @Column(name = "accessories")
    private String accessories;

    @Column(name = "aesthetic_condition")
    private String aestheticCondition;

    @Column(name = "client_id")
    private String clientId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", insertable = false, updatable = false)
    private Client client;

    public Device() {
    }

    public Device(String brand, String model, String serialNumber, DeviceTypeEnum deviceType, String password, String accessories, String aestheticCondition, String clientId, Client client) {
        this.brand = brand;
        this.model = model;
        this.serialNumber = serialNumber;
        this.deviceType = deviceType;
        this.password = password;
        this.accessories = accessories;
        this.aestheticCondition = aestheticCondition;
        this.clientId = clientId;
        this.client = client;
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

    public String getPassword() {
        return this.password;
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

    public Client getClient() {
        return this.client;
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

    public void setPassword(String password) {
        this.password = password;
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

    public void setClient(Client client) {
        this.client = client;
    }

    public static DeviceBuilder builder() {
        return new DeviceBuilder();
    }

    public static class DeviceBuilder {
        private String brand;
        private String model;
        private String serialNumber;
        private DeviceTypeEnum deviceType;
        private String password;
        private String accessories;
        private String aestheticCondition;
        private String clientId;
        private Client client;

        public DeviceBuilder brand(String brand) {
            this.brand = brand;
            return this;
        }

        public DeviceBuilder model(String model) {
            this.model = model;
            return this;
        }

        public DeviceBuilder serialNumber(String serialNumber) {
            this.serialNumber = serialNumber;
            return this;
        }

        public DeviceBuilder deviceType(DeviceTypeEnum deviceType) {
            this.deviceType = deviceType;
            return this;
        }

        public DeviceBuilder password(String password) {
            this.password = password;
            return this;
        }

        public DeviceBuilder accessories(String accessories) {
            this.accessories = accessories;
            return this;
        }

        public DeviceBuilder aestheticCondition(String aestheticCondition) {
            this.aestheticCondition = aestheticCondition;
            return this;
        }

        public DeviceBuilder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public DeviceBuilder client(Client client) {
            this.client = client;
            return this;
        }

        public Device build() {
            return new Device(brand, model, serialNumber, deviceType, password, accessories, aestheticCondition, clientId, client);
        }
    }
}
