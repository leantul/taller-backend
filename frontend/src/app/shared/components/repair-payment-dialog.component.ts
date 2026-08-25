import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DialogModule } from 'primeng/dialog';
import { InputNumberModule } from 'primeng/inputnumber';
import { Repair, RepairStatusUpdate } from '../models/repair.model';

@Component({
  selector: 'app-repair-payment-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule, DialogModule, InputNumberModule],
  template: `
    <p-dialog header="Registrar cobro" [(visible)]="visible" [modal]="true" [closable]="!saving"
      [style]="{width:'32rem', maxWidth:'95vw'}" (onHide)="cancelled.emit()">
      @if (repair) {
        <div class="detail-grid">
          <div class="detail-item"><label>Monto final</label><strong>{{ repair.price | currency:'ARS':'symbol':'1.2-2':'es-AR' }}</strong></div>
          <div class="detail-item"><label>Ya cobrado</label><strong>{{ paid | currency:'ARS':'symbol':'1.2-2':'es-AR' }}</strong></div>
          <div class="detail-item detail-wide"><label>Saldo pendiente</label><strong>{{ remaining | currency:'ARS':'symbol':'1.2-2':'es-AR' }}</strong></div>
          <div class="detail-item detail-wide">
            <label>Tipo de cobro</label>
            <select class="control" [(ngModel)]="paymentType" (ngModelChange)="onTypeChange()">
              @if (allowNoPayment) { <option value="NONE">No registrar pago ahora</option> }
              <option value="FULL">Cobro total</option><option value="PARTIAL">Cobro parcial</option>
            </select>
          </div>
          @if (paymentType === 'PARTIAL') {
            <div class="detail-item detail-wide"><label>Monto recibido</label>
              <p-inputnumber [(ngModel)]="amount" mode="currency" currency="ARS" locale="es-AR" [min]="0.01" [max]="remaining"></p-inputnumber>
              @if (error) { <small class="field-error">{{ error }}</small> }
            </div>
          }
        </div>
        <div class="dialog-actions">
          <button class="secondary-button" type="button" [disabled]="saving" (click)="close()">Cancelar</button>
          <button class="primary-button" type="button" [disabled]="saving || (remaining <= 0 && paymentType !== 'NONE')" (click)="confirm()">{{ paymentType === 'NONE' ? 'Confirmar retiro' : 'Registrar cobro' }}</button>
        </div>
      }
    </p-dialog>`
})
export class RepairPaymentDialogComponent {
  @Input() visible = false;
  @Input() repair: Repair | null = null;
  @Input() saving = false;
  @Input() targetStatus: Repair['status'] = 'COBRADO_ESPERANDO_RETIRO';
  @Input() allowNoPayment = false;
  @Output() visibleChange = new EventEmitter<boolean>();
  @Output() submitted = new EventEmitter<RepairStatusUpdate>();
  @Output() cancelled = new EventEmitter<void>();
  paymentType: 'NONE' | 'FULL' | 'PARTIAL' = 'FULL';
  amount: number | null = null;
  error = '';

  get paid(): number { return Number(this.repair?.totalPaid ?? (this.repair?.payments || []).reduce((sum, p) => sum + Number(p.amount || 0), 0)); }
  get remaining(): number { return Math.max(0, Number(this.repair?.price || 0) - this.paid); }
  onTypeChange(): void { this.amount = this.paymentType === 'FULL' ? this.remaining : null; this.error = ''; }
  confirm(): void {
    const amount = Number(this.amount || 0);
    if (this.paymentType === 'PARTIAL' && (amount <= 0 || amount > this.remaining)) {
      this.error = 'Ingresá un monto mayor que cero y no superior al saldo.'; return;
    }
    this.submitted.emit({ status: this.targetStatus, paymentType: this.paymentType === 'NONE' ? undefined : this.paymentType,
      paymentAmount: this.paymentType === 'PARTIAL' ? amount : undefined });
  }
  close(): void { this.visible = false; this.visibleChange.emit(false); this.cancelled.emit(); }
}
