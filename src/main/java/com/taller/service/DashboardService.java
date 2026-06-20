package com.taller.service;

import com.taller.model.enums.RepairStatusEnum;
import com.taller.model.repository.ClientRepository;
import com.taller.model.repository.DeviceRepository;
import com.taller.model.repository.RepairRepository;
import com.taller.model.repository.projection.ClientBasicView;
import com.taller.model.repository.projection.DeviceBasicView;
import com.taller.model.repository.projection.DeviceLastRepairView;
import com.taller.model.repository.projection.DeviceTypeCountView;
import com.taller.model.repository.projection.FinanceRepairView;
import com.taller.model.repository.projection.RepairListView;
import com.taller.model.repository.projection.RepairStatusCountView;
import com.taller.resource.dto.ClientDTO;
import com.taller.resource.dto.DashboardDTO;
import com.taller.resource.dto.DashboardInactiveDeviceDTO;
import com.taller.resource.dto.DashboardOverviewDTO;
import com.taller.resource.dto.DashboardRecentClientDTO;
import com.taller.resource.dto.DashboardRecentDeviceDTO;
import com.taller.resource.dto.DashboardRecentRepairDTO;
import com.taller.resource.dto.DashboardSeriesItemDTO;
import com.taller.resource.dto.DeviceDTO;
import com.taller.resource.dto.RepairDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final RepairService repairService;
    private final ClientRepository clientRepository;
    private final DeviceRepository deviceRepository;
    private final RepairRepository repairRepository;

    @Transactional(readOnly = true)
    public DashboardDTO monthSummary(int year, int month) {
        LocalDateTime from = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime to = from.plusMonths(1).minusSeconds(1);

        BigDecimal income = repairService.totalIncome(from, to);
        List<FinanceRepairView> repairs = repairRepository.findFinanceRowsBetween(from, to);
        BigDecimal costs = repairs.stream()
                .map(repair -> safeMoney(repair.getPartsCost()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DashboardDTO.builder()
                .totalRecaudacion(income)
                .totalCostos(costs)
                .totalGanancia(income.subtract(costs))
                .build();
    }

    @Transactional(readOnly = true)
    public List<ClientDTO> latestClientsWithDevices() {
        return clientRepository.findTop5WithDevicesBasic(PageRequest.of(0, 5)).stream().map(c -> {
            ClientDTO dto = new ClientDTO();
            dto.setId(c.getId()); dto.setName(c.getName()); dto.setLastName(c.getLastName()); dto.setDni(c.getDni()); dto.setEmail(c.getEmail()); dto.setPhone(c.getPhone());
            return dto;
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<DeviceDTO> latestDevices() {
        return deviceRepository.findBasicLatest(PageRequest.of(0, 5)).stream().map(d -> {
            DeviceDTO dto = new DeviceDTO();
            dto.setId(d.getId()); dto.setClientId(d.getClientId()); dto.setBrand(d.getBrand()); dto.setModel(d.getModel()); dto.setSerialNumber(d.getSerialNumber()); dto.setDeviceTypeId(d.getDeviceTypeId()); dto.setDeviceTypeName(d.getDeviceTypeName());
            return dto;
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<RepairDTO> latestRepairs() {
        return repairRepository.findLatestRows(PageRequest.of(0, 5)).stream().map(r -> {
            RepairDTO dto = new RepairDTO();
            dto.setId(r.getId()); dto.setIdClient(r.getIdClient()); dto.setOrderNumber(r.getOrderNumber()); dto.setStatus(r.getStatus()); dto.setPrice(r.getPrice()); dto.setReceiveDateTime(r.getReceiveDateTime());
            return dto;
        }).toList();
    }

    @Transactional(readOnly = true)
    public DashboardOverviewDTO overview() {
        DashboardOverviewDTO dto = new DashboardOverviewDTO();
        dto.setClientCount(clientRepository.count());
        dto.setDeviceCount(deviceRepository.count());
        dto.setRepairCount(repairRepository.count());

        Map<RepairStatusEnum, Long> statusCounts = new EnumMap<>(RepairStatusEnum.class);
        for (RepairStatusCountView countView : repairRepository.countByStatus()) {
            statusCounts.put(countView.getStatus(), countView.getTotal());
        }
        dto.setWaitingPickupCount(statusCounts.getOrDefault(RepairStatusEnum.ESPERANDO_RETIRO, 0L));
        dto.setInProgressCount(
                statusCounts.getOrDefault(RepairStatusEnum.HACIENDO, 0L)
                        + statusCounts.getOrDefault(RepairStatusEnum.RECIBIDA, 0L)
        );
        dto.setQuotedPendingCount(statusCounts.getOrDefault(RepairStatusEnum.PRESUPUESTADA_ESPERANDO_RESPUESTA, 0L));
        dto.setRepairStatuses(statusCounts.entrySet().stream()
                .map(entry -> new DashboardSeriesItemDTO(statusLabel(entry.getKey()), entry.getValue()))
                .sorted((left, right) -> Long.compare(((Number) right.getValue()).longValue(), ((Number) left.getValue()).longValue()))
                .toList());

        List<DeviceTypeCountView> deviceTypeCounts = deviceRepository.countByDeviceType();
        dto.setDeviceTypes(deviceTypeCounts.stream()
                .map(entry -> new DashboardSeriesItemDTO(entry.getDeviceTypeName(), entry.getTotal()))
                .toList());

        List<DashboardRecentClientDTO> latestClients = clientRepository.findTop5WithDevicesBasic(PageRequest.of(0, 5)).stream()
                .map(client -> {
                    DashboardRecentClientDTO clientDto = new DashboardRecentClientDTO();
                    clientDto.setId(client.getId());
                    clientDto.setName((client.getName() + " " + client.getLastName()).trim());
                    clientDto.setDeviceType("-");
                    return clientDto;
                })
                .toList();

        List<DeviceBasicView> latestDevices = deviceRepository.findBasicLatest(PageRequest.of(0, 5));
        Map<String, String> firstDeviceTypeByClient = new LinkedHashMap<>();
        for (DeviceBasicView device : latestDevices) {
            firstDeviceTypeByClient.putIfAbsent(device.getClientId(), device.getDeviceTypeName());
        }
        dto.setRecentClients(latestClients.stream()
                .peek(client -> client.setDeviceType(firstDeviceTypeByClient.getOrDefault(client.getId(), "-")))
                .toList());

        dto.setRecentDevices(latestDevices.stream().map(device -> {
            DashboardRecentDeviceDTO deviceDto = new DashboardRecentDeviceDTO();
            deviceDto.setId(device.getId());
            deviceDto.setDeviceTypeName(device.getDeviceTypeName());
            deviceDto.setBrand(device.getBrand());
            deviceDto.setModel(device.getModel());
            return deviceDto;
        }).toList());

        List<RepairListView> latestDeliveredRepairs = repairRepository.findLatestDeliveredRows(PageRequest.of(0, 5));
        Map<String, ClientBasicView> clientsById = clientRepository.findBasicByIdIn(
                latestDeliveredRepairs.stream()
                        .map(RepairListView::getIdClient)
                        .filter(id -> id != null && !id.isBlank())
                        .collect(Collectors.toSet())
        ).stream().collect(Collectors.toMap(ClientBasicView::getId, client -> client));

        dto.setRecentRepairs(latestDeliveredRepairs.stream()
                .map(repair -> {
                    DashboardRecentRepairDTO repairDto = new DashboardRecentRepairDTO();
                    repairDto.setRepairId(repair.getId());
                    LocalDateTime repairDate = repair.getReturnDateTime() != null ? repair.getReturnDateTime() : repair.getReceiveDateTime();
                    repairDto.setDate(repairDate != null ? repairDate.toLocalDate().toString() : "-");
                    ClientBasicView client = clientsById.get(repair.getIdClient());
                    repairDto.setClient(client != null ? (client.getName() + " " + client.getLastName()).trim() : repair.getIdClient());
                    repairDto.setPrice(repair.getPrice());
                    return repairDto;
                })
                .toList());

        List<DeviceLastRepairView> inactiveViews = repairRepository.findOldestLastRepairByDevice(PageRequest.of(0, 5));
        Map<String, DeviceBasicView> inactiveDevicesById = deviceRepository.findBasicByIdIn(
                inactiveViews.stream().map(DeviceLastRepairView::getDeviceId).toList()
        ).stream().collect(Collectors.toMap(DeviceBasicView::getId, device -> device));
        Map<String, ClientBasicView> inactiveClientsById = clientRepository.findBasicByIdIn(
                inactiveDevicesById.values().stream().map(DeviceBasicView::getClientId).collect(Collectors.toSet())
        ).stream().collect(Collectors.toMap(ClientBasicView::getId, client -> client));
        dto.setInactiveDevices(inactiveViews.stream().map(view -> {
            DeviceBasicView device = inactiveDevicesById.get(view.getDeviceId());
            ClientBasicView client = device != null ? inactiveClientsById.get(device.getClientId()) : null;
            DashboardInactiveDeviceDTO item = new DashboardInactiveDeviceDTO();
            String deviceLabel = device != null
                    ? (nullSafe(device.getDeviceTypeName()) + " " + nullSafe(device.getBrand()) + " " + nullSafe(device.getModel())).replaceAll("\\s+", " ").trim()
                    : view.getDeviceId();
            String ownerLabel = client != null ? (client.getName() + " " + client.getLastName()).trim() : "Cliente sin datos";
            item.setName(deviceLabel + " · " + ownerLabel);
            item.setLastRepair(view.getLastRepairDate() != null ? view.getLastRepairDate().toLocalDate().toString() : null);
            return item;
        }).toList());

        return dto;
    }

    private String statusLabel(RepairStatusEnum status) {
        return status != null ? status.getLabel() : "-";
    }

    private BigDecimal safeMoney(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private String nullSafe(String value) {
        return value != null ? value : "";
    }
}
