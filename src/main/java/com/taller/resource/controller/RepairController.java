package com.taller.resource.controller;

import com.taller.resource.dto.RepairDTO;
import com.taller.resource.dto.RepairStatusUpdateDTO;
import com.taller.resource.dto.StatusBoardRepairDTO;
import com.taller.service.RepairService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/repair")
@RequiredArgsConstructor
public class RepairController {

    private final RepairService repairService;

    @GetMapping
    public List<RepairDTO> getRepair() {
        return repairService.getAllRepairs();
    }

    @GetMapping("/status-board")
    public List<StatusBoardRepairDTO> getStatusBoard() {
        return repairService.getStatusBoard();
    }

    @GetMapping("/{id}")
    public RepairDTO getRepairById(@PathVariable String id) {
        return repairService.getRepairById(id);
    }

    @GetMapping("/search")
    public List<RepairDTO> search(@RequestParam String term) {
        return repairService.search(term);
    }

    @PostMapping
    public ResponseEntity<RepairDTO> saveRepair(@RequestBody RepairDTO repairDTO) {
        return new ResponseEntity<>(repairService.save(repairDTO), HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<RepairDTO> updateRepair(@RequestBody RepairDTO repairDTO) {
        return new ResponseEntity<>(repairService.save(repairDTO), HttpStatus.OK);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable String id, @RequestBody RepairStatusUpdateDTO request) {
        repairService.updateStatus(id, request.status());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRepair(@PathVariable String id) {
        repairService.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
