package com.taller.service;

import com.taller.model.AppMetadata;
import com.taller.model.DeviceObservation;
import com.taller.model.Notification;
import com.taller.model.Repair;
import com.taller.model.enums.RepairStatusEnum;
import com.taller.model.repository.AppMetadataRepository;
import com.taller.model.repository.ClientRepository;
import com.taller.model.repository.DeviceObservationRepository;
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
import static org.mockito.Mockito.lenient;
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
    @Mock
    private DeviceObservationRepository deviceObservationRepository;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(
                notificationRepository,
                repairRepository,
                repairPartRepository,
                clientRepository,
                deviceRepository,
                appMetadataRepository,
                deviceObservationRepository
        );

        lenient().when(repairPartRepository.findByRepairIdIn(anyList())).thenReturn(List.of());
        lenient().when(clientRepository.findBasicByIdIn(anyList())).thenReturn(List.of());
        lenient().when(deviceRepository.findBasicByIdIn(anyList())).thenReturn(List.of());
        lenient().when(deviceObservationRepository.findByResolvedAtIsNullAndFollowUpAtLessThanEqualOrderByFollowUpAtAsc(any(LocalDateTime.class))).thenReturn(List.of());
        lenient().when(deviceObservationRepository.findByResolvedAtIsNullAndFollowUpAtBetweenOrderByFollowUpAtAsc(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(List.of());
        lenient().when(deviceObservationRepository.findAllById(any())).thenReturn(List.of());
        lenient().when(notificationRepository.findByEntityIdInAndType(any(), anyString())).thenReturn(List.of());
        lenient().when(notificationRepository.findByEntityIdInAndTypeInAndEventDateBetween(any(), any(), any(), any())).thenReturn(List.of());
        lenient().when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(notificationRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void latest_generatesSixMonthsByCalendarDateIgnoringHour() {
        LocalDate today = LocalDate.now();
        LocalDateTime retiredAtNight = today.minusMonths(6).atTime(20, 0);
        Repair repair = deliveredRepair("repair-1", "device-1", retiredAtNight);

        when(appMetadataRepository.findById(anyString())).thenReturn(Optional.of(metadata(today.minusDays(1))));
        when(repairRepository.findByStatusAndReturnDateTimeIsNotNullOrderByReturnDateTimeDesc(RepairStatusEnum.RETIRADA))
                .thenReturn(List.of(repair));
        notificationService.synchronize();

        ArgumentCaptor<Iterable> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(notificationRepository).saveAll(captor.capture());
        Notification notification = (Notification) captor.getValue().iterator().next();
        assertEquals(today.atStartOfDay(), notification.getEventDate());
        assertEquals("WARRANTY_6_MONTHS", notification.getType());
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
        when(notificationRepository.findByReadedFalseOrderByEventDateDesc()).thenReturn(List.of(existingUnread));
        when(repairRepository.findAllById(any())).thenReturn(List.of(repair));

        notificationService.synchronize();
        List<NotificationDTO> notifications = notificationService.latest();

        verify(notificationRepository, atLeastOnce()).saveAll(any());
        assertEquals(1, notifications.size());
        assertFalse(Boolean.TRUE.equals(notifications.get(0).getReaded()));
        assertEquals("device-2", notifications.get(0).getDeviceId());
    }

    @Test
    void unreadCount_onlyReadsCurrentCount() {
        when(notificationRepository.countByReadedFalse()).thenReturn(2L);

        long unread = notificationService.unreadCount();

        assertEquals(2L, unread);
        verify(notificationRepository, never()).save(any(Notification.class));
        verify(repairRepository, never()).findByStatusAndReturnDateTimeIsNotNullOrderByReturnDateTimeDesc(any());
    }

    @Test
    void latest_generatesObservationNotificationWhenThreeMonthFollowUpIsDue() {
        LocalDate today = LocalDate.now();
        DeviceObservation observation = new DeviceObservation();
        observation.setId("observation-1");
        observation.setDeviceId("device-1");
        observation.setRepairId("repair-1");
        observation.setNote("Batería para reemplazar");
        observation.setObservedAt(today.minusMonths(3).atTime(10, 0));
        observation.setFollowUpAt(today.atTime(10, 0));

        when(appMetadataRepository.findById(anyString())).thenReturn(Optional.empty());
        when(repairRepository.findByStatusAndReturnDateTimeIsNotNullOrderByReturnDateTimeDesc(RepairStatusEnum.RETIRADA))
                .thenReturn(List.of());
        when(deviceObservationRepository.findByResolvedAtIsNullAndFollowUpAtLessThanEqualOrderByFollowUpAtAsc(any(LocalDateTime.class)))
                .thenReturn(List.of(observation));
        notificationService.synchronize();

        ArgumentCaptor<Iterable> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(notificationRepository).saveAll(captor.capture());
        Notification notification = (Notification) captor.getValue().iterator().next();
        assertEquals("DEVICE_OBSERVATION_3_MONTHS", notification.getType());
        assertEquals("observation-1", notification.getEntityId());
        assertEquals("repair-1", notification.getRepairId());
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

}
