package com.taller.resource.mapper;

import com.taller.model.DeviceObservation;
import com.taller.resource.dto.DeviceObservationDTO;

public final class DeviceObservationMapper {

    private DeviceObservationMapper() {
    }

    public static DeviceObservationDTO toDto(DeviceObservation observation) {
        DeviceObservationDTO dto = new DeviceObservationDTO();
        dto.setId(observation.getId());
        dto.setDeviceId(observation.getDeviceId());
        dto.setRepairId(observation.getRepairId());
        dto.setNote(observation.getNote());
        dto.setObservedAt(observation.getObservedAt());
        dto.setFollowUpAt(observation.getFollowUpAt());
        dto.setResolvedAt(observation.getResolvedAt());
        return dto;
    }
}
