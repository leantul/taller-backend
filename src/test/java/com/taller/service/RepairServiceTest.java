package com.taller.service;

import com.taller.model.Repair;
import com.taller.model.enums.RepairStatusEnum;
import com.taller.model.repository.DeviceObservationRepository;
import com.taller.model.repository.RepairPartRepository;
import com.taller.model.repository.RepairPaymentRepository;
import com.taller.model.repository.RepairRepository;
import com.taller.resource.dto.RepairDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
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
}
