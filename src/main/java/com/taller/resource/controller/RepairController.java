package com.taller.resource.controller;

import com.taller.model.Repair;
import com.taller.resource.dto.RepairDTO;
import com.taller.resource.mapper.RepairMapper;
import com.taller.service.RepairService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(originPatterns = "*", maxAge = 3600)
@RestController
@RequestMapping("/repair")
@RequiredArgsConstructor
public class RepairController {

    private final RepairService repairService;
    private final RepairMapper repairMapper;

    @GetMapping
    public List<RepairDTO> getRepair() {
        return repairMapper.repairToRepairDTOList(repairService.getAllRepairs());
    }

    @GetMapping("/{id}")
    public RepairDTO getRepairById(@PathVariable String id) {
        return repairMapper.repairToRepairDTO(repairService.getRepairById(id));
    }

    @PostMapping
    public ResponseEntity<Repair> saveRepair(@RequestBody RepairDTO repairDTO) {
        repairService.save(repairDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<Repair> updateRepair(@RequestBody RepairDTO repairDTO) {
        Repair repair = repairService.getRepairById(repairDTO.getId());

        if (repair != null) {
            repairService.save(repairDTO);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Repair> deleteRepair(@PathVariable String id) {
        repairService.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
