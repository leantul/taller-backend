package com.taller.service;

import com.taller.model.Repair;
import com.taller.model.RepairPart;
import com.taller.model.RepairPayment;
import com.taller.model.enums.RepairStatusEnum;
import com.taller.model.repository.RepairPaymentRepository;
import com.taller.model.repository.RepairPartRepository;
import com.taller.model.repository.RepairRepository;
import com.taller.resource.dto.RepairDTO;
import com.taller.resource.dto.RepairPartDTO;
import com.taller.resource.dto.RepairPaymentDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RepairService {

    private final RepairRepository repairRepository;
    private final RepairPartRepository repairPartRepository;
    private final RepairPaymentRepository repairPaymentRepository;

    public List<RepairDTO> getAllRepairs() {
      return repairRepository.findAll().stream().map(this::toDto).toList();
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

        return toDto(saved);
    }

    public void delete(String id) {
        repairPartRepository.deleteAll(repairPartRepository.findByRepairId(id));
        repairPaymentRepository.deleteAll(repairPaymentRepository.findByRepairId(id));
        repairRepository.deleteById(id);
    }

    public List<RepairDTO> search(String term) {
        return repairRepository.search(term).stream().map(this::toDto).toList();
    }

    public BigDecimal totalIncome(LocalDateTime from, LocalDateTime to) {
        return repairRepository.findByReturnDateTimeBetween(from, to).stream()
                .map(Repair::getPrice)
                .filter(v -> v != null)
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
        dto.setApproved(repair.getApproved());
        dto.setRejected(repair.getRejected());
        dto.setReadyNotifiedAt(repair.getReadyNotifiedAt());
        dto.setParts(repairPartRepository.findByRepairId(repair.getId()).stream().map(this::toPartDto).toList());
        dto.setPayments(repairPaymentRepository.findByRepairId(repair.getId()).stream().map(this::toPaymentDto).toList());
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
}
