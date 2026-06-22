package com.taller.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.taller.model.Client;
import com.taller.model.Device;
import com.taller.model.DeviceType;
import com.taller.model.Repair;
import com.taller.model.RepairPart;
import com.taller.model.RepairReport;
import com.taller.model.WorkshopSettings;
import com.taller.model.repository.RepairPartRepository;
import com.taller.model.repository.RepairReportHardwareItemRepository;
import com.taller.model.repository.RepairReportRepository;
import com.taller.model.repository.RepairReportSoftwareItemRepository;
import com.taller.model.repository.RepairRepository;
import com.taller.resource.dto.RepairReportDTO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
        Repair repair = repairFixture();
        when(repairRepository.findById("repair-1")).thenReturn(Optional.of(repair));
        when(repairReportRepository.findByRepairId("repair-1")).thenReturn(Optional.empty());
        when(repairPartRepository.findByRepairId("repair-1")).thenReturn(List.of(partFixture()));

        RepairReportDTO report = deliveryReportService.getByRepairId("repair-1");

        assertEquals("15", report.getOrderNumber());
        assertEquals("Ada", report.getClientName());
        assertEquals("Lovelace", report.getClientLastName());
        assertEquals("Notebook", report.getDeviceTypeName());
        assertEquals("Lenovo", report.getDeviceBrand());
        assertEquals("Pantalla reemplazada y pruebas completas.", report.getWorkPerformed());
        assertEquals(1, report.getHardwareItems().size());
        assertFalse(report.getShowPartPrices());
        assertEquals(BigDecimal.valueOf(58000), report.getFinalAmount());
        assertNotNull(report.getIssuedAt());
    }

    @Test
    void save_persistsSnapshotAndNestedItems() {
        Repair repair = repairFixture();
        when(repairRepository.findById("repair-1")).thenReturn(Optional.of(repair));
        when(repairReportRepository.findByRepairId("repair-1")).thenReturn(Optional.empty());
        when(repairReportRepository.save(any(RepairReport.class))).thenAnswer(invocation -> {
            RepairReport report = invocation.getArgument(0);
            report.setId("report-1");
            return report;
        });

        RepairReportDTO dto = new RepairReportDTO();
        dto.setRepairId("repair-1");
        dto.setOrderNumber("15");
        dto.setIssuedAt(LocalDateTime.of(2026, 6, 22, 11, 0));
        dto.setClientName("Ada");
        dto.setClientLastName("Lovelace");
        dto.setClientPhone("1133445566");
        dto.setClientEmail("ada@example.com");
        dto.setClientDni("30111222");
        dto.setDeviceTypeName("Notebook");
        dto.setDeviceBrand("Lenovo");
        dto.setDeviceModel("T14");
        dto.setDeviceSerialNumber("SN-1");
        dto.setReportedIssue("No enciende");
        dto.setWorkPerformed("Cambio de pantalla");
        dto.setFinalObservations("Lista para entregar");
        dto.setShowPartPrices(true);
        dto.setFinalAmount(BigDecimal.valueOf(58000));
        dto.setHardwareItems(List.of(hardwareItem()));
        dto.setSoftwareItems(List.of(softwareItem()));

        RepairReportDTO saved = deliveryReportService.save("repair-1", dto);

        assertEquals("report-1", saved.getId());
        verify(repairReportHardwareItemRepository).deleteAllByRepairReportId("report-1");
        verify(repairReportSoftwareItemRepository).deleteAllByRepairReportId("report-1");
        verify(repairReportHardwareItemRepository).saveAll(any());
        verify(repairReportSoftwareItemRepository).saveAll(any());
    }

    @Test
    void generatePdf_delegatesToPdfService() {
        RepairReport existing = RepairReport.builder()
                .repairId("repair-1")
                .orderNumber("15")
                .issuedAt(LocalDateTime.of(2026, 6, 22, 11, 0))
                .showPartPrices(false)
                .finalAmount(BigDecimal.TEN)
                .build();
        existing.setId("report-1");

        WorkshopSettings settings = WorkshopSettings.builder()
                .businessName("Taller")
                .logoAssetPath(WorkshopSettingsService.DEFAULT_LOGO_ASSET_PATH)
                .build();

        when(repairReportRepository.findByRepairId("repair-1")).thenReturn(Optional.of(existing));
        when(repairReportHardwareItemRepository.findByRepairReportIdOrderByCreationDateTimeAsc("report-1")).thenReturn(List.of());
        when(repairReportSoftwareItemRepository.findByRepairReportIdOrderByCreationDateTimeAsc("report-1")).thenReturn(List.of());
        when(workshopSettingsService.resolveSettings()).thenReturn(settings);
        when(deliveryReportPdfService.generate(any(), any())).thenReturn(new byte[] {1, 2, 3});

        byte[] pdf = deliveryReportService.generatePdf("repair-1");

        assertEquals(3, pdf.length);
        verify(deliveryReportPdfService).generate(any(), any());
    }

    private Repair repairFixture() {
        Client client = new Client();
        client.setName("Ada");
        client.setLastName("Lovelace");
        client.setPhone("1133445566");
        client.setEmail("ada@example.com");
        client.setDni("30111222");

        DeviceType deviceType = new DeviceType();
        deviceType.setName("Notebook");

        Device device = new Device();
        device.setBrand("Lenovo");
        device.setModel("T14");
        device.setSerialNumber("SN-1");
        device.setPassword("secreto");
        device.setDeviceType(deviceType);

        Repair repair = new Repair();
        repair.setId("repair-1");
        repair.setOrderNumber("15");
        repair.setDescription("No enciende");
        repair.setRepairNotes("Pantalla reemplazada y pruebas completas.");
        repair.setQuoteNotes("Servicio integral");
        repair.setPrice(BigDecimal.valueOf(58000));
        repair.setClient(client);
        repair.setDevice(device);
        return repair;
    }

    private RepairPart partFixture() {
        return RepairPart.builder()
                .name("Pantalla")
                .quantity(1)
                .provider("Stock local")
                .salePrice(BigDecimal.valueOf(25000))
                .build();
    }

    private com.taller.resource.dto.RepairReportHardwareItemDTO hardwareItem() {
        com.taller.resource.dto.RepairReportHardwareItemDTO item = new com.taller.resource.dto.RepairReportHardwareItemDTO();
        item.setPartName("Pantalla");
        item.setQuantity(1);
        item.setDetail("IPS full HD");
        item.setUnitPrice(BigDecimal.valueOf(25000));
        item.setIncludePrice(true);
        return item;
    }

    private com.taller.resource.dto.RepairReportSoftwareItemDTO softwareItem() {
        com.taller.resource.dto.RepairReportSoftwareItemDTO item = new com.taller.resource.dto.RepairReportSoftwareItemDTO();
        item.setSoftwareName("Google Chrome");
        item.setDetail("Última versión estable");
        return item;
    }
}
