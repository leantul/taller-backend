package com.taller.model.repository.projection;

import java.time.LocalDateTime;

public interface DeviceLastRepairView {
    String getDeviceId();
    LocalDateTime getLastRepairDate();
    String getDeviceTypeName();
    String getDeviceBrand();
    String getDeviceModel();
    String getClientName();
    String getClientLastName();
}
