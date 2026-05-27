package com.taller.service;

import com.taller.model.AppMetadata;
import com.taller.model.Client;
import com.taller.model.Device;
import com.taller.model.Notification;
import com.taller.model.Repair;
import com.taller.model.enums.RepairStatusEnum;
import com.taller.model.repository.AppMetadataRepository;
import com.taller.model.repository.ClientRepository;
import com.taller.model.repository.DeviceRepository;
import com.taller.model.repository.NotificationRepository;
import com.taller.model.repository.RepairPartRepository;
import com.taller.model.repository.RepairRepository;
import com.taller.resource.dto.NotificationDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private RepairRepository repairRepository;
    @Mock
    private RepairPartRepository repairPartRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private DeviceRepository deviceRepository;
    @Mock
    private AppMetadataRepository appMetadataRepository;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(
                notificationRepository,
                repairRepository,
                repairPartRepository,
                clientRepository,
                deviceRepository,
                appMetadataRepository
        );

        when(repairPartRepository.findByRepairId(anyString())).thenReturn(List.of());
        when(clientRepository.findAllById(anyList())).thenReturn(List.of());
        when(deviceRepository.findAllById(anyList())).thenReturn(List.of());
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void latest_generatesSixMonthsByCalendarDateIgnoringHour() {
        LocalDate today = LocalDate.now();
        LocalDateTime retiredAtNight = today.minusMonths(6).atTime(20, 0);
        Repair repair = deliveredRepair("repair-1", "device-1", retiredAtNight);

        when(appMetadataRepository.findById(anyString())).thenReturn(Optional.of(metadata(today.minusDays(1))));
        when(repairRepository.findByStatusAndReturnDateTimeIsNotNullOrderByReturnDateTimeDesc(RepairStatusEnum.RETIRADA))
                .thenReturn(List.of(repair));
        when(notificationRepository.findByEntityIdAndTypeAndEventDate("device-1", "WARRANTY_6_MONTHS", today.atStartOfDay()))
                .thenReturn(Optional.empty());
        when(notificationRepository.findByEntityIdAndTypeAndEventDate("device-1", "WARRANTY_1_YEAR", today.plusYears(1).atStartOfDay()))
                .thenReturn(Optional.empty());
        when(notificationRepository.findByReadedFalseOrderByEventDateDesc()).thenReturn(List.of());

        notificationService.latest();

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertEquals(today.atStartOfDay(), captor.getValue().getEventDate());
        assertEquals("WARRANTY_6_MONTHS", captor.getValue().getType());
    }

    @Test
    void latest_generatesBacklogForUnprocessedDaysAndKeepsUnreadVisible() {
        LocalDate today = LocalDate.now();
        LocalDate dueDate = today.minusDays(2);
        Repair repair = deliveredRepair("repair-2", "device-2", dueDate.minusMonths(6).atTime(15, 30));
        Notification existingUnread = Notification.builder()
                .title("Seguimiento de garantía")
                .message("pendiente")
                .readed(false)
                .eventDate(dueDate.atStartOfDay())
                .type("WARRANTY_6_MONTHS")
                .entityId("device-2")
                .repairId("repair-2")
                .build();

        when(appMetadataRepository.findById(anyString())).thenReturn(Optional.of(metadata(today.minusDays(3))));
        when(repairRepository.findByStatusAndReturnDateTimeIsNotNullOrderByReturnDateTimeDesc(RepairStatusEnum.RETIRADA))
                .thenReturn(List.of(repair));
        when(notificationRepository.findByEntityIdAndTypeAndEventDate("device-2", "WARRANTY_6_MONTHS", dueDate.atStartOfDay()))
                .thenReturn(Optional.empty());
        when(notificationRepository.findByEntityIdAndTypeAndEventDate("device-2", "WARRANTY_1_YEAR", dueDate.plusYears(1).atStartOfDay()))
                .thenReturn(Optional.empty());
        when(notificationRepository.findByReadedFalseOrderByEventDateDesc()).thenReturn(List.of(existingUnread));
        when(repairRepository.findById("repair-2")).thenReturn(Optional.of(repair));
        when(clientRepository.findAllById(anyList())).thenReturn(List.of(client("client-1")));
        when(deviceRepository.findAllById(anyList())).thenReturn(List.of(device("device-2", "client-1")));

        List<NotificationDTO> notifications = notificationService.latest();

        verify(notificationRepository, atLeastOnce()).save(any(Notification.class));
        assertEquals(1, notifications.size());
        assertFalse(Boolean.TRUE.equals(notifications.get(0).getReaded()));
        assertEquals("device-2", notifications.get(0).getDeviceId());
    }

    @Test
    void unreadCount_doesNotDuplicateExistingNotifications() {
        LocalDate today = LocalDate.now();
        Repair repair = deliveredRepair("repair-3", "device-3", today.minusYears(1).atTime(18, 0));

        when(appMetadataRepository.findById(anyString())).thenReturn(Optional.of(metadata(today.minusDays(1))));
        when(repairRepository.findByStatusAndReturnDateTimeIsNotNullOrderByReturnDateTimeDesc(RepairStatusEnum.RETIRADA))
                .thenReturn(List.of(repair));
        when(notificationRepository.findByEntityIdAndTypeAndEventDate("device-3", "WARRANTY_6_MONTHS", today.minusMonths(6).atStartOfDay()))
                .thenReturn(Optional.of(new Notification()));
        when(notificationRepository.findByEntityIdAndTypeAndEventDate("device-3", "WARRANTY_1_YEAR", today.atStartOfDay()))
                .thenReturn(Optional.of(new Notification()));
        when(notificationRepository.countByReadedFalse()).thenReturn(2L);

        long unread = notificationService.unreadCount();

        assertEquals(2L, unread);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    private Repair deliveredRepair(String repairId, String deviceId, LocalDateTime returnDateTime) {
        Repair repair = new Repair();
        repair.setId(repairId);
        repair.setIdClient("client-1");
        repair.setIdDevice(deviceId);
        repair.setOrderNumber("123");
        repair.setDescription("Cambio de disco");
        repair.setStatus(RepairStatusEnum.RETIRADA);
        repair.setReceiveDateTime(returnDateTime.minusDays(2));
        repair.setReturnDateTime(returnDateTime);
        return repair;
    }

    private AppMetadata metadata(LocalDate date) {
        AppMetadata metadata = new AppMetadata();
        metadata.setKey("notifications_warranty_last_generated_date");
        metadata.setValue(date.toString());
        return metadata;
    }

    private Client client(String clientId) {
        Client client = new Client();
        client.setId(clientId);
        client.setName("Ana");
        client.setLastName("Gomez");
        client.setPhone("3415551234");
        client.setEmail("ana@test.com");
        return client;
    }

    private Device device(String deviceId, String clientId) {
        Device device = new Device();
        device.setId(deviceId);
        device.setClientId(clientId);
        device.setBrand("Lenovo");
        device.setModel("ThinkPad");
        device.setSerialNumber("SN-1");
        return device;
    }
}
