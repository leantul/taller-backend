import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { InputNumberModule } from 'primeng/inputnumber';
import { MessageService } from 'primeng/api';
import { forkJoin } from 'rxjs';
import { ApiService } from '../../core/services/api.service';
import { DeliveryReport, SoftwareCatalogItem } from '../models/delivery-report.model';
import { Repair } from '../models/repair.model';

@Component({
  selector: 'app-delivery-report-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule, DialogModule, ButtonModule, InputNumberModule],
  template: `
    <p-dialog
      header="Reporte de reparación"
      [(visible)]="visible"
      [modal]="true"
      [draggable]="false"
      [resizable]="false"
      [style]="{ width: '74rem', maxWidth: '96vw' }"
      [contentStyle]="{ overflow: 'auto', padding: '0.75rem 0.75rem 0.5rem', maxHeight: '86vh' }">
      @if (report) {
        <div class="report-dialog-shell">
          <section class="report-dialog-banner">
            <div>
              <span class="eyebrow">Entrega</span>
              <strong>Orden #{{ report.orderNumber || '-' }}</strong>
            </div>
            <div class="report-banner-actions">
              <button class="secondary-button" type="button" [disabled]="isSaving || isGenerating" (click)="save()">Guardar</button>
              <button class="secondary-button" type="button" [disabled]="isSaving || isGenerating" (click)="previewPdf()">Previsualizar PDF</button>
              <button class="primary-button" type="button" [disabled]="isSaving || isGenerating" (click)="downloadPdf()">Descargar PDF</button>
            </div>
          </section>

          <div class="report-dialog-grid">
            <section class="report-card">
              <div class="section-title">Cliente</div>
              <div class="report-form-grid two-cols">
                <label class="field"><span>Nombre</span><input class="control" [(ngModel)]="report.clientName" /></label>
                <label class="field"><span>Apellido</span><input class="control" [(ngModel)]="report.clientLastName" /></label>
                <label class="field"><span>Teléfono</span><input class="control" [(ngModel)]="report.clientPhone" /></label>
                <label class="field"><span>Email</span><input class="control" [(ngModel)]="report.clientEmail" /></label>
                <label class="field"><span>DNI</span><input class="control" [(ngModel)]="report.clientDni" /></label>
              </div>
            </section>

            <section class="report-card">
              <div class="section-title">Equipo</div>
              <div class="report-form-grid two-cols">
                <label class="field"><span>Tipo</span><input class="control" [(ngModel)]="report.deviceTypeName" /></label>
                <label class="field"><span>Número de reparación</span><input class="control" [(ngModel)]="report.orderNumber" /></label>
                <label class="field"><span>Marca</span><input class="control" [(ngModel)]="report.deviceBrand" /></label>
                <label class="field"><span>Modelo</span><input class="control" [(ngModel)]="report.deviceModel" /></label>
                <label class="field"><span>Serie</span><input class="control" [(ngModel)]="report.deviceSerialNumber" /></label>
                <label class="field"><span>Fecha de emisión</span><input class="control" type="date" [(ngModel)]="issuedAtLocal" /></label>
              </div>
            </section>
          </div>

          <section class="report-card">
            <div class="section-title">Detalle técnico</div>
            <div class="report-form-grid">
              <label class="field"><span>Falla reportada</span><textarea class="control" rows="4" [(ngModel)]="report.reportedIssue"></textarea></label>
              <label class="field"><span>Trabajo realizado</span><textarea class="control" rows="5" [(ngModel)]="report.workPerformed"></textarea></label>
              <label class="field"><span>Observaciones finales</span><textarea class="control" rows="3" [(ngModel)]="report.finalObservations"></textarea></label>
            </div>
          </section>

          <section class="report-card">
            <div class="report-card-head">
              <div>
                <div class="section-title">Hardware</div>
                <small>Los repuestos se precargan desde la reparación y siguen siendo editables.</small>
              </div>
              <label class="inline-checkbox">
                <input type="checkbox" [(ngModel)]="report.showPartPrices" />
                <span>Mostrar columna de precios</span>
              </label>
            </div>
            <div class="native-table-wrap compact-table-wrap">
              <table class="native-table compact-native-table">
                <thead>
                  <tr>
                    <th>Repuesto</th>
                    <th>Cant.</th>
                    <th>Detalle</th>
                    <th>Precio</th>
                    <th>Mostrar</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  @for (item of report.hardwareItems; track item.id || $index) {
                    <tr>
                      <td><input class="control" [(ngModel)]="item.partName" /></td>
                      <td><input class="control" type="number" min="1" [(ngModel)]="item.quantity" /></td>
                      <td><input class="control" [(ngModel)]="item.detail" placeholder="Detalle o referencia" /></td>
                      <td><p-inputNumber [(ngModel)]="item.unitPrice" mode="currency" currency="ARS" locale="es-AR" [min]="0"></p-inputNumber></td>
                      <td class="centered-cell"><input type="checkbox" [(ngModel)]="item.includePrice" /></td>
                      <td class="centered-cell"><button class="icon-button" type="button" (click)="removeHardwareItem($index)"><i class="pi pi-trash"></i></button></td>
                    </tr>
                  } @empty {
                    <tr><td colspan="6" class="empty-cell">Sin repuestos cargados.</td></tr>
                  }
                </tbody>
              </table>
            </div>
            <div class="card-inline-actions">
              <button class="secondary-button" type="button" (click)="addHardwareItem()">Agregar repuesto</button>
            </div>
          </section>

          <section class="report-card">
            <div class="report-card-head">
              <div>
                <div class="section-title">Software</div>
                <small>Podés combinar software del catálogo con ítems manuales.</small>
              </div>
              <button class="secondary-button" type="button" (click)="addSoftwareItem()">Agregar manual</button>
            </div>
            @if (catalog.length) {
              <div class="catalog-chip-row">
                @for (item of catalog; track item.id || item.name) {
                  <button class="catalog-chip" type="button" (click)="appendCatalogItem(item)">{{ item.name }}</button>
                }
              </div>
            }
            <div class="native-table-wrap compact-table-wrap">
              <table class="native-table compact-native-table">
                <thead><tr><th>Software</th><th>Detalle</th><th></th></tr></thead>
                <tbody>
                  @for (item of report.softwareItems; track item.id || $index) {
                    <tr>
                      <td><input class="control" [(ngModel)]="item.softwareName" /></td>
                      <td><input class="control" [(ngModel)]="item.detail" placeholder="Versión, licencia o nota" /></td>
                      <td class="centered-cell"><button class="icon-button" type="button" (click)="removeSoftwareItem($index)"><i class="pi pi-trash"></i></button></td>
                    </tr>
                  } @empty {
                    <tr><td colspan="3" class="empty-cell">Sin software agregado.</td></tr>
                  }
                </tbody>
              </table>
            </div>
          </section>

          <section class="report-card report-total-card">
            <div>
              <div class="section-title">Resumen económico</div>
              <small>El monto final sí forma parte del reporte entregable.</small>
            </div>
            <label class="field report-total-field">
              <span>Monto final</span>
              <p-inputNumber [(ngModel)]="report.finalAmount" mode="currency" currency="ARS" locale="es-AR" [min]="0"></p-inputNumber>
            </label>
          </section>
        </div>
      }
    </p-dialog>
  `
})
export class DeliveryReportDialogComponent {
  visible = false;
  report: DeliveryReport | null = null;
  catalog: SoftwareCatalogItem[] = [];
  currentRepairId = '';
  isSaving = false;
  isGenerating = false;

  constructor(
    private readonly api: ApiService,
    private readonly messages: MessageService,
    private readonly changeDetector: ChangeDetectorRef
  ) {}

  open(repair: Repair): void {
    if (!repair.id) {
      return;
    }
    this.currentRepairId = repair.id;
    forkJoin({
      report: this.api.getDeliveryReport(repair.id),
      catalog: this.api.getSoftwareCatalog()
    }).subscribe({
      next: ({ report, catalog }) => {
        this.report = {
          ...report,
          hardwareItems: (report.hardwareItems || []).map((item) => ({ ...item, includePrice: item.includePrice !== false })),
          softwareItems: (report.softwareItems || []).map((item) => ({ ...item }))
        };
        this.catalog = catalog;
        this.visible = true;
        this.changeDetector.detectChanges();
      },
      error: () => this.messages.add({ severity: 'error', summary: 'Error', detail: 'No se pudo cargar el reporte de reparación.' })
    });
  }

  get issuedAtLocal(): string {
    if (!this.report?.issuedAt) {
      return '';
    }
    return this.report.issuedAt.slice(0, 10);
  }

  set issuedAtLocal(value: string) {
    if (!this.report) {
      return;
    }
    this.report.issuedAt = value ? `${value}T00:00:00` : '';
  }

  addHardwareItem(): void {
    this.report?.hardwareItems.push({ partName: '', quantity: 1, detail: '', unitPrice: 0, includePrice: true });
  }

  removeHardwareItem(index: number): void {
    this.report!.hardwareItems = this.report!.hardwareItems.filter((_, currentIndex) => currentIndex !== index);
  }

  addSoftwareItem(): void {
    this.report?.softwareItems.push({ softwareName: '', detail: '' });
  }

  appendCatalogItem(item: SoftwareCatalogItem): void {
    this.report?.softwareItems.push({ softwareName: item.name, detail: item.detail || '' });
  }

  removeSoftwareItem(index: number): void {
    this.report!.softwareItems = this.report!.softwareItems.filter((_, currentIndex) => currentIndex !== index);
  }

  save(): void {
    if (!this.report) {
      return;
    }
    this.isSaving = true;
    this.api.saveDeliveryReport(this.currentRepairId, this.report).subscribe({
      next: (saved) => {
        this.report = {
          ...saved,
          hardwareItems: (saved.hardwareItems || []).map((item) => ({ ...item, includePrice: item.includePrice !== false })),
          softwareItems: (saved.softwareItems || []).map((item) => ({ ...item }))
        };
        this.isSaving = false;
        this.messages.add({ severity: 'success', summary: 'Reporte guardado', detail: 'Los datos del reporte quedaron listos para regenerar el PDF.' });
      },
      error: () => {
        this.isSaving = false;
        this.messages.add({ severity: 'error', summary: 'Error', detail: 'No se pudo guardar el reporte.' });
      }
    });
  }

  previewPdf(): void {
    const previewWindow = window.open('', '_blank');
    if (!previewWindow) {
      this.messages.add({ severity: 'warn', summary: 'Ventana bloqueada', detail: 'Permití ventanas emergentes para previsualizar el PDF.' });
      return;
    }
    previewWindow.document.title = 'Generando reporte...';
    previewWindow.document.body.innerHTML = '<p style="font-family:Arial,sans-serif;padding:16px;">Generando PDF...</p>';
    this.saveAndGenerate('preview', previewWindow);
  }

  downloadPdf(): void {
    this.saveAndGenerate('download');
  }

  private saveAndGenerate(mode: 'preview' | 'download', previewWindow?: Window | null): void {
    if (!this.report) {
      return;
    }
    this.isGenerating = true;
    this.api.saveDeliveryReport(this.currentRepairId, this.report).subscribe({
      next: (saved) => {
        this.report = {
          ...saved,
          hardwareItems: (saved.hardwareItems || []).map((item) => ({ ...item, includePrice: item.includePrice !== false })),
          softwareItems: (saved.softwareItems || []).map((item) => ({ ...item }))
        };
        this.api.getDeliveryReportPdf(this.currentRepairId).subscribe({
          next: (blob) => {
            this.isGenerating = false;
            this.openBlob(blob, mode, previewWindow);
          },
          error: () => {
            this.isGenerating = false;
            previewWindow?.close();
            this.messages.add({ severity: 'error', summary: 'Error', detail: 'No se pudo generar el PDF.' });
          }
        });
      },
      error: () => {
        this.isGenerating = false;
        previewWindow?.close();
        this.messages.add({ severity: 'error', summary: 'Error', detail: 'No se pudo guardar el reporte antes de generar el PDF.' });
      }
    });
  }

  private openBlob(blob: Blob, mode: 'preview' | 'download', previewWindow?: Window | null): void {
    const url = URL.createObjectURL(blob);
    if (mode === 'preview') {
      if (previewWindow) {
        previewWindow.location.href = url;
        previewWindow.focus();
      } else {
        window.open(url, '_blank', 'noopener');
      }
      setTimeout(() => URL.revokeObjectURL(url), 60_000);
      return;
    }

    const link = document.createElement('a');
    link.href = url;
    link.download = `reporte-reparacion-${this.report?.orderNumber || this.currentRepairId}.pdf`;
    link.click();
    setTimeout(() => URL.revokeObjectURL(url), 10_000);
  }
}
