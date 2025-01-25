package com.taller.resource.mapper;

import com.taller.model.Device;
import com.taller.resource.dto.DeviceDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DeviceMapper {

    DeviceDTO deviceToDeviceDTO(Device device);

    List<DeviceDTO> devicesToDeviceDTOList(List<Device> allDevices);

    List<DeviceDTO> deviceToDeviceDTOList(List<Device> devicesByClientId);
}
