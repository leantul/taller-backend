package com.taller.service;

import com.taller.model.DeviceObservation;
import com.taller.model.Repair;
import com.taller.model.RepairPart;
import com.taller.model.RepairPayment;
import com.taller.model.RepairStatusHistory;
import com.taller.model.enums.RepairStatusEnum;
import com.taller.model.enums.CurrencyEnum;
import com.taller.model.repository.DeviceObservationRepository;
import com.taller.model.repository.RepairPaymentRepository;
import com.taller.model.repository.RepairPartRepository;
import com.taller.model.repository.RepairRepository;
import com.taller.model.repository.RepairStatusHistoryRepository;
import com.taller.model.repository.projection.FinanceRepairView;
import com.taller.model.repository.projection.RepairListView;
import com.taller.model.repository.projection.RepairStatusHistoryView;
import com.taller.model.repository.projection.StatusBoardRepairView;
import com.taller.resource.dto.DeviceObservationDTO;
import com.taller.resource.dto.PageDTO;
import com.taller.resource.dto.RepairDTO;
import com.taller.resource.dto.RepairPartDTO;
import com.taller.resource.dto.RepairPaymentDTO;
import com.taller.resource.dto.RepairStatusHistoryDTO;
import com.taller.resource.dto.RepairStatusUpdateDTO;
import com.taller.resource.dto.StatusBoardRepairDTO;
import com.taller.resource.mapper.DeviceObservationMapper;
import com.taller.resource.mapper.RepairPartMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.taller.service.support.PageSupport.boundedPageRequest;
import static com.taller.service.support.PageSupport.normalizeTerm;
import static com.taller.service.support.PageSupport.toPageDto;

@Service
@RequiredArgsConstructor
public class RepairService {

    private final RepairRepository repairRepository;
    private final RepairPartRepository repairPartRepository;
    private final RepairPaymentRepository repairPaymentRepository;
    private final DeviceObservationRepository deviceObservationRepository;
    private final RepairStatusHistoryRepository repairStatusHistoryRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<RepairDTO> getAllRepairs() {
      return repairRepository.findListRows().stream().map(this::toListDto).toList();
    }

    @Transactional(readOnly = true)
    public PageDTO<RepairDTO> findPage(int page, int size, String term, LocalDateTime from, LocalDateTime to, RepairStatusEnum status, String sortField, String sortOrder) {
        Page<RepairListView> result = findRepairPage(normalizeTerm(term), status, from, to,
                boundedPageRequest(page, size, 100, resolveSort(sortField, sortOrder)));
        return toPageDto(result, result.getContent().stream().map(this::toListDto).toList());
    }

    @Transactional(readOnly = true)
    public List<StatusBoardRepairDTO> getStatusBoard() {
        return repairRepository.findStatusBoardRows().stream().map(this::toStatusBoardDto).toList();
    }

    @Transactional(readOnly = true)
    public PageDTO<StatusBoardRepairDTO> getStatusBoardPage(RepairStatusEnum status, int page, int size) {
        if (status == null || status == RepairStatusEnum.RETIRADA) throw new IllegalArgumentException("Seleccioná un estado activo");
        Page<StatusBoardRepairView> result = repairRepository.findStatusBoardPage(status,
                boundedPageRequest(page, size, 50, Sort.by(Sort.Direction.DESC, "creationDateTime")));
        return toPageDto(result, result.getContent().stream().map(this::toStatusBoardDto).toList());
    }

    @Transactional(readOnly = true)
    public RepairDTO getRepairById(String id) {
        return repairRepository.findById(id).map(this::toDto).orElse(null);
    }

    @Transactional
    public RepairDTO save(RepairDTO repairDTO) {
        validateLaborAmount(repairDTO.getLaborAmount());
        Repair repair = repairDTO.getId() != null
                ? repairRepository.findById(repairDTO.getId()).orElseGet(Repair::new)
                : new Repair();
        boolean isNew = repair.getId() == null;
        RepairStatusEnum previousStatus = repair.getStatus();

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
            receiveDateTime = now();
        }
        repair.setReceiveDateTime(receiveDateTime);

        LocalDateTime returnDateTime = repairDTO.getReturnDateTime() != null
                ? repairDTO.getReturnDateTime()
                : repair.getReturnDateTime();
        if (returnDateTime == null && repairDTO.getStatus() == RepairStatusEnum.RETIRADA) {
            returnDateTime = now();
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

        if (!isNew && previousStatus != repair.getStatus()) {
            if (repair.getStatus() == RepairStatusEnum.COBRADO_ESPERANDO_RETIRO) {
                registerRequestedPayment(repair, repairDTO.getPaymentType(), repairDTO.getPaymentAmount());
            }
            if (repair.getStatus() == RepairStatusEnum.RETIRADA) {
                registerRemainingBalance(repair, "Saldo cobrado al retirar");
            }
        }

        List<RepairPaymentDTO> effectivePayments = repairDTO.getPayments() != null
                ? repairDTO.getPayments()
                : repair.getId() != null ? repairPaymentRepository.findByRepairId(repair.getId()).stream().map(this::toPaymentDto).toList() : List.of();
        validatePayments(repair, effectivePayments);

        if (repairDTO.getId() != null) {
            repair.setId(repairDTO.getId());
        }

        Repair saved = repairRepository.save(repair);
        recordStatusHistory(saved, previousStatus, isNew);

        if (repairDTO.getParts() != null) {
            syncParts(saved, repairDTO.getParts());
        }

        if (repairDTO.getPayments() != null) {
            syncPayments(saved, repairDTO.getPayments());
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
        updateStatus(id, new RepairStatusUpdateDTO(status, receiveDateTime, returnDateTime, null, null));
    }

    @Transactional
    public void updateStatus(String id, RepairStatusUpdateDTO request) {
        Repair repair = repairRepository.findById(id).orElseThrow();
        RepairStatusEnum previousStatus = repair.getStatus();
        RepairStatusEnum status = request.status();
        if (status == null) throw new IllegalArgumentException("Seleccioná un estado");
        if (status == RepairStatusEnum.COBRADO_ESPERANDO_RETIRO) {
            registerRequestedPayment(repair, request.paymentType(), request.paymentAmount());
        }
        if (status == RepairStatusEnum.RETIRADA) {
            registerRemainingBalance(repair, "Saldo cobrado al retirar");
        }
        repair.setStatus(status);
        if (status == RepairStatusEnum.RECIBIDA) {
            repair.setReceiveDateTime(request.receiveDateTime() != null ? request.receiveDateTime() : repair.getReceiveDateTime() != null ? repair.getReceiveDateTime() : now());
        }
        if (status == RepairStatusEnum.RETIRADA) {
            repair.setReturnDateTime(request.returnDateTime() != null ? request.returnDateTime() : repair.getReturnDateTime() != null ? repair.getReturnDateTime() : now());
        } else {
            repair.setReturnDateTime(null);
        }
        repairRepository.save(repair);
        recordStatusHistory(repair, previousStatus, false);
    }

    @Transactional
    public RepairDTO replacePayments(String id, List<RepairPaymentDTO> paymentDtos) {
        Repair repair = repairRepository.findById(id).orElseThrow();
        RepairStatusEnum previousStatus = repair.getStatus();
        if (repair.getStatus() == RepairStatusEnum.COBRADO_ESPERANDO_RETIRO && (paymentDtos == null || paymentDtos.isEmpty())) {
            repair.setStatus(RepairStatusEnum.ESPERANDO_RETIRO);
            repairRepository.save(repair);
        }
        validatePayments(repair, paymentDtos);
        syncPayments(repair, paymentDtos);
        recordStatusHistory(repair, previousStatus, false);
        return toDto(repair);
    }

    @Transactional
    public void delete(String id) {
        repairPartRepository.deleteAll(repairPartRepository.findByRepairId(id));
        repairPaymentRepository.deleteAll(repairPaymentRepository.findByRepairId(id));
        deviceObservationRepository.deleteAll(deviceObservationRepository.findByRepairId(id));
        repairStatusHistoryRepository.deleteByRepairId(id);
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
                            : repair.getPrice() != null ? repair.getPrice() : BigDecimal.ZERO;

                    return laborIncome.add(repair.getPartsProfit() != null ? repair.getPartsProfit() : BigDecimal.ZERO);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    private String nextOrderNumber() {
        Long value = repairRepository.nextOrderValue();
        return String.valueOf(value);
    }

    private void validateLaborAmount(BigDecimal laborAmount) {
        if (laborAmount == null) {
            throw new IllegalArgumentException("Completá la mano de obra. Si no corresponde, ingresá $0");
        }
        if (laborAmount.signum() < 0) {
            throw new IllegalArgumentException("La mano de obra no puede ser negativa");
        }
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
        dto.setParts(repairPartRepository.findByRepairId(repair.getId()).stream().map(RepairPartMapper::toDto).toList());
        List<RepairPayment> payments = repairPaymentRepository.findByRepairId(repair.getId());
        dto.setPayments(payments.stream().map(this::toPaymentDto).toList());
        BigDecimal totalPaid = payments.stream().map(RepairPayment::getAmount).filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalPaid(totalPaid);
        dto.setOutstandingBalance(positive(price(repair).subtract(totalPaid)));
        dto.setObservations(deviceObservationRepository.findByRepairIdOrderByObservedAtDesc(repair.getId()).stream().map(DeviceObservationMapper::toDto).toList());
        dto.setStatusHistory(repairStatusHistoryRepository.findByRepairIdOrderByChangedAtAscCreationDateTimeAsc(repair.getId()).stream().map(this::toStatusHistoryDto).toList());
        return dto;
    }

    private void recordStatusHistory(Repair repair, RepairStatusEnum previousStatus, boolean isNew) {
        if (repair.getId() == null || repair.getStatus() == null) {
            return;
        }
        if (!isNew && repair.getStatus() == previousStatus) {
            return;
        }

        LocalDateTime changedAt = statusChangedAt(repair);
        if (changedAt == null) {
            return;
        }
        if (repairStatusHistoryRepository.existsByRepairIdAndStatusAndChangedAt(repair.getId(), repair.getStatus(), changedAt)) {
            return;
        }

        repairStatusHistoryRepository.save(RepairStatusHistory.builder()
                .repairId(repair.getId())
                .status(repair.getStatus())
                .changedAt(changedAt)
                .build());
    }

    private LocalDateTime statusChangedAt(Repair repair) {
        if (repair.getStatus() == RepairStatusEnum.POR_RECIBIR) {
            return null;
        }
        if (repair.getStatus() == RepairStatusEnum.RECIBIDA) {
            return repair.getReceiveDateTime();
        }
        if (repair.getStatus() == RepairStatusEnum.RETIRADA) {
            return repair.getReturnDateTime();
        }
        return now();
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

    private RepairStatusHistoryDTO toStatusHistoryDto(RepairStatusHistoryView history) {
        RepairStatusHistoryDTO dto = new RepairStatusHistoryDTO();
        dto.setId(history.getId());
        dto.setRepairId(history.getRepairId());
        dto.setStatus(history.getStatus());
        dto.setChangedAt(history.getChangedAt());
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

    private void syncParts(Repair repair, List<RepairPartDTO> partDtos) {
        Map<String, RepairPart> existingById = repairPartRepository.findByRepairId(repair.getId()).stream()
                .filter(part -> part.getId() != null)
                .collect(Collectors.toMap(RepairPart::getId, part -> part));
        Set<String> incomingIds = partDtos.stream()
                .map(RepairPartDTO::getId)
                .filter(id -> id != null && existingById.containsKey(id))
                .collect(Collectors.toSet());
        repairPartRepository.deleteAll(existingById.values().stream()
                .filter(part -> !incomingIds.contains(part.getId()))
                .toList());
        repairPartRepository.saveAll(partDtos.stream().map(dto -> {
            RepairPart part = existingById.getOrDefault(dto.getId(), new RepairPart());
            part.setRepairId(repair.getId());
            part.setName(dto.getName());
            part.setQuantity(dto.getQuantity());
            part.setProvider(dto.getProvider());
            part.setCost(dto.getCost());
            part.setSalePrice(dto.getSalePrice());
            return part;
        }).toList());
    }

    private void syncPayments(Repair repair, List<RepairPaymentDTO> paymentDtos) {
        Map<String, RepairPayment> existingById = repairPaymentRepository.findByRepairId(repair.getId()).stream()
                .filter(payment -> payment.getId() != null)
                .collect(Collectors.toMap(RepairPayment::getId, payment -> payment));
        Set<String> incomingIds = paymentDtos.stream()
                .map(RepairPaymentDTO::getId)
                .filter(id -> id != null && existingById.containsKey(id))
                .collect(Collectors.toSet());
        repairPaymentRepository.deleteAll(existingById.values().stream()
                .filter(payment -> !incomingIds.contains(payment.getId()))
                .toList());
        repairPaymentRepository.saveAll(paymentDtos.stream().map(dto -> {
            RepairPayment payment = existingById.getOrDefault(dto.getId(), new RepairPayment());
            payment.setRepairId(repair.getId());
            payment.setAmount(dto.getAmount());
            payment.setCurrency(CurrencyEnum.ARS);
            payment.setPaymentDate(dto.getPaymentDate() != null ? dto.getPaymentDate() : now());
            payment.setNotes(dto.getNotes());
            return payment;
        }).toList());
    }

    private void registerRequestedPayment(Repair repair, RepairStatusUpdateDTO.PaymentType type, BigDecimal partialAmount) {
        if (type == null) throw new IllegalArgumentException("Indicá si el cobro es total o parcial");
        BigDecimal remaining = remainingBalance(repair);
        if (remaining.signum() <= 0) return;
        BigDecimal amount = type == RepairStatusUpdateDTO.PaymentType.FULL ? remaining : partialAmount;
        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("El monto cobrado debe ser mayor que cero");
        if (amount.compareTo(remaining) > 0) throw new IllegalArgumentException("El monto cobrado no puede superar el saldo pendiente");
        savePayment(repair, amount, type == RepairStatusUpdateDTO.PaymentType.FULL ? "Cobro total antes del retiro" : "Cobro parcial");
    }

    private void registerRemainingBalance(Repair repair, String notes) {
        BigDecimal remaining = remainingBalance(repair);
        if (remaining.signum() > 0) savePayment(repair, remaining, notes);
    }

    private void savePayment(Repair repair, BigDecimal amount, String notes) {
        if (price(repair).signum() <= 0) throw new IllegalArgumentException("La reparación debe tener un monto final mayor que cero para registrar un cobro");
        repairPaymentRepository.save(RepairPayment.builder().repairId(repair.getId()).amount(amount)
                .currency(CurrencyEnum.ARS).paymentDate(now()).notes(notes).build());
    }

    private BigDecimal remainingBalance(Repair repair) {
        BigDecimal paid = repairPaymentRepository.findByRepairId(repair.getId()).stream()
                .map(RepairPayment::getAmount).filter(java.util.Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        return positive(price(repair).subtract(paid));
    }

    private void validatePayments(Repair repair, List<RepairPaymentDTO> paymentDtos) {
        if (paymentDtos == null) throw new IllegalArgumentException("La lista de pagos es obligatoria");
        BigDecimal total = BigDecimal.ZERO;
        for (RepairPaymentDTO payment : paymentDtos) {
            if (payment.getAmount() == null || payment.getAmount().signum() <= 0) throw new IllegalArgumentException("Todos los pagos deben ser mayores que cero");
            if (payment.getCurrency() != null && payment.getCurrency() != CurrencyEnum.ARS) throw new IllegalArgumentException("Los cobros de reparaciones deben registrarse en ARS");
            total = total.add(payment.getAmount());
        }
        if (total.compareTo(price(repair)) > 0) throw new IllegalArgumentException("El total cobrado no puede superar el monto final");
        if (repair.getStatus() == RepairStatusEnum.RETIRADA && total.compareTo(price(repair)) != 0) throw new IllegalArgumentException("Una reparación retirada debe quedar completamente pagada");
        if (repair.getStatus() == RepairStatusEnum.COBRADO_ESPERANDO_RETIRO && total.signum() <= 0) throw new IllegalArgumentException("El estado cobrado esperando retiro requiere al menos un pago");
    }

    private BigDecimal price(Repair repair) {
        return repair.getPrice() != null ? repair.getPrice() : BigDecimal.ZERO;
    }

    private BigDecimal positive(BigDecimal value) {
        return value.signum() > 0 ? value : BigDecimal.ZERO;
    }

    private DeviceObservation toObservation(Repair repair, DeviceObservationDTO dto, DeviceObservation observation) {
        LocalDateTime observedAt = dto.getObservedAt() != null
                ? dto.getObservedAt()
                : observation.getObservedAt() != null ? observation.getObservedAt() : now();
        observation.setDeviceId(repair.getIdDevice());
        observation.setRepairId(repair.getId());
        observation.setNote(dto.getNote().trim());
        observation.setObservedAt(observedAt);
        observation.setFollowUpAt(dto.getFollowUpAt() != null ? dto.getFollowUpAt() : observedAt.plusMonths(3));
        observation.setResolvedAt(dto.getResolvedAt());
        return observation;
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
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

    private Page<RepairListView> findRepairPage(String term, RepairStatusEnum status, LocalDateTime from, LocalDateTime to, PageRequest pageRequest) {
        if (status != null) {
            if (from != null && to != null) {
                return repairRepository.findPageByStatusBetween(term, status, from, to, pageRequest);
            }
            if (from != null) {
                return repairRepository.findPageByStatusFrom(term, status, from, pageRequest);
            }
            if (to != null) {
                return repairRepository.findPageByStatusTo(term, status, to, pageRequest);
            }
            return repairRepository.findPageByStatus(term, status, pageRequest);
        }
        if (from != null && to != null) {
            return repairRepository.findPageBetween(term, from, to, pageRequest);
        }
        if (from != null) {
            return repairRepository.findPageFrom(term, from, pageRequest);
        }
        if (to != null) {
            return repairRepository.findPageTo(term, to, pageRequest);
        }
        return repairRepository.findPage(term, pageRequest);
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
                  WHEN com.taller.model.enums.RepairStatusEnum.COBRADO_ESPERANDO_RETIRO THEN 4
                  WHEN com.taller.model.enums.RepairStatusEnum.POR_RECIBIR THEN 5
                  WHEN com.taller.model.enums.RepairStatusEnum.RETIRADA THEN 6
                  ELSE 7
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
