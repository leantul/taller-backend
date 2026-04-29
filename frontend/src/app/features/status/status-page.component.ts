import { CdkDragDrop, DragDropModule, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';
import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { DialogModule } from 'primeng/dialog';
import { InputNumberModule } from 'primeng/inputnumber';
import { TagModule } from 'primeng/tag';
import { MessageService } from 'primeng/api';
import { ApiService } from '../../core/services/api.service';
import { Repair } from '../../shared/models/repair.model';

@Component({
  selector: 'app-status-page',
  standalone: true,
  imports: [CommonModule, DragDropModule, CardModule, TagModule, ButtonModule, DialogModule, FormsModule, InputNumberModule],
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

  showDeliverModal = false;
  selectedRepair: Repair | null = null;
  paidAmount = 0;

  constructor(private readonly api: ApiService, private readonly messageService: MessageService) {}

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

  openDeliveredModal(repair: Repair): void {
    this.selectedRepair = repair;
    this.paidAmount = repair.price || 0;
    this.showDeliverModal = true;
  }

  confirmDelivered(): void {
    if (!this.selectedRepair) return;
    const payload: Repair = { ...this.selectedRepair, status: 'RETIRADA', price: this.paidAmount };
    this.api.updateRepair(payload).subscribe({
      next: () => {
        this.messageService.add({ severity: 'success', summary: 'Entrega registrada', detail: `Cobrado ${this.paidAmount.toLocaleString('es-AR', { style: 'currency', currency: 'ARS' })}` });
        this.showDeliverModal = false;
        this.reload();
      },
      error: () => this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudo registrar la entrega.' })
    });
  }

  private reload(): void {
    this.api.getRepairs().subscribe((repairs) => {
      this.columns.forEach((c) => (c.items = repairs.filter((r) => r.status === c.status)));
    });
  }
}
