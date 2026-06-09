import { CdkDragDrop, DragDropModule, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';
import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CardModule } from 'primeng/card';
import { DialogModule } from 'primeng/dialog';
import { MessageService } from 'primeng/api';
import { ApiService } from '../../core/services/api.service';
import { Repair } from '../../shared/models/repair.model';
import { Client } from '../../shared/models/client.model';
import { Device } from '../../shared/models/device.model';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-status-page',
  standalone: true,
  imports: [CommonModule, FormsModule, DragDropModule, CardModule, DialogModule],
  templateUrl: './status-page.component.html'
})
export class StatusPageComponent implements OnInit {
  columns: { title: string; status: Repair['status']; items: Repair[] }[] = [
    { title: 'Por recibir', status: 'POR_RECIBIR', items: [] },
    { title: 'Recibida', status: 'RECIBIDA', items: [] },
    { title: 'Presupuestada', status: 'PRESUPUESTADA_ESPERANDO_RESPUESTA', items: [] },
    { title: 'En proceso', status: 'HACIENDO', items: [] },
    { title: 'Esperando retiro', status: 'ESPERANDO_RETIRO', items: [] }
  ];

  selectedRepair: Repair | null = null;
  showDetailModal = false;
  clientsById = new Map<string, Client>();
  devicesById = new Map<string, Device>();
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

    this.api.updateRepair(updated).subscribe({
      next: () => this.messageService.add({ severity: 'success', summary: 'Estado actualizado', detail: `Orden ${updated.orderNumber}` }),
      error: () => { this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudo actualizar el estado.' }); this.reload(); }
    });
  }

  openDetail(item: Repair): void {
    this.selectedRepair = { ...item };
    this.showDetailModal = true;
  }

  saveDetailStatus(): void {
    if (!this.selectedRepair) return;
    this.isSavingStatus = true;
    this.api.updateRepair(this.selectedRepair).subscribe({
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
    const client = this.clientsById.get(item.idClient);
    return client ? `${client.name} ${client.lastName}`.trim() : item.idClient;
  }

  deviceLabel(item: Repair): string {
    const device = this.devicesById.get(item.idDevice);
    return device ? `${device.deviceTypeName || '-'} ${device.brand} ${device.model}`.trim() : item.idDevice;
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
    forkJoin({ repairs: this.api.getRepairs(), clients: this.api.getClients(), devices: this.api.getDevices() }).subscribe(({ repairs, clients, devices }) => {
      this.clientsById = new Map(clients.map((client) => [client.id!, client]));
      this.devicesById = new Map(devices.map((device) => [device.id!, device]));
      this.columns = this.columns.map((column) => ({
        ...column,
        items: repairs.filter((repair) => repair.status === column.status)
      }));
      this.changeDetector.detectChanges();
    });
  }
}
