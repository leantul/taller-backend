package com.taller.resource.mapper;

import com.taller.model.Repair;
import com.taller.resource.dto.RepairDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RepairMapper {
    List<RepairDTO> repairToRepairDTOList(List<Repair> allRepairs);

    RepairDTO repairToRepairDTO(Repair repair);
}
