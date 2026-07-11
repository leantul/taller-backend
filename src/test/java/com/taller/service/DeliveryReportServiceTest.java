package com.taller.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.taller.model.RepairPart;
import com.taller.model.RepairReport;
import com.taller.model.WorkshopSettings;
import com.taller.model.repository.RepairPartRepository;
import com.taller.model.repository.RepairReportHardwareItemRepository;
import com.taller.model.repository.RepairReportRepository;
import com.taller.model.repository.RepairReportSoftwareItemRepository;
import com.taller.model.repository.RepairRepository;
import com.taller.model.repository.projection.DeliveryReportSourceView;
import com.taller.model.repository.projection.RepairReportIdView;
import com.taller.model.repository.projection.RepairReportView;
import com.taller.resource.dto.RepairReportDTO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeliveryReportServiceTest {

    @Mock
    private RepairRepository repairRepository;
    @Mock
    private RepairPartRepository repairPartRepository;
    @Mock
    private RepairReportRepository repairReportRepository;
    @Mock
    private RepairReportHardwareItemRepository repairReportHardwareItemRepository;
    @Mock
    private RepairReportSoftwareItemRepository repairReportSoftwareItemRepository;
    @Mock
    private WorkshopSettingsService workshopSettingsService;
    @Mock
    private DeliveryReportPdfService deliveryReportPdfService;

    private DeliveryReportService deliveryReportService;

    @BeforeEach
    void setUp() {
        deliveryReportService = new DeliveryReportService(
                repairRepository,
                repairPartRepository,
                repairReportRepository,
                repairReportHardwareItemRepository,
                repairReportSoftwareItemRepository,
                workshopSettingsService,
                deliveryReportPdfService
        );
    }

    @Test
    void getByRepairId_buildsDefaultSnapshotFromRepair() {
        DeliveryReportSourceView source = repairSourceFixtureForDefaultReport();
        when(repairRepository.findDeliveryReportSourceById("repair-1")).thenReturn(Optional.of(source));
        when(repairReportRepository.findViewByRepairId("repair-1")).thenReturn(Optional.empty());
        when(repairPartRepository.findByRepairId("repair-1")).thenReturn(List.of(partFixture()));

        RepairReportDTO report = deliveryReportService.getByRepairId("repair-1");

        assertEquals("15", report.getOrderNumber());
        assertEquals("Ada", report.getClientName());
        assertEquals("Lovelace", report.getClientLastName());
        assertEquals("Notebook", report.getDeviceTypeName());
        assertEquals("Lenovo", report.getDeviceBrand());
        assertEquals("Detalle del presupuesto base para el reporte.", report.getWorkPerformed());
        assertEquals(1, report.getHardwareItems().size());
        assertFalse(report.getShowPartPrices());
        assertEquals(BigDecimal.valueOf(58000), report.getFinalAmount());
        assertNotNull(report.getIssuedAt());
    }

    @Test
    void save_persistsSnapshotAndNestedItems() {
        stubReportSave();

        RepairReportDTO dto = new RepairReportDTO();
        dto.setRepairId("repair-1");
        dto.setOrderNumber("15");
        dto.setIssuedAt(LocalDateTime.of(2026, 6, 22, 11, 0));
        dto.setClientName("Ada");
        dto.setClientLastName("Lovelace");
        dto.setClientPhone("1133445566");
        dto.setClientEmail("ada@example.com");
        dto.setDeviceTypeName("Notebook");
        dto.setDeviceBrand("Lenovo");
        dto.setDeviceModel("T14");
        dto.setDeviceSerialNumber("SN-1");
        dto.setReportedIssue("No enciende");
        dto.setWorkPerformed("Cambio de pantalla");
        dto.setFinalObservations("Lista para entregar");
        dto.setShowPartPrices(false);
        dto.setFinalAmount(BigDecimal.valueOf(58000));
        dto.setHardwareItems(List.of(hardwareItem(true)));
        dto.setSoftwareItems(List.of(softwareItem()));

        RepairReportDTO saved = deliveryReportService.save("repair-1", dto);

        assertEquals("report-1", saved.getId());
        ArgumentCaptor<RepairReport> reportCaptor = ArgumentCaptor.forClass(RepairReport.class);
        verify(repairReportRepository).save(reportCaptor.capture());
        assertEquals(true, reportCaptor.getValue().getShowPartPrices());
        verify(repairReportHardwareItemRepository).deleteAllByRepairReportId("report-1");
        verify(repairReportSoftwareItemRepository).deleteAllByRepairReportId("report-1");
        verify(repairReportHardwareItemRepository).saveAll(any());
        verify(repairReportSoftwareItemRepository).saveAll(any());
    }

    @Test
    void save_derivesShowPartPricesFalseWhenNoHardwareItemIncludesPrice() {
        stubReportSave();

        RepairReportDTO dto = new RepairReportDTO();
        dto.setRepairId("repair-1");
        dto.setShowPartPrices(true);
        dto.setHardwareItems(List.of(hardwareItem(false)));
        dto.setSoftwareItems(List.of());

        deliveryReportService.save("repair-1", dto);

        ArgumentCaptor<RepairReport> reportCaptor = ArgumentCaptor.forClass(RepairReport.class);
        verify(repairReportRepository).save(reportCaptor.capture());
        assertEquals(false, reportCaptor.getValue().getShowPartPrices());
    }

    @Test
    void generatePdf_delegatesToPdfService() {
        RepairReportView savedView = savedReportView();
        WorkshopSettings settings = WorkshopSettings.builder()
                .businessName("Taller")
                .reportTitle("REPORTE DE REPARACIÓN")
                .logoAssetPath(WorkshopSettingsService.DEFAULT_LOGO_ASSET_PATH)
                .build();

        when(repairReportRepository.findViewByRepairId("repair-1")).thenReturn(Optional.of(savedView));
        when(repairReportHardwareItemRepository.findViewByRepairReportIdOrderByCreationDateTimeAsc("report-1")).thenReturn(List.of());
        when(repairReportSoftwareItemRepository.findViewByRepairReportIdOrderByCreationDateTimeAsc("report-1")).thenReturn(List.of());
        when(workshopSettingsService.resolveSettings()).thenReturn(settings);
        when(deliveryReportPdfService.generate(any(), any())).thenReturn(new byte[] {1, 2, 3});

        byte[] pdf = deliveryReportService.generatePdf("repair-1");

        assertEquals(3, pdf.length);
        verify(deliveryReportPdfService).generate(any(), any());
    }

    private DeliveryReportSourceView repairSourceFixtureForDefaultReport() {
        DeliveryReportSourceView source = mock(DeliveryReportSourceView.class);
        when(source.getOrderNumber()).thenReturn("15");
        when(source.getClientName()).thenReturn("Ada");
        when(source.getClientLastName()).thenReturn("Lovelace");
        when(source.getClientPhone()).thenReturn("1133445566");
        when(source.getClientEmail()).thenReturn("ada@example.com");
        when(source.getDeviceTypeName()).thenReturn("Notebook");
        when(source.getDeviceBrand()).thenReturn("Lenovo");
        when(source.getDeviceModel()).thenReturn("T14");
        when(source.getDeviceSerialNumber()).thenReturn("SN-1");
        when(source.getReportedIssue()).thenReturn("No enciende");
        when(source.getWorkPerformed()).thenReturn("Detalle del presupuesto base para el reporte.");
        when(source.getFinalAmount()).thenReturn(BigDecimal.valueOf(58000));
        return source;
    }

    private DeliveryReportSourceView repairSourceFixtureForSave() {
        DeliveryReportSourceView source = mock(DeliveryReportSourceView.class);
        when(source.getOrderNumber()).thenReturn("15");
        return source;
    }

    private void stubReportSave() {
        DeliveryReportSourceView source = repairSourceFixtureForSave();
        when(repairRepository.findDeliveryReportSourceById("repair-1")).thenReturn(Optional.of(source));
        when(repairReportRepository.findIdViewByRepairId("repair-1")).thenReturn(Optional.empty());
        when(repairReportRepository.save(any(RepairReport.class))).thenAnswer(invocation -> {
            RepairReport report = invocation.getArgument(0);
            report.setId("report-1");
            return report;
        });
        RepairReportView savedView = savedReportView();
        when(repairReportRepository.findViewByRepairId("repair-1")).thenReturn(Optional.of(savedView));
        when(repairReportHardwareItemRepository.findViewByRepairReportIdOrderByCreationDateTimeAsc("report-1")).thenReturn(List.of());
        when(repairReportSoftwareItemRepository.findViewByRepairReportIdOrderByCreationDateTimeAsc("report-1")).thenReturn(List.of());
    }

    private RepairReportView savedReportView() {
        RepairReportView view = mock(RepairReportView.class);
        when(view.getId()).thenReturn("report-1");
        when(view.getRepairId()).thenReturn("repair-1");
        when(view.getOrderNumber()).thenReturn("15");
        when(view.getIssuedAt()).thenReturn(LocalDateTime.of(2026, 6, 22, 11, 0));
        when(view.getClientName()).thenReturn("Ada");
        when(view.getClientLastName()).thenReturn("Lovelace");
        when(view.getClientPhone()).thenReturn("1133445566");
        when(view.getClientEmail()).thenReturn("ada@example.com");
        when(view.getDeviceTypeName()).thenReturn("Notebook");
        when(view.getDeviceBrand()).thenReturn("Lenovo");
        when(view.getDeviceModel()).thenReturn("T14");
        when(view.getDeviceSerialNumber()).thenReturn("SN-1");
        when(view.getReportedIssue()).thenReturn("No enciende");
        when(view.getWorkPerformed()).thenReturn("Cambio de pantalla");
        when(view.getFinalObservations()).thenReturn("Lista para entregar");
        when(view.getShowPartPrices()).thenReturn(true);
        when(view.getFinalAmount()).thenReturn(BigDecimal.valueOf(58000));
        return view;
    }

    private RepairPart partFixture() {
        return RepairPart.builder()
                .name("Pantalla")
                .quantity(1)
                .provider("Stock local")
                .salePrice(BigDecimal.valueOf(25000))
                .build();
    }

    private com.taller.resource.dto.RepairReportHardwareItemDTO hardwareItem(boolean includePrice) {
        com.taller.resource.dto.RepairReportHardwareItemDTO item = new com.taller.resource.dto.RepairReportHardwareItemDTO();
        item.setPartName("Pantalla");
        item.setQuantity(1);
        item.setDetail("IPS full HD");
        item.setUnitPrice(BigDecimal.valueOf(25000));
        item.setIncludePrice(includePrice);
        return item;
    }

    private com.taller.resource.dto.RepairReportSoftwareItemDTO softwareItem() {
        com.taller.resource.dto.RepairReportSoftwareItemDTO item = new com.taller.resource.dto.RepairReportSoftwareItemDTO();
        item.setSoftwareName("Google Chrome");
        item.setDetail("Última versión estable");
        return item;
    }
}
