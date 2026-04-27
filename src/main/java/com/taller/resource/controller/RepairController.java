package com.taller.resource.controller;

import com.taller.resource.dto.RepairDTO;
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

    @GetMapping
    public List<RepairDTO> getRepair() {
        return repairService.getAllRepairs();
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRepair(@PathVariable String id) {
        repairService.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
