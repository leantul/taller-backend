package com.taller.resource.controller;

import com.taller.resource.dto.PageDTO;
import com.taller.resource.dto.RepairReportDTO;
import com.taller.resource.dto.RepairDTO;
import com.taller.resource.dto.RepairStatusUpdateDTO;
import com.taller.resource.dto.StatusBoardRepairDTO;
import com.taller.service.DeliveryReportService;
import com.taller.service.RepairService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/repair")
@RequiredArgsConstructor
public class RepairController {

    private final RepairService repairService;
    private final DeliveryReportService deliveryReportService;

    @GetMapping
    public List<RepairDTO> getRepair() {
        return repairService.getAllRepairs();
    }

    @GetMapping("/page")
    public PageDTO<RepairDTO> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String term,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder) {
        return repairService.findPage(page, size, term, from, to, sortField, sortOrder);
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

    @GetMapping("/{id}/delivery-report")
    public RepairReportDTO getDeliveryReport(@PathVariable String id) {
        return deliveryReportService.getByRepairId(id);
    }

    @PutMapping("/{id}/delivery-report")
    public ResponseEntity<RepairReportDTO> saveDeliveryReport(@PathVariable String id, @RequestBody RepairReportDTO reportDTO) {
        return ResponseEntity.ok(deliveryReportService.save(id, reportDTO));
    }

    @GetMapping("/{id}/delivery-report/pdf")
    public ResponseEntity<byte[]> getDeliveryReportPdf(@PathVariable String id) {
        RepairDTO repair = repairService.getRepairById(id);
        String filename = "reporte-reparacion-" + (repair != null && repair.getOrderNumber() != null ? repair.getOrderNumber() : id) + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(deliveryReportService.generatePdf(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRepair(@PathVariable String id) {
        repairService.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
