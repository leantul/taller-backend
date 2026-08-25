import { CdkDragDrop, DragDropModule, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';
import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CardModule } from 'primeng/card';
import { DialogModule } from 'primeng/dialog';
import { MessageService } from 'primeng/api';
import { ApiService } from '../../core/services/api.service';
import { Repair } from '../../shared/models/repair.model';
import { StatusBoardRepair } from '../../shared/models/repair.model';
import { DeliveryReportDialogComponent } from '../../shared/components/delivery-report-dialog.component';
import { repairStatusClass } from '../../shared/utils/repair-status.util';
import { RepairPaymentDialogComponent } from '../../shared/components/repair-payment-dialog.component';
import { RepairStatusUpdate } from '../../shared/models/repair.model';

@Component({
  selector: 'app-status-page',
  standalone: true,
  imports: [CommonModule, FormsModule, DragDropModule, CardModule, DialogModule, DeliveryReportDialogComponent, RepairPaymentDialogComponent],
  templateUrl: './status-page.component.html'
})
export class StatusPageComponent implements OnInit {
  @ViewChild(DeliveryReportDialogComponent) private deliveryReportDialog?: DeliveryReportDialogComponent;
  columns: { title: string; status: Repair['status']; items: Repair[]; page: number; totalPages: number; loading: boolean }[] = [
    { title: 'Por recibir', status: 'POR_RECIBIR', items: [], page: 0, totalPages: 1, loading: false },
    { title: 'Recibida', status: 'RECIBIDA', items: [], page: 0, totalPages: 1, loading: false },
    { title: 'Presupuestada', status: 'PRESUPUESTADA_ESPERANDO_RESPUESTA', items: [], page: 0, totalPages: 1, loading: false },
    { title: 'En proceso', status: 'HACIENDO', items: [], page: 0, totalPages: 1, loading: false },
    { title: 'Esperando retiro', status: 'ESPERANDO_RETIRO', items: [], page: 0, totalPages: 1, loading: false },
    { title: 'Cobrado esperando retiro', status: 'COBRADO_ESPERANDO_RETIRO', items: [], page: 0, totalPages: 1, loading: false }
  ];

  selectedRepair: Repair | null = null;
  showDetailModal = false;
  isSavingStatus = false;
  showPaymentDialog = false;
  paymentRepair: Repair | null = null;
  private pendingPaidRepair: Repair | null = null;
  statusOptions = [
    { label: 'Por recibir', value: 'POR_RECIBIR' },
    { label: 'Recibida', value: 'RECIBIDA' },
    { label: 'Presupuestada', value: 'PRESUPUESTADA_ESPERANDO_RESPUESTA' },
    { label: 'En proceso', value: 'HACIENDO' },
    { label: 'Esperando retiro', value: 'ESPERANDO_RETIRO' },
    { label: 'Cobrado esperando retiro', value: 'COBRADO_ESPERANDO_RETIRO' },
    { label: 'Retirada', value: 'RETIRADA' }
  ];

  constructor(private readonly api: ApiService, private readonly messageService: MessageService, private readonly changeDetector: ChangeDetectorRef) {}

  ngOnInit(): void { this.reload(); }

  drop(event: CdkDragDrop<Repair[]>, status: Repair['status']): void {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
      return;
    }
    const moved = event.previousContainer.data[event.previousIndex];
    if (status === 'COBRADO_ESPERANDO_RETIRO') {
      this.preparePayment(moved);
      return;
    }
    const updated = { ...moved, status };
    transferArrayItem(event.previousContainer.data, event.container.data, event.previousIndex, event.currentIndex);
    event.container.data[event.currentIndex] = updated;

    this.updateStatus(updated);
  }

  private preparePayment(repair: Repair): void {
    if (!repair.id) return;
    this.api.getRepairById(repair.id).subscribe({
      next: detail => { this.pendingPaidRepair = repair; this.paymentRepair = detail; this.showPaymentDialog = true; },
      error: () => this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudo cargar el saldo de la reparación.' })
    });
  }

  savePaymentStatus(payload: RepairStatusUpdate): void {
    if (!this.pendingPaidRepair?.id) return;
    this.isSavingStatus = true;
    this.api.updateRepairStatus(this.pendingPaidRepair.id, payload).subscribe({
      next: () => { this.isSavingStatus = false; this.showPaymentDialog = false; this.messageService.add({ severity: 'success', summary: 'Cobro registrado', detail: `Orden ${this.pendingPaidRepair?.orderNumber}` }); this.pendingPaidRepair = null; this.reload(); },
      error: (error) => { this.isSavingStatus = false; this.messageService.add({ severity: 'error', summary: 'Error', detail: error?.error?.error || 'No se pudo registrar el cobro.' }); }
    });
  }

  cancelPayment(): void { this.showPaymentDialog = false; this.pendingPaidRepair = null; this.paymentRepair = null; }

  private updateStatus(repair: Repair): void {
    if (!repair.id) return;
    this.api.updateRepairStatus(repair.id, repair.status).subscribe({
      next: () => this.messageService.add({ severity: 'success', summary: 'Estado actualizado', detail: `Orden ${repair.orderNumber}` }),
      error: () => { this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudo actualizar el estado.' }); this.reload(); }
    });
  }

  openDetail(item: Repair): void {
    this.selectedRepair = { ...item };
    this.showDetailModal = true;
  }

  openDeliveryReport(event: Event, repair: Repair): void {
    event.stopPropagation();
    event.preventDefault();
    this.deliveryReportDialog?.open(repair);
  }

  canOpenDeliveryReport(repair: Repair): boolean {
    return repair.status === 'ESPERANDO_RETIRO' || repair.status === 'COBRADO_ESPERANDO_RETIRO' || repair.status === 'RETIRADA';
  }

  saveDetailStatus(): void {
    if (!this.selectedRepair) return;
    if (this.selectedRepair.status === 'COBRADO_ESPERANDO_RETIRO') { this.showDetailModal = false; this.preparePayment(this.selectedRepair); return; }
    this.isSavingStatus = true;
    if (!this.selectedRepair.id) {
      this.isSavingStatus = false;
      return;
    }
    this.api.updateRepairStatus(this.selectedRepair.id, this.selectedRepair.status).subscribe({
      next: () => {
        this.isSavingStatus = false;
        this.showDetailModal = false;
        this.messageService.add({ severity: 'success', summary: 'Estado actualizado', detail: `Orden ${this.selectedRepair?.orderNumber}` });
        this.reload();
      },
      error: () => {
        this.isSavingStatus = false;
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudo actualizar el estado.' });
      }
    });
  }

  clientName(item: Repair): string {
    return (item as StatusBoardRepair).clientName || item.idClient;
  }

  deviceLabel(item: Repair): string {
    return (item as StatusBoardRepair).deviceLabel || item.idDevice;
  }

  statusLabel(status: Repair['status']): string {
    switch (status) {
      case 'POR_RECIBIR': return 'Por recibir';
      case 'RECIBIDA': return 'Recibida';
      case 'PRESUPUESTADA_ESPERANDO_RESPUESTA': return 'Presupuestada';
      case 'HACIENDO': return 'En proceso';
      case 'ESPERANDO_RETIRO': return 'Esperando retiro';
      case 'COBRADO_ESPERANDO_RETIRO': return 'Cobrado esperando retiro';
      case 'RETIRADA': return 'Entregada';
      default: return status;
    }
  }

  statusClass(status: Repair['status']): string {
    return repairStatusClass(status) || 'is-muted';
  }

  private reload(): void {
    this.columns.forEach(column => { column.items = []; column.page = 0; column.totalPages = 1; this.loadColumn(column); });
  }

  loadMore(column: typeof this.columns[number]): void { if (column.page + 1 < column.totalPages) { column.page++; this.loadColumn(column, true); } }
  private loadColumn(column: typeof this.columns[number], append = false): void {
    column.loading = true;
    this.api.getStatusBoardPage(column.status, column.page, 20).subscribe({
      next: page => { column.items = append ? [...column.items, ...page.content] : page.content; column.totalPages = page.totalPages; column.loading = false; this.changeDetector.detectChanges(); },
      error: () => { column.loading = false; this.messageService.add({ severity: 'error', summary: 'Error', detail: `No se pudo cargar ${column.title}.` }); }
    });
  }

}
