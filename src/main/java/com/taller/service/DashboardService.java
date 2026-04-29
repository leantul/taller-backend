package com.taller.service;

import com.taller.model.Device;
import com.taller.model.repository.ClientRepository;
import com.taller.model.repository.DeviceRepository;
import com.taller.model.repository.RepairPartRepository;
import com.taller.model.repository.RepairRepository;
import com.taller.resource.dto.ClientDTO;
import com.taller.resource.dto.DashboardDTO;
import com.taller.resource.dto.DeviceDTO;
import com.taller.resource.dto.RepairDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final RepairService repairService;
    private final RepairPartRepository repairPartRepository;
    private final ClientRepository clientRepository;
    private final DeviceRepository deviceRepository;
    private final RepairRepository repairRepository;

    public DashboardDTO monthSummary(int year, int month) {
        LocalDateTime from = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime to = from.plusMonths(1).minusSeconds(1);

        BigDecimal income = repairService.totalIncome(from, to);
        BigDecimal costs = repairPartRepository.findAll().stream()
                .map(com.taller.model.RepairPart::getCost)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DashboardDTO.builder()
                .totalRecaudacion(income)
                .totalCostos(costs)
                .totalGanancia(income.subtract(costs))
                .build();
    }

    public List<ClientDTO> latestClientsWithDevices() {
        return clientRepository.findTop5WithDevices(PageRequest.of(0, 5)).stream().map(c -> {
            ClientDTO dto = new ClientDTO();
            dto.setId(c.getId()); dto.setName(c.getName()); dto.setLastName(c.getLastName()); dto.setDni(c.getDni()); dto.setEmail(c.getEmail()); dto.setPhone(c.getPhone());
            return dto;
        }).toList();
    }

    public List<DeviceDTO> latestDevices() {
        return deviceRepository.findAll(PageRequest.of(0, 5)).stream().map(d -> {
            DeviceDTO dto = new DeviceDTO();
            dto.setId(d.getId()); dto.setClientId(d.getClientId()); dto.setBrand(d.getBrand()); dto.setModel(d.getModel()); dto.setSerialNumber(d.getSerialNumber()); dto.setDeviceType(d.getDeviceType());
            return dto;
        }).toList();
    }

    public List<RepairDTO> latestRepairs() {
        return repairRepository.findLatest(PageRequest.of(0, 5)).stream().map(r -> {
            RepairDTO dto = new RepairDTO();
            dto.setId(r.getId()); dto.setIdClient(r.getIdClient()); dto.setOrderNumber(r.getOrderNumber()); dto.setStatus(r.getStatus()); dto.setPrice(r.getPrice()); dto.setReceiveDateTime(r.getReceiveDateTime());
            return dto;
        }).toList();
    }
}
