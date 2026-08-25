import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component } from '@angular/core';
import { DialogModule } from 'primeng/dialog';
import { FormsModule } from '@angular/forms';
import { InputNumberModule } from 'primeng/inputnumber';
import { MessageService } from 'primeng/api';
import { ApiService } from '../../core/services/api.service';
import { Repair } from '../models/repair.model';
import { repairStatusClass, repairStatusLabel } from '../utils/repair-status.util';

interface StatusTimelineItem {
  status: Repair['status'];
  changedAt?: string;
}

@Component({
  selector: 'app-repair-detail-dialog',
  standalone: true,
  imports: [CommonModule, DialogModule, FormsModule, InputNumberModule],
  template: `
    <p-dialog
      header="Detalle de reparación"
      [(visible)]="visible"
      [modal]="true"
      [style]="{width:'70rem', maxWidth:'95vw'}"
      [contentStyle]="{overflow:'hidden', padding:'0.35rem'}">
      @if (repair) {
        <div class="detail-dialog-body">
          <div class="detail-grid repair-detail-grid">
            <div class="detail-item"><label>Orden</label><strong>#{{ repair.orderNumber || '-' }}</strong></div>
            <div class="detail-item"><label>Estado</label><span class="status-pill" [ngClass]="statusClass(repair.status)">{{ statusLabel(repair.status) }}</span></div>
            <div class="detail-item"><label>Cliente</label><strong>{{ clientLabel }}</strong></div>
            <div class="detail-item"><label>Dispositivo</label><strong>{{ deviceLabel }}</strong></div>
            <div class="detail-item detail-wide">
              <label>Horarios por estado</label>
              <div class="status-timeline-list">
                @for (item of statusTimelineItems(); track item.status + '-' + (item.changedAt || $index)) {
                  <div class="status-timeline-row">
                    <span class="status-pill" [ngClass]="statusClass(item.status)">{{ statusLabel(item.status) }}</span>
                    <strong>{{ formatDateTime(item.changedAt) }}</strong>
                  </div>
                }
              </div>
            </div>
            <div class="detail-item"><label>Presupuesto</label><strong>{{ repair.quotedAmount || 0 | currency:'ARS':'symbol':'1.2-2':'es-AR' }}</strong></div>
            <div class="detail-item"><label>Mano de obra</label><strong>{{ repair.laborAmount || 0 | currency:'ARS':'symbol':'1.2-2':'es-AR' }}</strong></div>
            <div class="detail-item detail-wide"><label>Monto final</label><strong>{{ repair.price || 0 | currency:'ARS':'symbol':'1.2-2':'es-AR' }}</strong></div>
            <div class="detail-item"><label>Total cobrado</label><strong>{{ repair.totalPaid || 0 | currency:'ARS':'symbol':'1.2-2':'es-AR' }}</strong></div>
            <div class="detail-item"><label>Saldo pendiente</label><strong>{{ repair.outstandingBalance || 0 | currency:'ARS':'symbol':'1.2-2':'es-AR' }}</strong></div>
            <div class="detail-item detail-wide">
              <label>Historial de pagos</label>
              @for (payment of repair.payments || []; track payment.id || $index) {
                <div class="inline-row" style="gap:.5rem;margin-bottom:.5rem">
                  <p-inputnumber [(ngModel)]="payment.amount" mode="currency" currency="ARS" locale="es-AR" [min]="0.01"></p-inputnumber>
                  <input class="control" type="datetime-local" [ngModel]="toLocalDate(payment.paymentDate)" (ngModelChange)="payment.paymentDate = $event" />
                  <input class="control" [(ngModel)]="payment.notes" placeholder="Nota" />
                  <button class="icon-button" type="button" (click)="removePayment($index)"><i class="pi pi-trash"></i></button>
                </div>
              } @empty { <strong>Sin pagos registrados</strong> }
              <div class="dialog-actions">
                <button class="secondary-button" type="button" (click)="addPayment()">Agregar pago</button>
                <button class="primary-button" type="button" [disabled]="savingPayments" (click)="savePayments()">Guardar pagos</button>
              </div>
            </div>
            <div class="detail-item detail-wide detail-text-block"><label>Falla reportada</label><div class="detail-scrollable">{{ repair.description || 'Sin descripción' }}</div></div>
            <div class="detail-item detail-wide detail-text-block"><label>Detalle del presupuesto</label><div class="detail-scrollable">{{ repair.quoteNotes || 'Sin detalle cargado' }}</div></div>
            <div class="detail-item detail-wide detail-text-block"><label>Observaciones de la reparación</label><div class="detail-scrollable">{{ repair.repairNotes || 'Sin observaciones cargadas' }}</div></div>
            <div class="detail-item detail-wide">
              <label>Observaciones del dispositivo</label>
              @if (repair.observations?.length) {
                <div class="observation-list">
                  @for (observation of repair.observations || []; track observation.id || $index) {
                    <span class="observation-chip" [class.is-resolved]="observation.resolvedAt">{{ observation.note }}</span>
                  }
                </div>
              } @else { <strong>Sin observaciones cargadas</strong> }
            </div>
            <div class="detail-item detail-wide">
              <label>Repuestos cambiados</label>
              @if (repair.parts?.length) {
                <div class="native-table-wrap compact-table-wrap">
                  <table class="native-table compact-native-table">
                    <thead><tr><th>Repuesto</th><th>Cant.</th><th>Proveedor</th><th>Costo</th><th>Venta</th></tr></thead>
                    <tbody>
                      @for (part of repair.parts || []; track part.id || $index) {
                        <tr><td>{{ part.name }}</td><td>{{ part.quantity }}</td><td>{{ part.provider || '-' }}</td><td>{{ part.cost | currency:'ARS':'symbol':'1.2-2':'es-AR' }}</td><td>{{ part.salePrice | currency:'ARS':'symbol':'1.2-2':'es-AR' }}</td></tr>
                      }
                    </tbody>
                  </table>
                </div>
              } @else { <strong>Sin repuestos cargados</strong> }
            </div>
          </div>
        </div>
      }
    </p-dialog>
  `
})
export class RepairDetailDialogComponent {
  visible = false;
  repair: Repair | null = null;
  clientLabel = '-';
  deviceLabel = '-';
  savingPayments = false;

  constructor(private readonly api: ApiService, private readonly messages: MessageService, private readonly changeDetector: ChangeDetectorRef) {}

  open(repairId: string, clientLabel: string, deviceLabel: string): void {
    this.api.getRepairById(repairId).subscribe({
      next: (repair) => {
        this.repair = { ...repair, payments: (repair.payments || []).map(payment => ({ ...payment })) };
        this.clientLabel = clientLabel || repair.idClient || '-';
        this.deviceLabel = deviceLabel || repair.idDevice || '-';
        this.visible = true;
        this.changeDetector.detectChanges();
      },
      error: () => this.messages.add({ severity: 'error', summary: 'Error', detail: 'No se pudo cargar el detalle de la reparación.' })
    });
  }

  addPayment(): void { this.repair?.payments?.push({ amount: 0, currency: 'ARS', paymentDate: this.toLocalDate(new Date().toISOString()), notes: '' }); }
  removePayment(index: number): void { if (this.repair) this.repair.payments = (this.repair.payments || []).filter((_, i) => i !== index); }
  savePayments(): void {
    if (!this.repair?.id) return;
    this.savingPayments = true;
    this.api.replaceRepairPayments(this.repair.id, this.repair.payments || []).subscribe({
      next: repair => { this.savingPayments = false; this.repair = { ...repair, payments: (repair.payments || []).map(payment => ({ ...payment })) }; this.messages.add({ severity: 'success', summary: 'Pagos actualizados', detail: 'El saldo fue recalculado.' }); },
      error: error => { this.savingPayments = false; this.messages.add({ severity: 'error', summary: 'Error', detail: error?.error?.error || 'No se pudieron actualizar los pagos.' }); }
    });
  }

  toLocalDate(value?: string): string {
    if (!value) return '';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value.slice(0, 16);
    const offset = date.getTimezoneOffset() * 60000;
    return new Date(date.getTime() - offset).toISOString().slice(0, 16);
  }

  statusLabel(status: Repair['status']): string {
    return repairStatusLabel(status);
  }

  statusClass(status: Repair['status']): string {
    return repairStatusClass(status);
  }

  statusTimelineItems(): StatusTimelineItem[] {
    if (!this.repair) return [];

    const items = (this.repair.statusHistory || [])
      .filter((history) => !!history.changedAt)
      .map((history) => ({ status: history.status, changedAt: history.changedAt }));

    if (!items.some((item) => item.status === 'RECIBIDA') && this.repair.receiveDateTime) {
      items.unshift({ status: 'RECIBIDA', changedAt: this.repair.receiveDateTime });
    }

    if (!items.some((item) => item.status === 'RETIRADA')) {
      items.push({ status: 'RETIRADA', changedAt: this.repair.returnDateTime });
    }

    return items;
  }

  formatDateTime(value?: string): string {
    if (!value) return '-';
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? '-' : new Intl.DateTimeFormat('es-AR', { dateStyle: 'short', timeStyle: 'short' }).format(date);
  }
}
