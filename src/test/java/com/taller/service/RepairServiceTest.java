package com.taller.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taller.model.Repair;
import com.taller.model.RepairStatusHistory;
import com.taller.model.RepairPayment;
import com.taller.model.enums.RepairStatusEnum;
import com.taller.model.repository.DeviceObservationRepository;
import com.taller.model.repository.RepairPartRepository;
import com.taller.model.repository.RepairPaymentRepository;
import com.taller.model.repository.RepairRepository;
import com.taller.model.repository.RepairStatusHistoryRepository;
import com.taller.model.repository.projection.RepairListView;
import com.taller.model.repository.projection.RepairStatusHistoryView;
import com.taller.model.repository.projection.StatusBoardRepairView;
import com.taller.resource.dto.RepairDTO;
import com.taller.resource.dto.RepairStatusUpdateDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepairServiceTest {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Argentina/Buenos_Aires");
    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-06-20T20:00:00Z"), BUSINESS_ZONE);

    @Mock
    private RepairRepository repairRepository;
    @Mock
    private RepairPartRepository repairPartRepository;
    @Mock
    private RepairPaymentRepository repairPaymentRepository;
    @Mock
    private DeviceObservationRepository deviceObservationRepository;
    @Mock
    private RepairStatusHistoryRepository repairStatusHistoryRepository;

    private RepairService repairService;

    @BeforeEach
    void setUp() {
        repairService = new RepairService(repairRepository, repairPartRepository, repairPaymentRepository, deviceObservationRepository, repairStatusHistoryRepository, FIXED_CLOCK);
    }

    @Test
    void save_newRepair_assignsNextOrderAndReceiveDate() {
        when(repairRepository.nextOrderValue()).thenReturn(2L);
        when(repairRepository.save(any(Repair.class))).thenAnswer(inv -> {
            Repair r = inv.getArgument(0);
            r.setId("new-id");
            return r;
        });
        when(repairPartRepository.findByRepairId(any())).thenReturn(List.of());
        when(repairPaymentRepository.findByRepairId(any())).thenReturn(List.of());
        when(deviceObservationRepository.findByRepairIdOrderByObservedAtDesc(any())).thenReturn(List.of());
        when(repairStatusHistoryRepository.findByRepairIdOrderByChangedAtAscCreationDateTimeAsc(any())).thenReturn(List.of());

        RepairDTO dto = new RepairDTO();
        dto.setIdClient("c1");
        dto.setIdDevice("d1");
        dto.setDescription("Test");
        dto.setStatus(RepairStatusEnum.POR_RECIBIR);
        dto.setLaborAmount(BigDecimal.ZERO);

        RepairDTO saved = repairService.save(dto);

        assertEquals("2", saved.getOrderNumber());
        assertNotNull(saved.getReceiveDateTime());
    }

    @Test
    void save_newRepair_preservesSelectedReceiveDate() {
        stubSavedRepair();
        when(repairRepository.nextOrderValue()).thenReturn(4L);
        LocalDateTime selectedReceiveDate = LocalDateTime.of(2026, 5, 20, 14, 30);

        RepairDTO dto = new RepairDTO();
        dto.setIdClient("c1");
        dto.setIdDevice("d1");
        dto.setDescription("Test");
        dto.setStatus(RepairStatusEnum.RECIBIDA);
        dto.setLaborAmount(BigDecimal.ZERO);
        dto.setReceiveDateTime(selectedReceiveDate);

        RepairDTO saved = repairService.save(dto);

        assertEquals(selectedReceiveDate, saved.getReceiveDateTime());
    }

    @Test
    void save_newReceivedRepair_withoutSelectedDate_assignsCurrentDate() {
        stubSavedRepair();
        when(repairRepository.nextOrderValue()).thenReturn(5L);

        RepairDTO dto = new RepairDTO();
        dto.setIdClient("c1");
        dto.setIdDevice("d1");
        dto.setDescription("Test");
        dto.setStatus(RepairStatusEnum.RECIBIDA);
        dto.setLaborAmount(BigDecimal.ZERO);

        RepairDTO saved = repairService.save(dto);

        assertNotNull(saved.getReceiveDateTime());
    }

    @Test
    void save_retiradaWithoutReturnDate_assignsReturnDate() {
        when(repairRepository.nextOrderValue()).thenReturn(1L);
        when(repairRepository.save(any(Repair.class))).thenAnswer(inv -> {
            Repair r = inv.getArgument(0);
            r.setId("id-1");
            return r;
        });
        when(repairPartRepository.findByRepairId(any())).thenReturn(List.of());
        when(repairPaymentRepository.findByRepairId(any())).thenReturn(List.of());
        when(deviceObservationRepository.findByRepairIdOrderByObservedAtDesc(any())).thenReturn(List.of());
        when(repairStatusHistoryRepository.findByRepairIdOrderByChangedAtAscCreationDateTimeAsc(any())).thenReturn(List.of());

        RepairDTO dto = new RepairDTO();
        dto.setIdClient("c1");
        dto.setIdDevice("d1");
        dto.setDescription("Test");
        dto.setStatus(RepairStatusEnum.RETIRADA);
        dto.setLaborAmount(BigDecimal.ZERO);

        RepairDTO saved = repairService.save(dto);

        assertNotNull(saved.getReturnDateTime());
    }

    @Test
    void save_newRepair_trimsRepairNotes() {
        stubSavedRepair();
        when(repairRepository.nextOrderValue()).thenReturn(3L);

        RepairDTO dto = new RepairDTO();
        dto.setIdClient("c1");
        dto.setIdDevice("d1");
        dto.setDescription("Test");
        dto.setStatus(RepairStatusEnum.POR_RECIBIR);
        dto.setLaborAmount(BigDecimal.ZERO);
        dto.setRepairNotes("  Observación cargada durante el alta  ");

        RepairDTO saved = repairService.save(dto);

        assertEquals("Observación cargada durante el alta", saved.getRepairNotes());
    }

    @Test
    void save_existingRepair_trimsRepairNotes() {
        Repair existing = existingRepair("Observación anterior");
        when(repairRepository.findById("id-1")).thenReturn(Optional.of(existing));
        stubSavedRepair();

        RepairDTO dto = updateDto("  Trabajo realizado y pruebas completas.  ");

        RepairDTO saved = repairService.save(dto);

        assertEquals("Trabajo realizado y pruebas completas.", saved.getRepairNotes());
    }

    @Test
    void save_withoutLaborAmount_isRejected() {
        RepairDTO dto = new RepairDTO();

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> repairService.save(dto));

        assertEquals("Completá la mano de obra. Si no corresponde, ingresá $0", error.getMessage());
        verify(repairRepository, never()).save(any());
    }

    @Test
    void save_withNegativeLaborAmount_isRejected() {
        RepairDTO dto = new RepairDTO();
        dto.setLaborAmount(BigDecimal.valueOf(-1));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> repairService.save(dto));

        assertEquals("La mano de obra no puede ser negativa", error.getMessage());
        verify(repairRepository, never()).save(any());
    }

    @Test
    void save_existingRepair_emptyRepairNotesClearsValue() {
        Repair existing = existingRepair("Observación anterior");
        when(repairRepository.findById("id-1")).thenReturn(Optional.of(existing));
        stubSavedRepair();

        RepairDTO saved = repairService.save(updateDto("   "));

        assertNull(saved.getRepairNotes());
    }

    @Test
    void save_existingRepair_missingRepairNotesPreservesValue() {
        Repair existing = existingRepair("Observación anterior");
        when(repairRepository.findById("id-1")).thenReturn(Optional.of(existing));
        stubSavedRepair();

        RepairDTO saved = repairService.save(updateDto(null));

        assertEquals("Observación anterior", saved.getRepairNotes());
    }

    @Test
    void save_existingRepairWithSameStatusDoesNotStoreHistory() {
        Repair existing = existingRepair("Observación anterior");
        when(repairRepository.findById("id-1")).thenReturn(Optional.of(existing));
        stubSavedRepair();

        repairService.save(updateDto("Trabajo realizado"));

        verify(repairStatusHistoryRepository, never()).save(any());
    }

    @Test
    void listAndSearchRepairs_doNotSerializeRepairNotes() throws JsonProcessingException {
        RepairListView row = mock(RepairListView.class);
        when(repairRepository.findListRows()).thenReturn(List.of(row));
        when(repairRepository.searchListRows("test")).thenReturn(List.of(row));

        RepairDTO listed = repairService.getAllRepairs().getFirst();
        RepairDTO searched = repairService.search("test").getFirst();
        ObjectMapper objectMapper = new ObjectMapper();

        assertFalse(objectMapper.writeValueAsString(listed).contains("repairNotes"));
        assertFalse(objectMapper.writeValueAsString(searched).contains("repairNotes"));
    }

    @Test
    void statusBoard_mapsEnrichedLabelsWithoutLoadingRelatedEntities() {
        StatusBoardRepairView row = mock(StatusBoardRepairView.class);
        when(row.getId()).thenReturn("r1");
        when(row.getClientName()).thenReturn("Ada");
        when(row.getClientLastName()).thenReturn("Lovelace");
        when(row.getDeviceTypeName()).thenReturn("Notebook");
        when(row.getDeviceBrand()).thenReturn("Lenovo");
        when(row.getDeviceModel()).thenReturn("T14");
        when(repairRepository.findStatusBoardRows()).thenReturn(List.of(row));

        var result = repairService.getStatusBoard().getFirst();

        assertEquals("Ada Lovelace", result.clientName());
        assertEquals("Notebook Lenovo T14", result.deviceLabel());
    }

    @Test
    void statusBoard_preservesMissingDeviceTypePlaceholder() {
        StatusBoardRepairView row = mock(StatusBoardRepairView.class);
        when(row.getDeviceBrand()).thenReturn("Lenovo");
        when(row.getDeviceModel()).thenReturn("T14");
        when(repairRepository.findStatusBoardRows()).thenReturn(List.of(row));

        var result = repairService.getStatusBoard().getFirst();

        assertEquals("- Lenovo T14", result.deviceLabel());
    }

    @Test
    void updateStatus_preservesFieldsNotOwnedByStatusBoard() {
        Repair existing = existingRepair("Trabajo realizado");
        existing.setLaborAmount(new java.math.BigDecimal("12000"));
        existing.setQuoteNotes("Presupuesto aprobado");
        existing.setApproved(true);
        when(repairRepository.findById("id-1")).thenReturn(Optional.of(existing));
        when(repairRepository.save(existing)).thenReturn(existing);

        repairService.updateStatus("id-1", RepairStatusEnum.HACIENDO);

        assertEquals(RepairStatusEnum.HACIENDO, existing.getStatus());
        assertEquals(new java.math.BigDecimal("12000"), existing.getLaborAmount());
        assertEquals("Presupuesto aprobado", existing.getQuoteNotes());
        assertEquals(true, existing.getApproved());
        verify(repairRepository).save(existing);
    }

    @Test
    void updateStatus_toRetiradaAssignsReturnDateWhenMissing() {
        Repair existing = existingRepair(null);
        when(repairRepository.findById("id-1")).thenReturn(Optional.of(existing));
        when(repairRepository.save(existing)).thenReturn(existing);

        repairService.updateStatus("id-1", RepairStatusEnum.RETIRADA);

        assertEquals(RepairStatusEnum.RETIRADA, existing.getStatus());
        assertNotNull(existing.getReturnDateTime());
    }

    @Test
    void updateStatus_partialPaymentThenRetiradaOnlyChargesRemainingBalance() {
        Repair existing = existingRepair(null);
        existing.setPrice(new BigDecimal("100000"));
        when(repairRepository.findById("id-1")).thenReturn(Optional.of(existing));
        when(repairRepository.save(existing)).thenReturn(existing);
        when(repairPaymentRepository.findByRepairId("id-1"))
                .thenReturn(List.of())
                .thenReturn(List.of(RepairPayment.builder().repairId("id-1").amount(new BigDecimal("40000")).build()));

        repairService.updateStatus("id-1", new RepairStatusUpdateDTO(
                RepairStatusEnum.COBRADO_ESPERANDO_RETIRO, null, null,
                RepairStatusUpdateDTO.PaymentType.PARTIAL, new BigDecimal("40000")));
        repairService.updateStatus("id-1", RepairStatusEnum.RETIRADA);

        ArgumentCaptor<RepairPayment> captor = ArgumentCaptor.forClass(RepairPayment.class);
        verify(repairPaymentRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        assertEquals(new BigDecimal("40000"), captor.getAllValues().get(0).getAmount());
        assertEquals(new BigDecimal("60000"), captor.getAllValues().get(1).getAmount());
        assertEquals(RepairStatusEnum.RETIRADA, existing.getStatus());
    }

    @Test
    void updateStatus_rejectsPartialPaymentAboveRemainingBalance() {
        Repair existing = existingRepair(null);
        existing.setPrice(new BigDecimal("100000"));
        when(repairRepository.findById("id-1")).thenReturn(Optional.of(existing));
        when(repairPaymentRepository.findByRepairId("id-1")).thenReturn(List.of(
                RepairPayment.builder().amount(new BigDecimal("40000")).build()));

        assertThrows(IllegalArgumentException.class, () -> repairService.updateStatus("id-1",
                new RepairStatusUpdateDTO(RepairStatusEnum.COBRADO_ESPERANDO_RETIRO, null, null,
                        RepairStatusUpdateDTO.PaymentType.PARTIAL, new BigDecimal("70000"))));
        verify(repairPaymentRepository, never()).save(any());
    }

    @Test
    void save_samePaidStatusRegistersAnAdditionalPartialPayment() {
        Repair existing = existingRepair(null);
        existing.setStatus(RepairStatusEnum.COBRADO_ESPERANDO_RETIRO);
        existing.setPrice(new BigDecimal("100000"));
        existing.setLaborAmount(BigDecimal.ZERO);
        RepairPayment firstPayment = RepairPayment.builder().repairId("id-1").amount(new BigDecimal("40000")).build();
        RepairPayment secondPayment = RepairPayment.builder().repairId("id-1").amount(new BigDecimal("20000")).build();
        when(repairRepository.findById("id-1")).thenReturn(Optional.of(existing));
        when(repairRepository.save(existing)).thenReturn(existing);
        when(repairPaymentRepository.findByRepairId("id-1"))
                .thenReturn(List.of(firstPayment))
                .thenReturn(List.of(firstPayment, secondPayment));
        when(repairPaymentRepository.save(any(RepairPayment.class))).thenReturn(secondPayment);
        when(repairPartRepository.findByRepairId("id-1")).thenReturn(List.of());
        when(deviceObservationRepository.findByRepairIdOrderByObservedAtDesc("id-1")).thenReturn(List.of());
        when(repairStatusHistoryRepository.findByRepairIdOrderByChangedAtAscCreationDateTimeAsc("id-1")).thenReturn(List.of());

        RepairDTO dto = new RepairDTO();
        dto.setId("id-1"); dto.setIdClient("c1"); dto.setIdDevice("d1"); dto.setDescription("Test");
        dto.setStatus(RepairStatusEnum.COBRADO_ESPERANDO_RETIRO); dto.setPrice(new BigDecimal("100000"));
        dto.setLaborAmount(BigDecimal.ZERO); dto.setPaymentType(com.taller.resource.dto.RepairStatusUpdateDTO.PaymentType.PARTIAL);
        dto.setPaymentAmount(new BigDecimal("20000"));

        RepairDTO result = repairService.save(dto);

        ArgumentCaptor<RepairPayment> paymentCaptor = ArgumentCaptor.forClass(RepairPayment.class);
        verify(repairPaymentRepository).save(paymentCaptor.capture());
        assertEquals(new BigDecimal("20000"), paymentCaptor.getValue().getAmount());
        assertEquals(new BigDecimal("60000"), result.getTotalPaid());
        assertEquals(new BigDecimal("40000"), result.getOutstandingBalance());
        verify(repairStatusHistoryRepository, never()).save(any());
    }

    @Test
    void updateStatus_toRecibidaUsesSelectedReceiveDate() {
        Repair existing = existingRepair(null);
        existing.setReceiveDateTime(LocalDateTime.of(2026, 6, 19, 8, 0));
        LocalDateTime selectedReceiveDate = LocalDateTime.of(2026, 6, 20, 9, 15);
        when(repairRepository.findById("id-1")).thenReturn(Optional.of(existing));
        when(repairRepository.save(existing)).thenReturn(existing);

        repairService.updateStatus("id-1", RepairStatusEnum.RECIBIDA, selectedReceiveDate, null);

        assertEquals(RepairStatusEnum.RECIBIDA, existing.getStatus());
        assertEquals(selectedReceiveDate, existing.getReceiveDateTime());
    }

    @Test
    void updateStatus_toRetiradaUsesSelectedReturnDate() {
        Repair existing = existingRepair(null);
        LocalDateTime selectedReturnDate = LocalDateTime.of(2026, 6, 21, 18, 30);
        when(repairRepository.findById("id-1")).thenReturn(Optional.of(existing));
        when(repairRepository.save(existing)).thenReturn(existing);

        repairService.updateStatus("id-1", RepairStatusEnum.RETIRADA, null, selectedReturnDate);

        assertEquals(RepairStatusEnum.RETIRADA, existing.getStatus());
        assertEquals(selectedReturnDate, existing.getReturnDateTime());
    }

    @Test
    void updateStatus_toIntermediateStatusStoresHistoryWithCurrentDate() {
        Repair existing = existingRepair(null);
        existing.setStatus(RepairStatusEnum.RECIBIDA);
        when(repairRepository.findById("id-1")).thenReturn(Optional.of(existing));
        when(repairRepository.save(existing)).thenReturn(existing);

        repairService.updateStatus("id-1", RepairStatusEnum.HACIENDO);

        ArgumentCaptor<RepairStatusHistory> historyCaptor = ArgumentCaptor.forClass(RepairStatusHistory.class);
        verify(repairStatusHistoryRepository).save(historyCaptor.capture());
        RepairStatusHistory history = historyCaptor.getValue();

        assertEquals("id-1", history.getRepairId());
        assertEquals(RepairStatusEnum.HACIENDO, history.getStatus());
        assertEquals(LocalDateTime.of(2026, 6, 20, 17, 0), history.getChangedAt());
    }

    @Test
    void updateStatus_toRecibidaStoresSelectedReceiveDateInHistory() {
        Repair existing = existingRepair(null);
        existing.setStatus(RepairStatusEnum.POR_RECIBIR);
        LocalDateTime selectedReceiveDate = LocalDateTime.of(2026, 6, 20, 9, 15);
        when(repairRepository.findById("id-1")).thenReturn(Optional.of(existing));
        when(repairRepository.save(existing)).thenReturn(existing);

        repairService.updateStatus("id-1", RepairStatusEnum.RECIBIDA, selectedReceiveDate, null);

        ArgumentCaptor<RepairStatusHistory> historyCaptor = ArgumentCaptor.forClass(RepairStatusHistory.class);
        verify(repairStatusHistoryRepository).save(historyCaptor.capture());

        assertEquals(RepairStatusEnum.RECIBIDA, historyCaptor.getValue().getStatus());
        assertEquals(selectedReceiveDate, historyCaptor.getValue().getChangedAt());
    }

    @Test
    void updateStatus_sameStatusDoesNotStoreHistory() {
        Repair existing = existingRepair(null);
        existing.setStatus(RepairStatusEnum.HACIENDO);
        when(repairRepository.findById("id-1")).thenReturn(Optional.of(existing));
        when(repairRepository.save(existing)).thenReturn(existing);

        repairService.updateStatus("id-1", RepairStatusEnum.HACIENDO);

        verify(repairStatusHistoryRepository, never()).save(any());
    }

    @Test
    void getRepairById_returnsStatusHistoryInRepositoryOrder() {
        Repair existing = existingRepair(null);
        existing.setStatus(RepairStatusEnum.RETIRADA);
        when(repairRepository.findById("id-1")).thenReturn(Optional.of(existing));
        when(repairPartRepository.findByRepairId("id-1")).thenReturn(List.of());
        when(repairPaymentRepository.findByRepairId("id-1")).thenReturn(List.of());
        when(deviceObservationRepository.findByRepairIdOrderByObservedAtDesc("id-1")).thenReturn(List.of());

        RepairStatusHistoryView received = mock(RepairStatusHistoryView.class);
        LocalDateTime receivedAt = LocalDateTime.of(2026, 6, 20, 9, 15);
        when(received.getId()).thenReturn("h1");
        when(received.getRepairId()).thenReturn("id-1");
        when(received.getStatus()).thenReturn(RepairStatusEnum.RECIBIDA);
        when(received.getChangedAt()).thenReturn(receivedAt);

        RepairStatusHistoryView doing = mock(RepairStatusHistoryView.class);
        LocalDateTime doingAt = LocalDateTime.of(2026, 6, 20, 11, 0);
        when(doing.getId()).thenReturn("h2");
        when(doing.getRepairId()).thenReturn("id-1");
        when(doing.getStatus()).thenReturn(RepairStatusEnum.HACIENDO);
        when(doing.getChangedAt()).thenReturn(doingAt);

        when(repairStatusHistoryRepository.findByRepairIdOrderByChangedAtAscCreationDateTimeAsc("id-1")).thenReturn(List.of(received, doing));

        RepairDTO result = repairService.getRepairById("id-1");

        assertEquals(2, result.getStatusHistory().size());
        assertEquals(RepairStatusEnum.RECIBIDA, result.getStatusHistory().get(0).getStatus());
        assertEquals(receivedAt, result.getStatusHistory().get(0).getChangedAt());
        assertEquals(RepairStatusEnum.HACIENDO, result.getStatusHistory().get(1).getStatus());
        assertEquals(doingAt, result.getStatusHistory().get(1).getChangedAt());
    }

    @Test
    void findPage_usesRequestedSortAcrossWholeQuery() {
        Page<RepairListView> page = new PageImpl<>(List.of());
        when(repairRepository.findPage(any(), any(Pageable.class))).thenReturn(page);

        repairService.findPage(0, 10, "", null, null, null, "clientName", "asc");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repairRepository).findPage(any(), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();

        assertEquals("client.name: ASC, ignoring case,client.lastName: ASC, ignoring case,orderNumber: DESC", pageable.getSort().toString());
    }

    @Test
    void findPage_passesStatusFilterToRepository() {
        Page<RepairListView> page = new PageImpl<>(List.of());
        when(repairRepository.findPageByStatus(any(), any(), any(Pageable.class))).thenReturn(page);

        repairService.findPage(0, 10, "ada", null, null, RepairStatusEnum.RECIBIDA, null, null);

        verify(repairRepository).findPageByStatus(eq("ada"), eq(RepairStatusEnum.RECIBIDA), any(Pageable.class));
    }

    @Test
    void findPage_defaultSortStartsWithReceivedStatusOrder() {
        Page<RepairListView> page = new PageImpl<>(List.of());
        when(repairRepository.findPage(any(), any(Pageable.class))).thenReturn(page);

        repairService.findPage(0, 10, "", null, null, null, null, null);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repairRepository).findPage(any(), pageableCaptor.capture());
        String sort = pageableCaptor.getValue().getSort().toString();

        assertTrue(sort.contains("RepairStatusEnum.RECIBIDA THEN 0"));
        assertTrue(sort.contains("RepairStatusEnum.COBRADO_ESPERANDO_RETIRO THEN 4"));
        assertTrue(sort.contains("RepairStatusEnum.POR_RECIBIR THEN 5"));
        assertTrue(sort.contains("RepairStatusEnum.RETIRADA THEN 6"));
    }

    @Test
    void findPage_withDateRangeAndStatusUsesDedicatedRepositoryQuery() {
        Page<RepairListView> page = new PageImpl<>(List.of());
        LocalDateTime from = LocalDateTime.of(2026, 7, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 7, 2, 23, 59);
        when(repairRepository.findPageByStatusBetween(any(), any(), any(), any(), any(Pageable.class))).thenReturn(page);

        repairService.findPage(0, 10, "", from, to, RepairStatusEnum.HACIENDO, null, null);

        verify(repairRepository).findPageByStatusBetween(eq(""), eq(RepairStatusEnum.HACIENDO), eq(from), eq(to), any(Pageable.class));
    }

    private RepairDTO updateDto(String repairNotes) {
        RepairDTO dto = new RepairDTO();
        dto.setId("id-1");
        dto.setIdClient("c1");
        dto.setIdDevice("d1");
        dto.setDescription("Test");
        dto.setStatus(RepairStatusEnum.HACIENDO);
        dto.setLaborAmount(BigDecimal.ZERO);
        dto.setRepairNotes(repairNotes);
        return dto;
    }

    private Repair existingRepair(String repairNotes) {
        Repair repair = new Repair();
        repair.setId("id-1");
        repair.setRepairNotes(repairNotes);
        repair.setStatus(RepairStatusEnum.HACIENDO);
        return repair;
    }

    private void stubSavedRepair() {
        when(repairRepository.save(any(Repair.class))).thenAnswer(inv -> {
            Repair repair = inv.getArgument(0);
            if (repair.getId() == null) {
                repair.setId("new-id");
            }
            return repair;
        });
        when(repairPartRepository.findByRepairId(any())).thenReturn(List.of());
        when(repairPaymentRepository.findByRepairId(any())).thenReturn(List.of());
        when(deviceObservationRepository.findByRepairIdOrderByObservedAtDesc(any())).thenReturn(List.of());
        when(repairStatusHistoryRepository.findByRepairIdOrderByChangedAtAscCreationDateTimeAsc(any())).thenReturn(List.of());
    }
}
