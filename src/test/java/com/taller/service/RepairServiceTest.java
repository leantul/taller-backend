package com.taller.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taller.model.Repair;
import com.taller.model.enums.RepairStatusEnum;
import com.taller.model.repository.DeviceObservationRepository;
import com.taller.model.repository.RepairPartRepository;
import com.taller.model.repository.RepairPaymentRepository;
import com.taller.model.repository.RepairRepository;
import com.taller.model.repository.projection.RepairListView;
import com.taller.model.repository.projection.StatusBoardRepairView;
import com.taller.resource.dto.RepairDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepairServiceTest {

    @Mock
    private RepairRepository repairRepository;
    @Mock
    private RepairPartRepository repairPartRepository;
    @Mock
    private RepairPaymentRepository repairPaymentRepository;
    @Mock
    private DeviceObservationRepository deviceObservationRepository;

    private RepairService repairService;

    @BeforeEach
    void setUp() {
        repairService = new RepairService(repairRepository, repairPartRepository, repairPaymentRepository, deviceObservationRepository);
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

        RepairDTO dto = new RepairDTO();
        dto.setIdClient("c1");
        dto.setIdDevice("d1");
        dto.setDescription("Test");
        dto.setStatus(RepairStatusEnum.POR_RECIBIR);

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

        RepairDTO dto = new RepairDTO();
        dto.setIdClient("c1");
        dto.setIdDevice("d1");
        dto.setDescription("Test");
        dto.setStatus(RepairStatusEnum.RETIRADA);

        RepairDTO saved = repairService.save(dto);

        assertNotNull(saved.getReturnDateTime());
    }

    @Test
    void save_newRepair_ignoresRepairNotes() {
        stubSavedRepair();
        when(repairRepository.nextOrderValue()).thenReturn(3L);

        RepairDTO dto = new RepairDTO();
        dto.setIdClient("c1");
        dto.setIdDevice("d1");
        dto.setDescription("Test");
        dto.setStatus(RepairStatusEnum.POR_RECIBIR);
        dto.setRepairNotes("No debe guardarse durante el alta");

        RepairDTO saved = repairService.save(dto);

        assertNull(saved.getRepairNotes());
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

    private RepairDTO updateDto(String repairNotes) {
        RepairDTO dto = new RepairDTO();
        dto.setId("id-1");
        dto.setIdClient("c1");
        dto.setIdDevice("d1");
        dto.setDescription("Test");
        dto.setStatus(RepairStatusEnum.HACIENDO);
        dto.setRepairNotes(repairNotes);
        return dto;
    }

    private Repair existingRepair(String repairNotes) {
        Repair repair = new Repair();
        repair.setId("id-1");
        repair.setRepairNotes(repairNotes);
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
    }
}
