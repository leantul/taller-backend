package com.taller.model.repository.projection;

public interface DeviceListView {
    String getId();
    String getBrand();
    String getModel();
    String getSerialNumber();
    String getDeviceTypeId();
    String getDeviceTypeName();
    String getClientId();
    String getCurrentPassword();
}
