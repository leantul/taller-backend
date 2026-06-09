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
import com.taller.model.repository.projection.RepairListView;
import com.taller.resource.dto.DeviceObservationDTO;
import com.taller.resource.dto.RepairDTO;
import com.taller.resource.dto.RepairPartDTO;
import com.taller.resource.dto.RepairPaymentDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
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

    public List<RepairDTO> getAllRepairs() {
      return repairRepository.findListRows().stream().map(this::toListDto).toList();
    }

    public RepairDTO getRepairById(String id) {
        return repairRepository.findById(id).map(this::toDto).orElse(null);
    }

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
        if (!isNew && repairDTO.getRepairNotes() != null) {
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

    public void delete(String id) {
        repairPartRepository.deleteAll(repairPartRepository.findByRepairId(id));
        repairPaymentRepository.deleteAll(repairPaymentRepository.findByRepairId(id));
        deviceObservationRepository.deleteAll(deviceObservationRepository.findByRepairId(id));
        repairRepository.deleteById(id);
    }

    public List<RepairDTO> search(String term) {
        return repairRepository.searchListRows(term).stream().map(this::toListDto).toList();
    }

    public BigDecimal totalIncome(LocalDateTime from, LocalDateTime to) {
        List<RepairListView> repairs = repairRepository.findFinanceRowsBetween(from, to);
        Map<String, List<RepairPart>> partsByRepairId = partsByRepairId(repairs.stream().map(RepairListView::getId).toList());

        return repairs.stream()
                .map(repair -> {
                    BigDecimal laborIncome = repair.getLaborAmount() != null
                            ? repair.getLaborAmount()
                            : (repair.getPrice() != null ? repair.getPrice() : BigDecimal.ZERO);

                    BigDecimal partsIncome = partsByRepairId.getOrDefault(repair.getId(), List.of()).stream()
                            .map(part -> {
                                BigDecimal cost = part.getCost() != null ? part.getCost() : BigDecimal.ZERO;
                                BigDecimal sale = part.getSalePrice() != null ? part.getSalePrice() : BigDecimal.ZERO;
                                BigDecimal qty = BigDecimal.valueOf(part.getQuantity() != null ? part.getQuantity() : 1);
                                return sale.subtract(cost).multiply(qty);
                            })
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return laborIncome.add(partsIncome);
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
        return dto;
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

    private String normalizeOptionalText(String value) {
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private Map<String, List<RepairPart>> partsByRepairId(Collection<String> repairIds) {
        if (repairIds == null || repairIds.isEmpty()) {
            return Map.of();
        }
        return repairPartRepository.findByRepairIdIn(repairIds).stream()
                .collect(Collectors.groupingBy(RepairPart::getRepairId));
    }
}
