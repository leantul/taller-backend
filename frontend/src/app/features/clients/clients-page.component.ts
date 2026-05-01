import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { DialogModule } from 'primeng/dialog';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ApiService } from '../../core/services/api.service';
import { Client } from '../../shared/models/client.model';
import { Repair } from '../../shared/models/repair.model';

@Component({
  selector: 'app-clients-page',
  standalone: true,
  imports: [CommonModule, FormsModule, CardModule, InputTextModule, ButtonModule, TableModule, DialogModule, ConfirmDialogModule],
  providers: [ConfirmationService],
  template: `
    <p-confirmdialog></p-confirmdialog>
    <div class="page-grid">
      <p-card header="Nuevo cliente" subheader="Alta rápida">
        <form class="p-fluid" (ngSubmit)="save()">
          <div class="field"><label>Nombre</label><input pInputText [(ngModel)]="draft.name" name="name" required /></div>
          <div class="field"><label>Apellido</label><input pInputText [(ngModel)]="draft.lastName" name="lastName" required /></div>
          <div class="field"><label>DNI</label><input pInputText [(ngModel)]="draft.dni" name="dni" pattern="^[0-9]+$" required /></div>
          <div class="field"><label>Email</label><input pInputText [(ngModel)]="draft.email" name="email" type="email" required /></div>
          <div class="field"><label>Celular</label><input pInputText [(ngModel)]="draft.phone" name="phone" required /></div>
          <button pButton type="submit" label="Guardar cliente" icon="pi pi-check"></button>
        </form>
      </p-card>

      <p-card header="Clientes">
        <div class="table-toolbar">
          <span class="p-input-icon-left"><i class="pi pi-search"></i><input pInputText [(ngModel)]="searchTerm" (ngModelChange)="onSearch()" placeholder="Buscar por nombre, DNI o email" /></span>
        </div>
        <p-table [value]="clients" size="small" [paginator]="true" [rows]="10" sortMode="multiple">
          <ng-template pTemplate="header"><tr><th pSortableColumn="name">Nombre <p-sortIcon field="name"></p-sortIcon></th><th pSortableColumn="dni">DNI <p-sortIcon field="dni"></p-sortIcon></th><th pSortableColumn="email">Email <p-sortIcon field="email"></p-sortIcon></th><th pSortableColumn="phone">Teléfono <p-sortIcon field="phone"></p-sortIcon></th><th>Acciones</th></tr></ng-template>
          <ng-template pTemplate="body" let-c>
            <tr>
              <td>{{ c.name }} {{ c.lastName }}</td><td>{{ c.dni }}</td><td>{{ c.email }}</td><td>{{ c.phone }} <a [href]="whatsAppLink(c.phone)" target="_blank" rel="noopener" class="wa-link"><i class="pi pi-whatsapp"></i></a></td>
              <td>
                <button pButton type="button" class="p-button-text p-button-sm" icon="pi pi-pencil" (click)="openEdit(c)"></button>
                <button pButton type="button" class="p-button-text p-button-sm" icon="pi pi-history" (click)="openRepairs(c)"></button>
                <button pButton type="button" class="p-button-text p-button-sm" icon="pi pi-trash" (click)="confirmDelete(c)"></button>
              </td>
            </tr>
          </ng-template>
        </p-table>
      </p-card>
    </div>

    <p-dialog header="Editar cliente" [(visible)]="editVisible" [modal]="true" [style]="{width:'32rem'}">
      <div class="field"><label>Nombre</label><input pInputText [(ngModel)]="editing.name" /></div>
      <div class="field"><label>Apellido</label><input pInputText [(ngModel)]="editing.lastName" /></div>
      <div class="field"><label>DNI</label><input pInputText [(ngModel)]="editing.dni" /></div>
      <div class="field"><label>Email</label><input pInputText [(ngModel)]="editing.email" /></div>
      <div class="field"><label>Celular</label><input pInputText [(ngModel)]="editing.phone" /></div>
      <button pButton type="button" label="Guardar cambios" icon="pi pi-check" (click)="updateClient()"></button>
    </p-dialog>


    <p-dialog header="Reparaciones del cliente" [(visible)]="repairsVisible" [modal]="true" [style]="{width:'56rem'}">
      <p-table [value]="clientRepairs" size="small" sortMode="multiple">
        <ng-template pTemplate="header"><tr><th pSortableColumn="orderNumber">Orden <p-sortIcon field="orderNumber"></p-sortIcon></th><th pSortableColumn="status">Estado <p-sortIcon field="status"></p-sortIcon></th><th pSortableColumn="receiveDateTime">Ingreso <p-sortIcon field="receiveDateTime"></p-sortIcon></th><th pSortableColumn="returnDateTime">Entrega <p-sortIcon field="returnDateTime"></p-sortIcon></th><th>Detalle</th></tr></ng-template>
        <ng-template pTemplate="body" let-r><tr><td>#{{ r.orderNumber }}</td><td>{{ r.status }}</td><td>{{ r.receiveDateTime ? (r.receiveDateTime | date:'dd/MM/yyyy HH:mm') : '-' }}</td><td>{{ r.returnDateTime ? (r.returnDateTime | date:'dd/MM/yyyy HH:mm') : '-' }}</td><td><button pButton type="button" class="p-button-text p-button-sm" icon="pi pi-eye" (click)="goToRepair(r)"></button></td></tr></ng-template>
      </p-table>
      @if (selectedRepair) {
        <div class="field"><label>Descripción</label><div>{{ selectedRepair.description || 'Sin descripción' }}</div></div>
        <div class="field"><label>Presupuesto</label><div>{{ (selectedRepair.quotedAmount || 0) | currency:'ARS':'symbol':'1.2-2':'es-AR' }}</div></div>
      }
    </p-dialog>
  `
})
export class ClientsPageComponent implements OnInit {
  clients: Client[] = [];
  draft: Client = { name: '', lastName: '', dni: '', email: '', phone: '' };
  editing: Client = { name: '', lastName: '', dni: '', email: '', phone: '' };
  editVisible = false;
  searchTerm = '';
  repairsVisible = false;
  clientRepairs: Repair[] = [];
  selectedRepair: Repair | null = null;

  constructor(private readonly api: ApiService, private readonly messageService: MessageService, private readonly confirmationService: ConfirmationService, private readonly router: Router) {}

  ngOnInit(): void { this.reload(); }

  save(): void {
    this.api.createClient(this.draft).subscribe({
      next: () => { this.messageService.add({ severity: 'success', summary: 'Cliente guardado', detail: 'El cliente se creó correctamente.' }); this.draft = { name: '', lastName: '', dni: '', email: '', phone: '' }; this.reload(); },
      error: (error) => { const detail = error?.status === 403 ? 'No autorizado (403). Verificá permisos/token de sesión.' : 'No se pudo guardar el cliente.'; this.messageService.add({ severity: 'error', summary: 'Error', detail }); }
    });
  }

  openEdit(client: Client): void { this.editing = { ...client }; this.editVisible = true; }

  updateClient(): void {
    this.api.createClient(this.editing).subscribe({
      next: () => { this.messageService.add({ severity: 'success', summary: 'Cliente actualizado', detail: 'Cambios guardados.' }); this.editVisible = false; this.reload(); },
      error: (error) => { const detail = error?.status === 403 ? 'No autorizado (403). Verificá permisos/token de sesión.' : 'No se pudo actualizar el cliente.'; this.messageService.add({ severity: 'error', summary: 'Error', detail }); }
    });
  }

  openRepairs(client: Client): void {
    this.api.getRepairs().subscribe((repairs) => {
      this.clientRepairs = repairs
        .filter((repair) => repair.idClient === client.id)
        .sort((a, b) => new Date(b.receiveDateTime || 0).getTime() - new Date(a.receiveDateTime || 0).getTime());
      this.selectedRepair = this.clientRepairs[0] || null;
      this.repairsVisible = true;
    });
  }


  goToRepair(repair: Repair): void {
    this.selectedRepair = repair;
    this.repairsVisible = false;
    const term = repair.orderNumber || repair.id || '';
    this.router.navigate(['/reparaciones'], { queryParams: term ? { q: term } : undefined });
  }
  confirmDelete(client: Client): void {
    this.confirmationService.confirm({
      message: `¿Eliminar a ${client.name} ${client.lastName}?`,
      header: 'Confirmar eliminación',
      acceptLabel: 'Eliminar',
      rejectLabel: 'Cancelar',
      accept: () => this.deleteClient(client)
    });
  }

  deleteClient(client: Client): void {
    this.api.deleteClient(client.id!).subscribe({
      next: () => { this.messageService.add({ severity: 'success', summary: 'Cliente eliminado', detail: 'Se eliminó correctamente.' }); this.reload(); },
      error: (error) => { const detail = error?.status === 403 ? 'No autorizado (403). Verificá permisos/token de sesión.' : 'No se pudo eliminar el cliente.'; this.messageService.add({ severity: 'error', summary: 'Error', detail }); }
    });
  }

  onSearch(): void { if (!this.searchTerm.trim()) { this.reload(); return; } this.api.searchClients(this.searchTerm).subscribe((clients) => (this.clients = clients)); }
  whatsAppLink(phone: string): string { const digits = (phone || "").replace(/\D/g, ""); return `https://wa.me/${digits}`; }
  private reload(): void { this.api.getClients().subscribe((clients) => (this.clients = clients.slice().reverse())); }
}
