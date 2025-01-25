package com.taller.service;

import com.taller.model.Repair;
import com.taller.model.repository.RepairRepository;
import com.taller.resource.dto.RepairDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RepairService {

    private final RepairRepository repairRepository;

    public List<Repair> getAllRepairs() {
      return repairRepository.findAll();
    }

    public Repair getRepairById(String id) {
        return repairRepository.findById(id).orElse(null);
    }

    public void save(RepairDTO repairDTO) {
        Repair repair = Repair.builder()
                .idDevice(repairDTO.getDevice().getId())
                .idClient(repairDTO.getClient().getId())
                .description(repairDTO.getDescription())
                .status(repairDTO.getStatus())
                .receiveDateTime(repairDTO.getReceiveDateTime())
                .returnDateTime(repairDTO.getReturnDateTime())
                .price(repairDTO.getPrice())
                .build();

        repairRepository.save(repair);
    }

    public void delete(String id) {
        repairRepository.deleteById(id);
    }
}
