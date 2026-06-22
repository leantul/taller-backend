package com.taller.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.taller.model.WorkshopSettings;
import com.taller.resource.dto.RepairReportDTO;
import com.taller.resource.dto.RepairReportHardwareItemDTO;
import com.taller.resource.dto.RepairReportSoftwareItemDTO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

class DeliveryReportPdfServiceTest {

    private final DeliveryReportPdfService deliveryReportPdfService = new DeliveryReportPdfService();

    @Test
    void generate_createsPdfWithoutPartPriceWhenDisabled() throws Exception {
        byte[] pdf = deliveryReportPdfService.generate(report(false, false), settings());

        String text = extractText(pdf);

        assertTrue(text.contains("REPORTE"));
        assertTrue(text.contains("REPARACIÓN"));
        assertTrue(text.contains("Google Chrome"));
        assertTrue(text.contains("22/06/2026"));
        assertFalse(text.contains("11:00"));
        assertFalse(text.contains("25.000"));
        assertFalse(text.toLowerCase().contains("secreto"));
        assertFalse(text.contains("ESPERANDO_RETIRO"));
    }

    @Test
    void generate_includesPartPriceWhenEnabled() throws Exception {
        byte[] pdf = deliveryReportPdfService.generate(report(true, true), settings());

        String text = extractText(pdf);

        assertTrue(text.contains("25.000"));
        assertTrue(text.contains("$"));
    }

    @Test
    void generate_hidesEmptyHardwareAndSoftwareSections() throws Exception {
        RepairReportDTO report = report(false, false);
        report.setHardwareItems(List.of());
        report.setSoftwareItems(List.of());

        byte[] pdf = deliveryReportPdfService.generate(report, settings());
        String text = extractText(pdf);

        assertFalse(text.contains("Repuestos cambiados"));
        assertFalse(text.contains("Software instalado"));
    }

    private RepairReportDTO report(boolean showPartPrices, boolean includePrice) {
        RepairReportHardwareItemDTO hardwareItem = new RepairReportHardwareItemDTO();
        hardwareItem.setPartName("Pantalla");
        hardwareItem.setQuantity(1);
        hardwareItem.setDetail("IPS Full HD");
        hardwareItem.setUnitPrice(BigDecimal.valueOf(25000));
        hardwareItem.setIncludePrice(includePrice);

        RepairReportSoftwareItemDTO softwareItem = new RepairReportSoftwareItemDTO();
        softwareItem.setSoftwareName("Google Chrome");
        softwareItem.setDetail("Última versión estable");

        RepairReportDTO report = new RepairReportDTO();
        report.setRepairId("repair-1");
        report.setOrderNumber("15");
        report.setIssuedAt(LocalDateTime.of(2026, 6, 22, 11, 0));
        report.setClientName("Ada");
        report.setClientLastName("Lovelace");
        report.setClientPhone("1133445566");
        report.setClientEmail("ada@example.com");
        report.setClientDni("30111222");
        report.setDeviceTypeName("Notebook");
        report.setDeviceBrand("Lenovo");
        report.setDeviceModel("T14");
        report.setDeviceSerialNumber("SN-1");
        report.setReportedIssue("No enciende");
        report.setWorkPerformed("Cambio de pantalla");
        report.setFinalObservations("Pruebas completadas");
        report.setShowPartPrices(showPartPrices);
        report.setFinalAmount(BigDecimal.valueOf(58000));
        report.setHardwareItems(List.of(hardwareItem));
        report.setSoftwareItems(List.of(softwareItem));
        return report;
    }

    private WorkshopSettings settings() {
        return WorkshopSettings.builder()
                .businessName("Taller")
                .whatsapp("11 3344 5566")
                .instagram("@taller")
                .reportTitle("REPORTE DE REPARACIÓN")
                .logoAssetPath(WorkshopSettingsService.DEFAULT_LOGO_ASSET_PATH)
                .build();
    }

    private String extractText(byte[] pdf) throws Exception {
        try (PDDocument document = PDDocument.load(pdf)) {
            return new PDFTextStripper().getText(document);
        }
    }
}
