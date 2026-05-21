package com.taller.model.repository.projection;

import com.taller.model.enums.DeviceTypeEnum;

public interface DeviceListView {
    String getId();
    String getBrand();
    String getModel();
    String getSerialNumber();
    DeviceTypeEnum getDeviceType();
    String getClientId();
    String getCurrentPassword();
}
