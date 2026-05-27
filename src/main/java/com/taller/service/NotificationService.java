package com.taller.service;

import com.taller.model.AppMetadata;
import com.taller.model.Notification;
import com.taller.model.Repair;
import com.taller.model.RepairPart;
import com.taller.model.enums.RepairStatusEnum;
import com.taller.model.repository.AppMetadataRepository;
import com.taller.model.repository.ClientRepository;
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
    private static final String LAST_GENERATED_DATE_KEY = "notifications_warranty_last_generated_date";

    private final NotificationRepository notificationRepository;
    private final RepairRepository repairRepository;
    private final RepairPartRepository repairPartRepository;
    private final ClientRepository clientRepository;
    private final DeviceRepository deviceRepository;
    private final AppMetadataRepository appMetadataRepository;

    public List<NotificationDTO> latest() {
        synchronizeWarrantyNotifications();
        return toDtos(notificationRepository.findByReadedFalseOrderByEventDateDesc());
    }

    public long unreadCount() {
        synchronizeWarrantyNotifications();
        return notificationRepository.countByReadedFalse();
    }

    public NotificationDTO save(NotificationDTO dto) {
        Notification notification = Notification.builder()
                .title(dto.getTitle())
                .message(dto.getMessage())
                .readed(Boolean.FALSE)
                .eventDate(dto.getEventDate())
                .type(dto.getType())
                .entityId(dto.getEntityId())
                .repairId(dto.getRepairId())
                .build();

        if (dto.getId() != null) {
            notification.setId(dto.getId());
            notification.setReaded(dto.getReaded());
        }

        return toDtos(List.of(notificationRepository.save(notification))).stream().findFirst().orElseGet(NotificationDTO::new);
    }

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

        List<Repair> latestRepairsByDevice = findLatestDeliveredRepairByDevice();
        for (Repair repair : latestRepairsByDevice) {
            LocalDate returnDate = toLocalDate(repair.getReturnDateTime());
            if (returnDate == null) {
                continue;
            }
            createWarrantyNotificationIfDue(repair, TYPE_WARRANTY_6_MONTHS, returnDate.plusMonths(6), startDate, today);
            createWarrantyNotificationIfDue(repair, TYPE_WARRANTY_1_YEAR, returnDate.plusYears(1), startDate, today);
        }

        saveLastGeneratedDate(today);
    }

    private void createWarrantyNotificationIfDue(Repair repair, String type, LocalDate dueDate, LocalDate startDate, LocalDate endDate) {
        if (dueDate.isAfter(endDate)) {
            return;
        }
        if (startDate != null && dueDate.isBefore(startDate)) {
            return;
        }
        if (repair.getIdDevice() == null) {
            return;
        }

        LocalDateTime eventDate = dueDate.atStartOfDay();
        boolean exists = notificationRepository.findByEntityIdAndTypeAndEventDate(repair.getIdDevice(), type, eventDate).isPresent();
        if (exists) {
            return;
        }

        notificationRepository.save(Notification.builder()
                .title(titleFor(type))
                .message(messageFor(type, repair.getOrderNumber()))
                .readed(Boolean.FALSE)
                .eventDate(eventDate)
                .type(type)
                .entityId(repair.getIdDevice())
                .repairId(repair.getId())
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
        return appMetadataRepository.findById(LAST_GENERATED_DATE_KEY)
                .map(AppMetadata::getValue)
                .flatMap(this::parseDateSafely);
    }

    private void saveLastGeneratedDate(LocalDate date) {
        AppMetadata metadata = new AppMetadata();
        metadata.setKey(LAST_GENERATED_DATE_KEY);
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

    private String safeOrderNumber(String orderNumber) {
        return orderNumber != null && !orderNumber.isBlank() ? orderNumber : "-";
    }

    private List<NotificationDTO> toDtos(List<Notification> notifications) {
        if (notifications == null || notifications.isEmpty()) {
            return List.of();
        }

        Set<String> repairIds = notifications.stream()
                .map(Notification::getRepairId)
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toSet());

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

        Map<String, ClientBasicView> clientsById = clientIds.isEmpty()
                ? Map.of()
                : clientRepository.findBasicByIdIn(clientIds).stream()
                .collect(Collectors.toMap(ClientBasicView::getId, Function.identity()));

        Map<String, DeviceBasicView> devicesById = deviceIds.isEmpty()
                ? Map.of()
                : deviceRepository.findBasicByIdIn(deviceIds).stream()
                .collect(Collectors.toMap(DeviceBasicView::getId, Function.identity()));

        return notifications.stream()
                .map(notification -> toDto(notification, repairsById, partsByRepairId, clientsById, devicesById))
                .toList();
    }

    private NotificationDTO toDto(
            Notification notification,
            Map<String, Repair> repairsById,
            Map<String, List<RepairPartDTO>> partsByRepairId,
            Map<String, ClientBasicView> clientsById,
            Map<String, DeviceBasicView> devicesById
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
            dto.setDeviceType(device.getDeviceType());
            dto.setDeviceBrand(device.getBrand());
            dto.setDeviceModel(device.getModel());
            dto.setDeviceSerialNumber(device.getSerialNumber());
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
}
