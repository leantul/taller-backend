import { CdkDragDrop, DragDropModule, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';
import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CardModule } from 'primeng/card';
import { DialogModule } from 'primeng/dialog';
import { TagModule } from 'primeng/tag';
import { MessageService } from 'primeng/api';
import { ApiService } from '../../core/services/api.service';
import { Repair } from '../../shared/models/repair.model';
import { Client } from '../../shared/models/client.model';
import { Device } from '../../shared/models/device.model';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-status-page',
  standalone: true,
  imports: [CommonModule, DragDropModule, CardModule, TagModule, DialogModule],
  templateUrl: './status-page.component.html'
})
export class StatusPageComponent implements OnInit {
  columns: { title: string; status: Repair['status']; items: Repair[] }[] = [
    { title: 'Por recibir', status: 'POR_RECIBIR', items: [] },
    { title: 'A reparar', status: 'RECIBIDA', items: [] },
    { title: 'En proceso', status: 'HACIENDO', items: [] },
    { title: 'Lista para entregar', status: 'ESPERANDO_RETIRO', items: [] },
    { title: 'Entregada', status: 'RETIRADA', items: [] }
  ];

  selectedRepair: Repair | null = null;
  showDetailModal = false;
  clientsById = new Map<string, Client>();
  devicesById = new Map<string, Device>();

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
    this.selectedRepair = item;
    this.showDetailModal = true;
  }

  clientName(item: Repair): string {
    const client = this.clientsById.get(item.idClient);
    return client ? `${client.name} ${client.lastName}`.trim() : item.idClient;
  }

  deviceLabel(item: Repair): string {
    const device = this.devicesById.get(item.idDevice);
    return device ? `${device.deviceType} ${device.brand} ${device.model}`.trim() : item.idDevice;
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
