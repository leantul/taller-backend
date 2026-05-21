package com.taller.model.repository.projection;

import com.taller.model.enums.DeviceTypeEnum;

public interface DeviceTypeCountView {
    DeviceTypeEnum getDeviceType();
    Long getTotal();
}
