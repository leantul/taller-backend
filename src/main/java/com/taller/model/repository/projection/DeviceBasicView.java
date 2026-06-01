package com.taller.model.repository.projection;

import com.taller.model.enums.DeviceTypeEnum;

public interface DeviceBasicView {
    String getId();
    String getBrand();
    String getModel();
    String getSerialNumber();
    DeviceTypeEnum getDeviceType();
    String getClientId();
}
