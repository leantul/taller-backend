package com.taller.service;

import com.taller.model.AppMetadata;
import com.taller.model.DeviceObservation;
import com.taller.model.Notification;
import com.taller.model.Repair;
import com.taller.model.RepairPart;
import com.taller.model.enums.RepairStatusEnum;
import com.taller.model.repository.AppMetadataRepository;
import com.taller.model.repository.ClientRepository;
import com.taller.model.repository.DeviceObservationRepository;
import com.taller.model.repository.DeviceRepository;
import com.taller.model.repository.NotificationRepository;
import com.taller.model.repository.RepairPartRepository;
import com.taller.model.repository.RepairRepository;
import com.taller.model.repository.projection.ClientBasicView;
import com.taller.model.repository.projection.DeviceBasicView;
import com.taller.resource.dto.NotificationDTO;
import com.taller.resource.dto.RepairPartDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final String TYPE_WARRANTY_6_MONTHS = "WARRANTY_6_MONTHS";
    private static final String TYPE_WARRANTY_1_YEAR = "WARRANTY_1_YEAR";
    private static final String TYPE_DEVICE_OBSERVATION_3_MONTHS = "DEVICE_OBSERVATION_3_MONTHS";
    private static final String LAST_GENERATED_DATE_KEY = "notifications_warranty_last_generated_date";
    private static final String LAST_OBSERVATIONS_GENERATED_DATE_KEY = "notifications_observations_last_generated_date";
    private static final Set<String> WARRANTY_TYPES = Set.of(TYPE_WARRANTY_6_MONTHS, TYPE_WARRANTY_1_YEAR);

    private final NotificationRepository notificationRepository;
    private final RepairRepository repairRepository;
    private final RepairPartRepository repairPartRepository;
    private final ClientRepository clientRepository;
    private final DeviceRepository deviceRepository;
    private final AppMetadataRepository appMetadataRepository;
    private final DeviceObservationRepository deviceObservationRepository;

    @Transactional(readOnly = true)
    public List<NotificationDTO> latest() {
        return toDtos(notificationRepository.findByReadedFalseOrderByEventDateDesc());
    }

    @Transactional(readOnly = true)
    public long unreadCount() {
        return notificationRepository.countByReadedFalse();
    }

    @Transactional
    public void synchronize() {
        synchronizeWarrantyNotifications();
        synchronizeObservationNotifications();
    }

    @Transactional
    public NotificationDTO save(NotificationDTO dto) {
        Notification notification = dto.getId() == null
                ? new Notification()
                : notificationRepository.findById(dto.getId())
                        .orElseThrow(() -> new IllegalArgumentException("La notificación indicada no existe"));
        notification.setTitle(dto.getTitle());
        notification.setMessage(dto.getMessage());
        notification.setReaded(dto.getId() == null ? Boolean.FALSE : dto.getReaded());
        notification.setEventDate(dto.getEventDate());
        notification.setType(dto.getType());
        notification.setEntityId(dto.getEntityId());
        notification.setRepairId(dto.getRepairId());

        return toDtos(List.of(notificationRepository.save(notification))).stream().findFirst().orElseGet(NotificationDTO::new);
    }

    @Transactional
    public void markAsRead(String id) {
        notificationRepository.findById(id).ifPresent(notification -> {
            notification.setReaded(Boolean.TRUE);
            notificationRepository.save(notification);
        });
    }

    private void synchronizeWarrantyNotifications() {
        LocalDate today = LocalDate.now();
        LocalDate lastGeneratedDate = getLastGeneratedDate().orElse(null);
        LocalDate startDate = lastGeneratedDate != null ? lastGeneratedDate.plusDays(1) : null;

        if (startDate != null && startDate.isAfter(today)) {
            return;
        }

        List<WarrantyCandidate> candidates = findLatestDeliveredRepairByDevice().stream()
                .flatMap(repair -> warrantyCandidates(repair, startDate, today).stream())
                .toList();
        if (!candidates.isEmpty()) {
            Set<NotificationKey> existingKeys = notificationRepository.findByEntityIdInAndTypeInAndEventDateBetween(
                            candidates.stream().map(candidate -> candidate.repair().getIdDevice()).collect(Collectors.toSet()),
                            WARRANTY_TYPES,
                            candidates.stream().map(WarrantyCandidate::eventDate).min(LocalDateTime::compareTo).orElseThrow(),
                            candidates.stream().map(WarrantyCandidate::eventDate).max(LocalDateTime::compareTo).orElseThrow())
                    .stream()
                    .map(notification -> new NotificationKey(notification.getEntityId(), notification.getType(), notification.getEventDate()))
                    .collect(Collectors.toSet());
            List<Notification> notifications = candidates.stream()
                    .filter(candidate -> !existingKeys.contains(candidate.key()))
                    .map(this::toNotification)
                    .toList();
            if (!notifications.isEmpty()) {
                notificationRepository.saveAll(notifications);
            }
        }

        saveLastGeneratedDate(today);
    }

    private void synchronizeObservationNotifications() {
        LocalDate today = LocalDate.now();
        LocalDate lastGeneratedDate = getLastGeneratedDate(LAST_OBSERVATIONS_GENERATED_DATE_KEY).orElse(null);
        LocalDate startDate = lastGeneratedDate != null ? lastGeneratedDate.plusDays(1) : null;

        if (startDate != null && startDate.isAfter(today)) {
            return;
        }

        LocalDateTime endDate = today.atTime(23, 59, 59);
        List<DeviceObservation> observations = startDate == null
                ? deviceObservationRepository.findByResolvedAtIsNullAndFollowUpAtLessThanEqualOrderByFollowUpAtAsc(endDate)
                : deviceObservationRepository.findByResolvedAtIsNullAndFollowUpAtBetweenOrderByFollowUpAtAsc(startDate.atStartOfDay(), endDate);

        Set<String> observationIds = observations.stream()
                .map(DeviceObservation::getId)
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toSet());
        Set<String> alreadyNotifiedObservationIds = observationIds.isEmpty()
                ? Set.of()
                : notificationRepository.findByEntityIdInAndType(observationIds, TYPE_DEVICE_OBSERVATION_3_MONTHS).stream()
                .map(Notification::getEntityId)
                .collect(Collectors.toSet());

        List<Notification> notifications = observations.stream()
                .filter(observation -> observation.getId() != null && !alreadyNotifiedObservationIds.contains(observation.getId()))
                .map(observation -> buildObservationNotificationIfDue(observation, startDate, today))
                .flatMap(Optional::stream)
                .toList();
        if (!notifications.isEmpty()) {
            notificationRepository.saveAll(notifications);
        }

        saveLastGeneratedDate(LAST_OBSERVATIONS_GENERATED_DATE_KEY, today);
    }

    private List<WarrantyCandidate> warrantyCandidates(Repair repair, LocalDate startDate, LocalDate endDate) {
        LocalDate returnDate = toLocalDate(repair.getReturnDateTime());
        if (returnDate == null || repair.getIdDevice() == null) {
            return List.of();
        }
        return List.of(
                        new WarrantyCandidate(repair, TYPE_WARRANTY_6_MONTHS, returnDate.plusMonths(6).atStartOfDay()),
                        new WarrantyCandidate(repair, TYPE_WARRANTY_1_YEAR, returnDate.plusYears(1).atStartOfDay()))
                .stream()
                .filter(candidate -> !candidate.eventDate().toLocalDate().isAfter(endDate))
                .filter(candidate -> startDate == null || !candidate.eventDate().toLocalDate().isBefore(startDate))
                .toList();
    }

    private Notification toNotification(WarrantyCandidate candidate) {
        return Notification.builder()
                .title(titleFor(candidate.type()))
                .message(messageFor(candidate.type(), candidate.repair().getOrderNumber()))
                .readed(Boolean.FALSE)
                .eventDate(candidate.eventDate())
                .type(candidate.type())
                .entityId(candidate.repair().getIdDevice())
                .repairId(candidate.repair().getId())
                .build();
    }

    private Optional<Notification> buildObservationNotificationIfDue(DeviceObservation observation, LocalDate startDate, LocalDate endDate) {
        LocalDate dueDate = toLocalDate(observation.getFollowUpAt());
        if (dueDate == null) {
            return Optional.empty();
        }
        if (dueDate.isAfter(endDate)) {
            return Optional.empty();
        }
        if (startDate != null && dueDate.isBefore(startDate)) {
            return Optional.empty();
        }
        if (observation.getId() == null || observation.getDeviceId() == null) {
            return Optional.empty();
        }

        return Optional.of(Notification.builder()
                .title(titleFor(TYPE_DEVICE_OBSERVATION_3_MONTHS))
                .message(messageForObservation(observation))
                .readed(Boolean.FALSE)
                .eventDate(dueDate.atStartOfDay())
                .type(TYPE_DEVICE_OBSERVATION_3_MONTHS)
                .entityId(observation.getId())
                .repairId(observation.getRepairId())
                .build());
    }

    private List<Repair> findLatestDeliveredRepairByDevice() {
        return repairRepository.findByStatusAndReturnDateTimeIsNotNullOrderByReturnDateTimeDesc(RepairStatusEnum.RETIRADA).stream()
                .filter(repair -> repair.getIdDevice() != null)
                .collect(Collectors.toMap(
                        Repair::getIdDevice,
                        Function.identity(),
                        (existing, ignored) -> existing
                ))
                .values()
                .stream()
                .sorted(Comparator.comparing(Repair::getReturnDateTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private Optional<LocalDate> getLastGeneratedDate() {
        return getLastGeneratedDate(LAST_GENERATED_DATE_KEY);
    }

    private Optional<LocalDate> getLastGeneratedDate(String key) {
        return appMetadataRepository.findById(key)
                .map(AppMetadata::getValue)
                .flatMap(this::parseDateSafely);
    }

    private void saveLastGeneratedDate(LocalDate date) {
        saveLastGeneratedDate(LAST_GENERATED_DATE_KEY, date);
    }

    private void saveLastGeneratedDate(String key, LocalDate date) {
        AppMetadata metadata = new AppMetadata();
        metadata.setKey(key);
        metadata.setValue(date.toString());
        appMetadataRepository.save(metadata);
    }

    private Optional<LocalDate> parseDateSafely(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDate.parse(rawValue));
        } catch (DateTimeParseException ex) {
            return Optional.empty();
        }
    }

    private LocalDate toLocalDate(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.toLocalDate() : null;
    }

    private String titleFor(String type) {
        return switch (type) {
            case TYPE_WARRANTY_6_MONTHS -> "Seguimiento de garantía";
            case TYPE_WARRANTY_1_YEAR -> "Control anual de equipo";
            case TYPE_DEVICE_OBSERVATION_3_MONTHS -> "Seguimiento de observación";
            default -> "Aviso";
        };
    }

    private String messageFor(String type, String orderNumber) {
        return switch (type) {
            case TYPE_WARRANTY_6_MONTHS ->
                    "Conviene revisar el seguimiento de la orden #" + safeOrderNumber(orderNumber) + " por control de garantía.";
            case TYPE_WARRANTY_1_YEAR ->
                    "La orden #" + safeOrderNumber(orderNumber) + " ya alcanzó un año desde el retiro y quedó lista para seguimiento comercial.";
            default -> "Hay un aviso pendiente para revisar.";
        };
    }

    private String messageForObservation(DeviceObservation observation) {
        return "Hace 3 meses se registró: " + safeObservation(observation.getNote()) + ". Conviene contactar al cliente para consultar si desea realizar el trabajo.";
    }

    private String safeOrderNumber(String orderNumber) {
        return orderNumber != null && !orderNumber.isBlank() ? orderNumber : "-";
    }

    private String safeObservation(String note) {
        return note != null && !note.isBlank() ? note : "observación pendiente";
    }

    private List<NotificationDTO> toDtos(List<Notification> notifications) {
        if (notifications == null || notifications.isEmpty()) {
            return List.of();
        }

        Set<String> repairIds = notifications.stream()
                .map(Notification::getRepairId)
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toSet());

        Set<String> observationIds = notifications.stream()
                .filter(notification -> TYPE_DEVICE_OBSERVATION_3_MONTHS.equals(notification.getType()))
                .map(Notification::getEntityId)
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toSet());

        Map<String, DeviceObservation> observationsById = observationIds.isEmpty()
                ? Map.of()
                : deviceObservationRepository.findAllById(observationIds).stream()
                .collect(Collectors.toMap(DeviceObservation::getId, Function.identity()));

        observationsById.values().stream()
                .map(DeviceObservation::getRepairId)
                .filter(id -> id != null && !id.isBlank())
                .forEach(repairIds::add);

        Map<String, Repair> repairsById = repairRepository.findAllById(repairIds).stream()
                .collect(Collectors.toMap(Repair::getId, Function.identity()));

        Map<String, List<RepairPartDTO>> partsByRepairId = repairIds.isEmpty()
                ? Map.of()
                : repairPartRepository.findByRepairIdIn(repairIds).stream()
                .map(this::toPartDto)
                .collect(Collectors.groupingBy(RepairPartDTO::getRepairId));

        Set<String> clientIds = repairsById.values().stream()
                .map(Repair::getIdClient)
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toSet());
        Set<String> deviceIds = repairsById.values().stream()
                .map(Repair::getIdDevice)
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toSet());
        observationsById.values().stream()
                .map(DeviceObservation::getDeviceId)
                .filter(id -> id != null && !id.isBlank())
                .forEach(deviceIds::add);

        Map<String, DeviceBasicView> devicesById = deviceIds.isEmpty()
                ? Map.of()
                : deviceRepository.findBasicByIdIn(deviceIds).stream()
                .collect(Collectors.toMap(DeviceBasicView::getId, Function.identity()));

        devicesById.values().stream()
                .map(DeviceBasicView::getClientId)
                .filter(id -> id != null && !id.isBlank())
                .forEach(clientIds::add);

        Map<String, ClientBasicView> clientsById = clientIds.isEmpty()
                ? Map.of()
                : clientRepository.findBasicByIdIn(clientIds).stream()
                .collect(Collectors.toMap(ClientBasicView::getId, Function.identity()));

        return notifications.stream()
                .map(notification -> toDto(notification, repairsById, partsByRepairId, clientsById, devicesById, observationsById))
                .toList();
    }

    private NotificationDTO toDto(
            Notification notification,
            Map<String, Repair> repairsById,
            Map<String, List<RepairPartDTO>> partsByRepairId,
            Map<String, ClientBasicView> clientsById,
            Map<String, DeviceBasicView> devicesById,
            Map<String, DeviceObservation> observationsById
    ) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setReaded(notification.getReaded());
        dto.setEventDate(notification.getEventDate());
        dto.setType(notification.getType());
        dto.setEntityId(notification.getEntityId());
        dto.setRepairId(notification.getRepairId());

        Repair repair = notification.getRepairId() != null ? repairsById.get(notification.getRepairId()) : null;
        if (repair != null) {
            enrichDto(dto, repair, partsByRepairId, clientsById, devicesById);
        }
        DeviceObservation observation = TYPE_DEVICE_OBSERVATION_3_MONTHS.equals(notification.getType())
                ? observationsById.get(notification.getEntityId())
                : null;
        if (observation != null) {
            enrichObservationDto(dto, observation, repairsById, partsByRepairId, clientsById, devicesById);
        }
        return dto;
    }

    private void enrichDto(
            NotificationDTO dto,
            Repair repair,
            Map<String, List<RepairPartDTO>> partsByRepairId,
            Map<String, ClientBasicView> clientsById,
            Map<String, DeviceBasicView> devicesById
    ) {
        dto.setRepairId(repair.getId());
        dto.setDeviceId(repair.getIdDevice());
        dto.setClientId(repair.getIdClient());
        dto.setOrderNumber(repair.getOrderNumber());
        dto.setRepairDescription(repair.getDescription());
        dto.setStatus(repair.getStatus());
        dto.setReceiveDateTime(repair.getReceiveDateTime());
        dto.setReturnDateTime(repair.getReturnDateTime());
        dto.setQuotedAmount(repair.getQuotedAmount());
        dto.setPrice(repair.getPrice());
        dto.setQuoteNotes(repair.getQuoteNotes());
        dto.setParts(partsByRepairId.getOrDefault(repair.getId(), List.of()));

        ClientBasicView client = clientsById.get(repair.getIdClient());
        if (client != null) {
            dto.setClientName(client.getName());
            dto.setClientLastName(client.getLastName());
            dto.setClientPhone(client.getPhone());
            dto.setClientEmail(client.getEmail());
        }

        DeviceBasicView device = devicesById.get(repair.getIdDevice());
        if (device != null) {
            dto.setDeviceTypeName(device.getDeviceTypeName());
            dto.setDeviceBrand(device.getBrand());
            dto.setDeviceModel(device.getModel());
            dto.setDeviceSerialNumber(device.getSerialNumber());
        }
    }

    private void enrichObservationDto(
            NotificationDTO dto,
            DeviceObservation observation,
            Map<String, Repair> repairsById,
            Map<String, List<RepairPartDTO>> partsByRepairId,
            Map<String, ClientBasicView> clientsById,
            Map<String, DeviceBasicView> devicesById
    ) {
        dto.setObservationId(observation.getId());
        dto.setObservationNote(observation.getNote());
        dto.setObservationObservedAt(observation.getObservedAt());
        dto.setObservationFollowUpAt(observation.getFollowUpAt());
        dto.setDeviceId(observation.getDeviceId());

        Repair repair = observation.getRepairId() != null ? repairsById.get(observation.getRepairId()) : null;
        if (repair != null) {
            enrichDto(dto, repair, partsByRepairId, clientsById, devicesById);
        }

        DeviceBasicView device = devicesById.get(observation.getDeviceId());
        if (device != null) {
            dto.setDeviceTypeName(device.getDeviceTypeName());
            dto.setDeviceBrand(device.getBrand());
            dto.setDeviceModel(device.getModel());
            dto.setDeviceSerialNumber(device.getSerialNumber());
            dto.setClientId(device.getClientId());

            ClientBasicView client = clientsById.get(device.getClientId());
            if (client != null) {
                dto.setClientName(client.getName());
                dto.setClientLastName(client.getLastName());
                dto.setClientPhone(client.getPhone());
                dto.setClientEmail(client.getEmail());
            }
        }
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

    private record WarrantyCandidate(Repair repair, String type, LocalDateTime eventDate) {
        private NotificationKey key() {
            return new NotificationKey(repair.getIdDevice(), type, eventDate);
        }
    }

    private record NotificationKey(String entityId, String type, LocalDateTime eventDate) {
    }
}
