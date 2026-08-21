package com.taller.resource.mapper;

import com.taller.model.RepairPart;
import com.taller.resource.dto.RepairPartDTO;

public final class RepairPartMapper {

    private RepairPartMapper() {
    }

    public static RepairPartDTO toDto(RepairPart part) {
        RepairPartDTO dto = new RepairPartDTO();
        dto.setId(part.getId());
        dto.setRepairId(part.getRepairId());
        dto.setName(part.getName());
        dto.setQuantity(part.getQuantity());
        dto.setProvider(part.getProvider());
        dto.setCost(part.getCost());
        dto.setSalePrice(part.getSalePrice());
        return dto;
    }
}
