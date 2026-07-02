package com.taller.service;

import com.taller.model.DeviceObservation;
import com.taller.model.Repair;
import com.taller.model.RepairPart;
import com.taller.model.RepairPayment;
import com.taller.model.enums.RepairStatusEnum;
import com.taller.model.repository.DeviceObservationRepository;
import com.taller.model.repository.RepairPaymentRepository;
import com.taller.model.repository.RepairPartRepository;
import com.taller.model.repository.RepairRepository;
import com.taller.model.repository.projection.FinanceRepairView;
import com.taller.model.repository.projection.RepairListView;
import com.taller.model.repository.projection.StatusBoardRepairView;
import com.taller.resource.dto.DeviceObservationDTO;
import com.taller.resource.dto.PageDTO;
import com.taller.resource.dto.RepairDTO;
import com.taller.resource.dto.RepairPartDTO;
import com.taller.resource.dto.RepairPaymentDTO;
import com.taller.resource.dto.StatusBoardRepairDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RepairService {

    private final RepairRepository repairRepository;
    private final RepairPartRepository repairPartRepository;
    private final RepairPaymentRepository repairPaymentRepository;
    private final DeviceObservationRepository deviceObservationRepository;

    @Transactional(readOnly = true)
    public List<RepairDTO> getAllRepairs() {
      return repairRepository.findListRows().stream().map(this::toListDto).toList();
    }

    @Transactional(readOnly = true)
    public PageDTO<RepairDTO> findPage(int page, int size, String term, LocalDateTime from, LocalDateTime to, RepairStatusEnum status, String sortField, String sortOrder) {
        Page<RepairListView> result = findRepairPage(normalizeTerm(term), status, from, to, pageRequest(page, size, 100, sortField, sortOrder));
        return toPage(result, result.getContent().stream().map(this::toListDto).toList());
    }

    @Transactional(readOnly = true)
    public List<StatusBoardRepairDTO> getStatusBoard() {
        return repairRepository.findStatusBoardRows().stream().map(this::toStatusBoardDto).toList();
    }

    @Transactional(readOnly = true)
    public RepairDTO getRepairById(String id) {
        return repairRepository.findById(id).map(this::toDto).orElse(null);
    }

    @Transactional
    public RepairDTO save(RepairDTO repairDTO) {
        Repair repair = repairDTO.getId() != null
                ? repairRepository.findById(repairDTO.getId()).orElseGet(Repair::new)
                : new Repair();
        boolean isNew = repair.getId() == null;

        repair.setIdDevice(resolveDeviceId(repairDTO));
        repair.setIdClient(resolveClientId(repairDTO));
        repair.setDescription(repairDTO.getDescription());
        if (isNew && (repairDTO.getOrderNumber() == null || repairDTO.getOrderNumber().isBlank())) {
            repair.setOrderNumber(nextOrderNumber());
        } else {
            repair.setOrderNumber(repairDTO.getOrderNumber());
        }
        repair.setStatus(repairDTO.getStatus());

        LocalDateTime receiveDateTime = repairDTO.getReceiveDateTime() != null
                ? repairDTO.getReceiveDateTime()
                : repair.getReceiveDateTime();
        if (receiveDateTime == null && isNew) {
            receiveDateTime = LocalDateTime.now();
        }
        repair.setReceiveDateTime(receiveDateTime);

        LocalDateTime returnDateTime = repairDTO.getReturnDateTime() != null
                ? repairDTO.getReturnDateTime()
                : repair.getReturnDateTime();
        if (returnDateTime == null && repairDTO.getStatus() == RepairStatusEnum.RETIRADA) {
            returnDateTime = LocalDateTime.now();
        }
        repair.setReturnDateTime(returnDateTime);
        repair.setPrice(repairDTO.getPrice());
        repair.setLaborAmount(repairDTO.getLaborAmount());
        repair.setExtraAmount(repairDTO.getExtraAmount());
        repair.setQuotedAmount(repairDTO.getQuotedAmount());
        repair.setQuoteNotes(repairDTO.getQuoteNotes());
        if (repairDTO.getRepairNotes() != null) {
            repair.setRepairNotes(normalizeOptionalText(repairDTO.getRepairNotes()));
        }
        repair.setApproved(repairDTO.getApproved());
        repair.setRejected(repairDTO.getRejected());
        repair.setReadyNotifiedAt(repairDTO.getReadyNotifiedAt());

        if (repairDTO.getId() != null) {
            repair.setId(repairDTO.getId());
        }

        Repair saved = repairRepository.save(repair);

        if (repairDTO.getParts() != null) {
            repairPartRepository.deleteAll(repairPartRepository.findByRepairId(saved.getId()));
            List<RepairPart> parts = repairDTO.getParts().stream().map(dto -> RepairPart.builder()
                    .repairId(saved.getId())
                    .name(dto.getName())
                    .quantity(dto.getQuantity())
                    .provider(dto.getProvider())
                    .cost(dto.getCost())
                    .salePrice(dto.getSalePrice())
                    .build()).toList();
            repairPartRepository.saveAll(parts);
        }

        if (repairDTO.getPayments() != null) {
            repairPaymentRepository.deleteAll(repairPaymentRepository.findByRepairId(saved.getId()));
            List<RepairPayment> payments = repairDTO.getPayments().stream().map(dto -> RepairPayment.builder()
                    .repairId(saved.getId())
                    .amount(dto.getAmount())
                    .currency(dto.getCurrency())
                    .paymentDate(dto.getPaymentDate())
                    .notes(dto.getNotes())
                    .build()).toList();
            repairPaymentRepository.saveAll(payments);
        }

        if (repairDTO.getObservations() != null) {
            syncObservations(saved, repairDTO.getObservations());
        }

        return toDto(saved);
    }

    @Transactional
    public void updateStatus(String id, RepairStatusEnum status) {
        updateStatus(id, status, null, null);
    }

    @Transactional
    public void updateStatus(String id, RepairStatusEnum status, LocalDateTime receiveDateTime, LocalDateTime returnDateTime) {
        Repair repair = repairRepository.findById(id).orElseThrow();
        repair.setStatus(status);
        if (status == RepairStatusEnum.RECIBIDA) {
            repair.setReceiveDateTime(receiveDateTime != null ? receiveDateTime : (repair.getReceiveDateTime() != null ? repair.getReceiveDateTime() : LocalDateTime.now()));
        }
        if (status == RepairStatusEnum.RETIRADA) {
            repair.setReturnDateTime(returnDateTime != null ? returnDateTime : (repair.getReturnDateTime() != null ? repair.getReturnDateTime() : LocalDateTime.now()));
        } else {
            repair.setReturnDateTime(null);
        }
        repairRepository.save(repair);
    }

    @Transactional
    public void delete(String id) {
        repairPartRepository.deleteAll(repairPartRepository.findByRepairId(id));
        repairPaymentRepository.deleteAll(repairPaymentRepository.findByRepairId(id));
        deviceObservationRepository.deleteAll(deviceObservationRepository.findByRepairId(id));
        repairRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<RepairDTO> search(String term) {
        return repairRepository.searchListRows(term).stream().map(this::toListDto).toList();
    }

    @Transactional(readOnly = true)
    public BigDecimal totalIncome(LocalDateTime from, LocalDateTime to) {
        List<FinanceRepairView> repairs = repairRepository.findFinanceRowsBetween(from, to);

        return repairs.stream()
                .map(repair -> {
                    BigDecimal laborIncome = repair.getLaborAmount() != null
                            ? repair.getLaborAmount()
                            : (repair.getPrice() != null ? repair.getPrice() : BigDecimal.ZERO);

                    return laborIncome.add(repair.getPartsProfit() != null ? repair.getPartsProfit() : BigDecimal.ZERO);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    private String nextOrderNumber() {
        Long value = repairRepository.nextOrderValue();
        return String.valueOf(value);
    }

    private RepairDTO toDto(Repair repair) {
        RepairDTO dto = new RepairDTO();
        dto.setId(repair.getId());
        dto.setIdDevice(repair.getIdDevice());
        dto.setIdClient(repair.getIdClient());
        dto.setDescription(repair.getDescription());
        dto.setOrderNumber(repair.getOrderNumber());
        dto.setStatus(repair.getStatus());
        dto.setReceiveDateTime(repair.getReceiveDateTime());
        dto.setReturnDateTime(repair.getReturnDateTime());
        dto.setPrice(repair.getPrice());
        dto.setLaborAmount(repair.getLaborAmount());
        dto.setExtraAmount(repair.getExtraAmount());
        dto.setQuotedAmount(repair.getQuotedAmount());
        dto.setQuoteNotes(repair.getQuoteNotes());
        dto.setRepairNotes(repair.getRepairNotes());
        dto.setApproved(repair.getApproved());
        dto.setRejected(repair.getRejected());
        dto.setReadyNotifiedAt(repair.getReadyNotifiedAt());
        dto.setParts(repairPartRepository.findByRepairId(repair.getId()).stream().map(this::toPartDto).toList());
        dto.setPayments(repairPaymentRepository.findByRepairId(repair.getId()).stream().map(this::toPaymentDto).toList());
        dto.setObservations(deviceObservationRepository.findByRepairIdOrderByObservedAtDesc(repair.getId()).stream().map(this::toObservationDto).toList());
        return dto;
    }

    private RepairDTO toListDto(RepairListView repair) {
        RepairDTO dto = new RepairDTO();
        dto.setId(repair.getId());
        dto.setIdDevice(repair.getIdDevice());
        dto.setIdClient(repair.getIdClient());
        dto.setDescription(repair.getDescription());
        dto.setOrderNumber(repair.getOrderNumber());
        dto.setStatus(repair.getStatus());
        dto.setReceiveDateTime(repair.getReceiveDateTime());
        dto.setReturnDateTime(repair.getReturnDateTime());
        dto.setPrice(repair.getPrice());
        dto.setLaborAmount(repair.getLaborAmount());
        dto.setExtraAmount(repair.getExtraAmount());
        dto.setQuotedAmount(repair.getQuotedAmount());
        dto.setQuoteNotes(repair.getQuoteNotes());
        dto.setApproved(repair.getApproved());
        dto.setRejected(repair.getRejected());
        dto.setReadyNotifiedAt(repair.getReadyNotifiedAt());
        dto.setClientName(joinLabel(repair.getClientName(), repair.getClientLastName()));
        dto.setClientPhone(repair.getClientPhone());
        dto.setDeviceLabel(joinLabel(defaultLabel(repair.getDeviceTypeName()), repair.getDeviceBrand(), repair.getDeviceModel()));
        return dto;
    }

    private StatusBoardRepairDTO toStatusBoardDto(StatusBoardRepairView repair) {
        String clientName = joinLabel(repair.getClientName(), repair.getClientLastName());
        String deviceLabel = joinLabel(defaultLabel(repair.getDeviceTypeName()), repair.getDeviceBrand(), repair.getDeviceModel());
        return new StatusBoardRepairDTO(
                repair.getId(), repair.getIdDevice(), repair.getIdClient(), repair.getOrderNumber(), repair.getDescription(),
                repair.getStatus(), repair.getReceiveDateTime(), repair.getReturnDateTime(), repair.getPrice(),
                repair.getQuotedAmount(), clientName, deviceLabel
        );
    }

    private String joinLabel(String... values) {
        return java.util.Arrays.stream(values)
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.joining(" "));
    }

    private String defaultLabel(String value) {
        return value != null && !value.isBlank() ? value : "-";
    }

    private RepairPartDTO toPartDto(RepairPart part) {
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

    private RepairPaymentDTO toPaymentDto(RepairPayment payment) {
        RepairPaymentDTO dto = new RepairPaymentDTO();
        dto.setId(payment.getId());
        dto.setRepairId(payment.getRepairId());
        dto.setAmount(payment.getAmount());
        dto.setCurrency(payment.getCurrency());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setNotes(payment.getNotes());
        return dto;
    }

    private void syncObservations(Repair repair, List<DeviceObservationDTO> observationDtos) {
        Map<String, DeviceObservation> existingById = deviceObservationRepository.findByRepairId(repair.getId()).stream()
                .filter(observation -> observation.getId() != null)
                .collect(Collectors.toMap(DeviceObservation::getId, observation -> observation));

        List<DeviceObservationDTO> validDtos = observationDtos.stream()
                .filter(dto -> dto.getNote() != null && !dto.getNote().isBlank())
                .toList();

        Set<String> incomingIds = validDtos.stream()
                .map(DeviceObservationDTO::getId)
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toSet());

        List<DeviceObservation> toDelete = existingById.values().stream()
                .filter(observation -> !incomingIds.contains(observation.getId()))
                .toList();
        deviceObservationRepository.deleteAll(toDelete);

        List<DeviceObservation> observations = validDtos.stream()
                .map(dto -> toObservation(repair, dto, existingById.getOrDefault(dto.getId(), new DeviceObservation())))
                .toList();
        deviceObservationRepository.saveAll(observations);
    }

    private DeviceObservation toObservation(Repair repair, DeviceObservationDTO dto, DeviceObservation observation) {
        LocalDateTime observedAt = dto.getObservedAt() != null
                ? dto.getObservedAt()
                : (observation.getObservedAt() != null ? observation.getObservedAt() : LocalDateTime.now());
        observation.setDeviceId(repair.getIdDevice());
        observation.setRepairId(repair.getId());
        observation.setNote(dto.getNote().trim());
        observation.setObservedAt(observedAt);
        observation.setFollowUpAt(dto.getFollowUpAt() != null ? dto.getFollowUpAt() : observedAt.plusMonths(3));
        observation.setResolvedAt(dto.getResolvedAt());
        return observation;
    }

    private DeviceObservationDTO toObservationDto(DeviceObservation observation) {
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

    private String resolveDeviceId(RepairDTO repairDTO) {
        if (repairDTO.getIdDevice() != null) {
            return repairDTO.getIdDevice();
        }
        return repairDTO.getDevice() != null ? repairDTO.getDevice().getId() : null;
    }

    private String resolveClientId(RepairDTO repairDTO) {
        if (repairDTO.getIdClient() != null) {
            return repairDTO.getIdClient();
        }
        return repairDTO.getClient() != null ? repairDTO.getClient().getId() : null;
    }

    private <T> PageDTO<T> toPage(Page<?> page, List<T> content) {
        return new PageDTO<>(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    private String normalizeTerm(String term) {
        return term == null ? "" : term.trim();
    }

    private PageRequest pageRequest(int page, int size, int maximumSize, String sortField, String sortOrder) {
        return PageRequest.of(
                Math.max(0, page),
                Math.min(Math.max(1, size), maximumSize),
                resolveSort(sortField, sortOrder)
        );
    }

    private Page<RepairListView> findRepairPage(String term, RepairStatusEnum status, LocalDateTime from, LocalDateTime to, PageRequest pageRequest) {
        return repairRepository.findPageFiltered(term, status, from, to, pageRequest);
    }

    private Sort resolveSort(String sortField, String sortOrder) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return switch (sortField == null ? "" : sortField.trim()) {
            case "clientName" -> Sort.by(
                    new Sort.Order(direction, "client.name").ignoreCase(),
                    new Sort.Order(direction, "client.lastName").ignoreCase(),
                    new Sort.Order(Sort.Direction.DESC, "orderNumber")
            );
            case "deviceLabel" -> Sort.by(
                    new Sort.Order(direction, "device.brand").ignoreCase(),
                    new Sort.Order(direction, "device.model").ignoreCase(),
                    new Sort.Order(Sort.Direction.DESC, "orderNumber")
            );
            case "price" -> Sort.by(
                    new Sort.Order(direction, "price"),
                    new Sort.Order(Sort.Direction.DESC, "orderNumber")
            );
            case "status" -> workflowStatusSort(direction)
                    .and(Sort.by(new Sort.Order(Sort.Direction.DESC, "orderNumber")));
            case "orderNumber" -> numericOrderNumberSort(direction);
            default -> defaultRepairSort();
        };
    }

    private Sort defaultRepairSort() {
        return workflowStatusSort(Sort.Direction.ASC)
                .and(JpaSort.unsafe(Sort.Direction.DESC, "COALESCE(r.receiveDateTime, r.returnDateTime)"))
                .and(numericOrderNumberSort(Sort.Direction.DESC));
    }

    private Sort workflowStatusSort(Sort.Direction direction) {
        return JpaSort.unsafe(
                direction,
                """
                (CASE r.status
                  WHEN com.taller.model.enums.RepairStatusEnum.RECIBIDA THEN 0
                  WHEN com.taller.model.enums.RepairStatusEnum.PRESUPUESTADA_ESPERANDO_RESPUESTA THEN 1
                  WHEN com.taller.model.enums.RepairStatusEnum.HACIENDO THEN 2
                  WHEN com.taller.model.enums.RepairStatusEnum.ESPERANDO_RETIRO THEN 3
                  WHEN com.taller.model.enums.RepairStatusEnum.POR_RECIBIR THEN 4
                  WHEN com.taller.model.enums.RepairStatusEnum.RETIRADA THEN 5
                  ELSE 6
                END)
                """
        );
    }

    private Sort numericOrderNumberSort(Sort.Direction direction) {
        return JpaSort.unsafe(direction, "length(r.orderNumber)")
                .and(JpaSort.unsafe(direction, "r.orderNumber"));
    }

    private String normalizeOptionalText(String value) {
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
