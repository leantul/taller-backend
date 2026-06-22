package com.taller.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.taller.model.WorkshopSettings;
import com.taller.resource.dto.RepairReportDTO;
import com.taller.resource.dto.RepairReportHardwareItemDTO;
import com.taller.resource.dto.RepairReportSoftwareItemDTO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class DeliveryReportPdfService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String WHATSAPP_ICON_ASSET_PATH = "report/whatsapp-icon.png";
    private static final String INSTAGRAM_ICON_ASSET_PATH = "report/instagram-icon.png";

    public byte[] generate(RepairReportDTO report, WorkshopSettings settings) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(buildHtml(report, settings), null);
            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo generar el PDF del reporte", exception);
        }
    }

    private String buildHtml(RepairReportDTO report, WorkshopSettings settings) {
        String logo = toDataUri(settings.getLogoAssetPath());
        String whatsappIcon = toDataUri(WHATSAPP_ICON_ASSET_PATH);
        String instagramIcon = toDataUri(INSTAGRAM_ICON_ASSET_PATH);
        List<RepairReportHardwareItemDTO> hardwareItems = visibleHardwareItems(report.getHardwareItems());
        List<RepairReportSoftwareItemDTO> softwareItems = visibleSoftwareItems(report.getSoftwareItems());
        boolean showPriceColumn = Boolean.TRUE.equals(report.getShowPartPrices())
                && hardwareItems.stream().anyMatch(item -> Boolean.TRUE.equals(item.getIncludePrice()));
        String hardwareSection = hardwareItems.isEmpty()
                ? ""
                : """
                <div class="section-title">Repuestos cambiados</div>
                %s
                """.formatted(hardwareTable(hardwareItems, showPriceColumn));
        String softwareSection = softwareItems.isEmpty()
                ? ""
                : """
                <div class="section-title">Software instalado / configurado</div>
                %s
                """.formatted(softwareTable(softwareItems));

        return """
                <!DOCTYPE html>
                <html lang="es">
                  <head>
                    <meta charset="UTF-8" />
                    <style>
                      @page { size: A4; margin: 20mm 16mm 18mm; }
                      body { font-family: Helvetica, Arial, sans-serif; color: #152126; font-size: 11px; }
                      .header { display: table; width: 100%%; margin-bottom: 18px; }
                      .header > div { display: table-cell; vertical-align: middle; }
                      .header-left { width: 26%%; }
                      .header-center { width: 48%%; text-align: center; }
                      .header-right { width: 26%%; text-align: right; }
                      .logo { width: 150px; height: auto; }
                      .title { font-size: 23px; font-weight: 700; letter-spacing: 1px; }
                      .contact-line { margin-bottom: 6px; white-space: nowrap; font-size: 10.5px; color: #52606b; }
                      .contact-line img { width: 12px; height: 12px; vertical-align: -2px; margin-right: 5px; }
                      .rule { height: 1px; background: #d5dde3; margin: 8px 0 16px; }
                      .meta { display: table; width: 100%%; margin-bottom: 16px; }
                      .meta > div { display: table-cell; vertical-align: top; }
                      .meta-box { width: 50%%; padding: 12px 14px; border: 1px solid #d5dde3; }
                      .meta-box + .meta-box { border-left: none; }
                      .section-title { font-size: 10px; font-weight: 700; letter-spacing: 1px; color: #0c8a9f; margin-bottom: 8px; text-transform: uppercase; }
                      .meta-row { margin-bottom: 6px; }
                      .meta-row strong { display: inline-block; min-width: 64px; }
                      .summary-strip { display: table; width: 100%%; margin-bottom: 14px; }
                      .summary-strip > div { display: table-cell; vertical-align: middle; }
                      .summary-label { font-size: 10px; letter-spacing: 1px; color: #70808d; text-transform: uppercase; }
                      .summary-value { font-size: 16px; font-weight: 700; }
                      .text-panel { margin-bottom: 14px; border: 1px solid #d5dde3; padding: 12px 14px; }
                      .text-panel p { margin: 0; white-space: pre-wrap; line-height: 1.55; }
                      table { width: 100%%; border-collapse: collapse; margin-bottom: 14px; }
                      th { background: #eef4f6; color: #29404d; text-transform: uppercase; font-size: 9.5px; letter-spacing: .8px; }
                      th, td { border: 1px solid #d5dde3; padding: 8px 9px; text-align: left; vertical-align: top; }
                      td.num, th.num { text-align: right; white-space: nowrap; }
                      .empty { color: #70808d; font-style: italic; }
                      .footer { margin-top: 10px; text-align: right; font-size: 9.5px; color: #70808d; }
                    </style>
                  </head>
                  <body>
                    <div class="header">
                      <div class="header-left"><img class="logo" src="%s" alt="Logo taller" /></div>
                      <div class="header-center"><div class="title">REPORTE DE REPARACIÓN</div></div>
                      <div class="header-right">
                        <div class="contact-line">%s %s</div>
                        <div class="contact-line">%s %s</div>
                      </div>
                    </div>
                    <div class="rule"></div>
                    <div class="summary-strip">
                      <div>
                        <div class="summary-label">Numero de reparacion</div>
                        <div class="summary-value">#%s</div>
                      </div>
                      <div style="text-align:right;">
                        <div class="summary-label">Fecha de emision</div>
                        <div class="summary-value">%s</div>
                      </div>
                    </div>
                    <div class="meta">
                      <div class="meta-box">
                        <div class="section-title">Cliente</div>
                        <div class="meta-row"><strong>Nombre</strong>%s %s</div>
                        <div class="meta-row"><strong>Telefono</strong>%s</div>
                        <div class="meta-row"><strong>Email</strong>%s</div>
                        <div class="meta-row"><strong>DNI</strong>%s</div>
                      </div>
                      <div class="meta-box">
                        <div class="section-title">Equipo</div>
                        <div class="meta-row"><strong>Tipo</strong>%s</div>
                        <div class="meta-row"><strong>Marca</strong>%s</div>
                        <div class="meta-row"><strong>Modelo</strong>%s</div>
                        <div class="meta-row"><strong>Serie</strong>%s</div>
                      </div>
                    </div>
                    %s
                    %s
                    %s
                    %s
                    <div class="summary-strip">
                      <div>
                        <div class="summary-label">Monto final</div>
                        <div class="summary-value">%s</div>
                      </div>
                    </div>
                    <div class="footer">%s · Orden #%s</div>
                  </body>
                </html>
                """.formatted(
                logo,
                contactIcon(whatsappIcon), escapeText(blankFallback(settings.getWhatsapp())),
                contactIcon(instagramIcon), escapeText(blankFallback(settings.getInstagram())),
                escapeText(blankFallback(report.getOrderNumber())),
                escapeText(report.getIssuedAt() != null ? DATE_TIME_FORMATTER.format(report.getIssuedAt()) : "-"),
                escapeText(blankFallback(report.getClientName())),
                escapeText(blankFallback(report.getClientLastName())),
                escapeText(blankFallback(report.getClientPhone())),
                escapeText(blankFallback(report.getClientEmail())),
                escapeText(blankFallback(report.getClientDni())),
                escapeText(blankFallback(report.getDeviceTypeName())),
                escapeText(blankFallback(report.getDeviceBrand())),
                escapeText(blankFallback(report.getDeviceModel())),
                escapeText(blankFallback(report.getDeviceSerialNumber())),
                textPanel("Falla reportada", report.getReportedIssue()),
                textPanel("Trabajo realizado", report.getWorkPerformed(), report.getFinalObservations()),
                hardwareSection,
                softwareSection,
                money(report.getFinalAmount()),
                escapeText(blankFallback(settings.getBusinessName())),
                escapeText(blankFallback(report.getOrderNumber()))
        );
    }

    private String textPanel(String title, String... values) {
        StringBuilder content = new StringBuilder();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                if (!content.isEmpty()) {
                    content.append("\n\n");
                }
                content.append(value.trim());
            }
        }
        String safeValue = content.isEmpty() ? "Sin detalle cargado" : content.toString();
        return """
                <div class="text-panel">
                  <div class="section-title">%s</div>
                  <p>%s</p>
                </div>
                """.formatted(escapeText(title), escapeText(safeValue));
    }

    private String hardwareTable(List<RepairReportHardwareItemDTO> items, boolean showPriceColumn) {
        StringBuilder rows = new StringBuilder();
        for (RepairReportHardwareItemDTO item : items) {
            rows.append("<tr>")
                    .append("<td>").append(escapeText(blankFallback(item.getPartName()))).append("</td>")
                    .append("<td class=\"num\">").append(item.getQuantity() != null ? item.getQuantity() : 1).append("</td>")
                    .append("<td>").append(escapeText(blankFallback(item.getDetail()))).append("</td>");
            if (showPriceColumn) {
                rows.append("<td class=\"num\">")
                        .append(Boolean.TRUE.equals(item.getIncludePrice()) ? money(item.getUnitPrice()) : "-")
                        .append("</td>");
            }
            rows.append("</tr>");
        }
        String header = showPriceColumn
                ? "<thead><tr><th>Repuesto</th><th class=\"num\">Cantidad</th><th>Detalle</th><th class=\"num\">Precio</th></tr></thead>"
                : "<thead><tr><th>Repuesto</th><th class=\"num\">Cantidad</th><th>Detalle</th></tr></thead>";
        return "<table>" + header + "<tbody>" + rows + "</tbody></table>";
    }

    private String softwareTable(List<RepairReportSoftwareItemDTO> items) {
        StringBuilder rows = new StringBuilder();
        for (RepairReportSoftwareItemDTO item : items) {
            rows.append("<tr>")
                    .append("<td>").append(escapeText(blankFallback(item.getSoftwareName()))).append("</td>")
                    .append("<td>").append(escapeText(blankFallback(item.getDetail()))).append("</td>")
                    .append("</tr>");
        }
        return "<table><thead><tr><th>Software</th><th>Detalle</th></tr></thead><tbody>" + rows + "</tbody></table>";
    }

    private List<RepairReportHardwareItemDTO> visibleHardwareItems(List<RepairReportHardwareItemDTO> items) {
        if (items == null) {
            return List.of();
        }
        return items.stream()
                .filter(item -> item.getPartName() != null && !item.getPartName().isBlank())
                .toList();
    }

    private List<RepairReportSoftwareItemDTO> visibleSoftwareItems(List<RepairReportSoftwareItemDTO> items) {
        if (items == null) {
            return List.of();
        }
        return items.stream()
                .filter(item -> item.getSoftwareName() != null && !item.getSoftwareName().isBlank())
                .toList();
    }

    private String blankFallback(String value) {
        return value == null || value.isBlank() ? "-" : value.trim();
    }

    private String money(BigDecimal value) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
        return format.format(value != null ? value : BigDecimal.ZERO);
    }

    private String toDataUri(String assetPath) {
        String path = assetPath == null || assetPath.isBlank() ? WorkshopSettingsService.DEFAULT_LOGO_ASSET_PATH : assetPath;
        ClassPathResource resource = new ClassPathResource(path);
        try (InputStream inputStream = resource.getInputStream()) {
            byte[] bytes = inputStream.readAllBytes();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo cargar el logo del reporte", exception);
        }
    }

    private String escapeText(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("\n", "<br />");
    }

    private String contactIcon(String dataUri) {
        return "<img alt=\"\" src=\"" + dataUri + "\" />";
    }
}
