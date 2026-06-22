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

@Component({
  selector: 'app-status-page',
  standalone: true,
  imports: [CommonModule, FormsModule, DragDropModule, CardModule, DialogModule, DeliveryReportDialogComponent],
  templateUrl: './status-page.component.html'
})
export class StatusPageComponent implements OnInit {
  @ViewChild(DeliveryReportDialogComponent) private deliveryReportDialog?: DeliveryReportDialogComponent;
  columns: { title: string; status: Repair['status']; items: Repair[] }[] = [
    { title: 'Por recibir', status: 'POR_RECIBIR', items: [] },
    { title: 'Recibida', status: 'RECIBIDA', items: [] },
    { title: 'Presupuestada', status: 'PRESUPUESTADA_ESPERANDO_RESPUESTA', items: [] },
    { title: 'En proceso', status: 'HACIENDO', items: [] },
    { title: 'Esperando retiro', status: 'ESPERANDO_RETIRO', items: [] }
  ];

  selectedRepair: Repair | null = null;
  showDetailModal = false;
  isSavingStatus = false;
  statusOptions = [
    { label: 'Por recibir', value: 'POR_RECIBIR' },
    { label: 'Recibida', value: 'RECIBIDA' },
    { label: 'Presupuestada', value: 'PRESUPUESTADA_ESPERANDO_RESPUESTA' },
    { label: 'En proceso', value: 'HACIENDO' },
    { label: 'Esperando retiro', value: 'ESPERANDO_RETIRO' }
  ];

  constructor(private readonly api: ApiService, private readonly messageService: MessageService, private readonly changeDetector: ChangeDetectorRef) {}

  ngOnInit(): void { this.reload(); }

  drop(event: CdkDragDrop<Repair[]>, status: Repair['status']): void {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
      return;
    }
    const moved = event.previousContainer.data[event.previousIndex];
    const updated = { ...moved, status };
    transferArrayItem(event.previousContainer.data, event.container.data, event.previousIndex, event.currentIndex);
    event.container.data[event.currentIndex] = updated;

    this.updateStatus(updated);
  }

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
    return repair.status === 'ESPERANDO_RETIRO' || repair.status === 'RETIRADA';
  }

  saveDetailStatus(): void {
    if (!this.selectedRepair) return;
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
      case 'RETIRADA': return 'Entregada';
      default: return status;
    }
  }

  statusClass(status: Repair['status']): string {
    switch (status) {
      case 'POR_RECIBIR': return 'is-muted';
      case 'RECIBIDA': return 'is-info';
      case 'PRESUPUESTADA_ESPERANDO_RESPUESTA': return 'is-warning';
      case 'HACIENDO': return 'is-active';
      case 'ESPERANDO_RETIRO': return 'is-success';
      case 'RETIRADA': return 'is-closed';
      default: return 'is-muted';
    }
  }

  private reload(): void {
    this.api.getStatusBoardRepairs().subscribe((repairs) => {
      this.columns = this.columns.map((column) => ({
        ...column,
        items: repairs.filter((repair) => repair.status === column.status)
      }));
      this.changeDetector.detectChanges();
    });
  }

}
