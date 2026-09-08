package com.taller.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.taller.model.repository.DeviceRepository;
import com.taller.model.repository.RepairRepository;
import com.taller.model.repository.projection.DashboardCountsView;
import com.taller.model.repository.projection.RepairMonthlyCountView;
import com.taller.resource.dto.DashboardOverviewDTO;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private RepairService repairService;

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private RepairRepository repairRepository;

    @Test
    void overview_buildsTwelveMonthlyBucketsAndUsesOperationalAggregates() {
        DashboardCountsView counts = mock(DashboardCountsView.class);
        RepairMonthlyCountView august = mock(RepairMonthlyCountView.class);
        RepairMonthlyCountView september = mock(RepairMonthlyCountView.class);
        when(counts.getClientCount()).thenReturn(36L);
        when(counts.getDeviceCount()).thenReturn(45L);
        when(counts.getRepairCount()).thenReturn(47L);
        when(august.getMonth()).thenReturn(LocalDateTime.of(2026, 8, 1, 0, 0));
        when(august.getTotal()).thenReturn(3L);
        when(september.getMonth()).thenReturn(LocalDateTime.of(2026, 9, 1, 0, 0));
        when(september.getTotal()).thenReturn(5L);
        when(repairRepository.dashboardCounts()).thenReturn(counts);
        when(repairRepository.countByStatus()).thenReturn(List.of());
        when(repairRepository.countReceivedByMonth(any(), any())).thenReturn(List.of(august, september));
        when(repairRepository.averageCompletedTurnaroundDays()).thenReturn(4.26);
        when(repairRepository.countOverdueOpenRepairs(any())).thenReturn(2L);
        when(repairRepository.findOldestLastRepairByDevice(any())).thenReturn(List.of());

        Clock clock = Clock.fixed(Instant.parse("2026-09-08T15:00:00Z"), ZoneId.of("America/Argentina/Buenos_Aires"));
        DashboardOverviewDTO overview = new DashboardService(repairService, deviceRepository, repairRepository, clock).overview();

        assertEquals(12, overview.getMonthlyReceivedRepairs().size());
        assertEquals("Oct 2025", overview.getMonthlyReceivedRepairs().getFirst().getLabel());
        assertEquals(0L, overview.getMonthlyReceivedRepairs().getFirst().getValue());
        assertEquals(3L, overview.getMonthlyReceivedRepairs().get(10).getValue());
        assertEquals(5L, overview.getMonthlyReceivedRepairs().get(11).getValue());
        assertEquals(4.3, overview.getAverageTurnaroundDays());
        assertEquals(2L, overview.getOverdueOpenRepairs());
        verify(repairRepository).countReceivedByMonth(
                eq(LocalDateTime.of(2025, 10, 1, 0, 0)),
                eq(LocalDateTime.of(2026, 10, 1, 0, 0)));
        verify(repairRepository).countOverdueOpenRepairs(eq(LocalDateTime.of(2026, 9, 1, 0, 0)));
    }

    @Test
    void overview_usesZeroWhenThereAreNoCompletedTurnaroundRecords() {
        DashboardCountsView counts = mock(DashboardCountsView.class);
        when(repairRepository.dashboardCounts()).thenReturn(counts);
        when(repairRepository.countByStatus()).thenReturn(List.of());
        when(repairRepository.countReceivedByMonth(any(), any())).thenReturn(List.of());
        when(repairRepository.averageCompletedTurnaroundDays()).thenReturn(null);
        when(repairRepository.countOverdueOpenRepairs(any())).thenReturn(0L);
        when(repairRepository.findOldestLastRepairByDevice(any())).thenReturn(List.of());

        DashboardOverviewDTO overview = new DashboardService(
                repairService,
                deviceRepository,
                repairRepository,
                Clock.systemUTC()).overview();

        assertEquals(0.0, overview.getAverageTurnaroundDays());
    }
}
