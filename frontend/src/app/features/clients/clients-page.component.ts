import { Component, OnInit } from '@angular/core';
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
        <p-table [value]="clients" size="small" [paginator]="true" [rows]="10">
          <ng-template pTemplate="header"><tr><th>Nombre</th><th>DNI</th><th>Email</th><th>Teléfono</th><th>Acciones</th></tr></ng-template>
          <ng-template pTemplate="body" let-c>
            <tr>
              <td>{{ c.name }} {{ c.lastName }}</td><td>{{ c.dni }}</td><td>{{ c.email }}</td><td>{{ c.phone }}</td>
              <td>
                <button pButton type="button" class="p-button-text p-button-sm" icon="pi pi-pencil" (click)="openEdit(c)"></button>
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
  `
})
export class ClientsPageComponent implements OnInit {
  clients: Client[] = [];
  draft: Client = { name: '', lastName: '', dni: '', email: '', phone: '' };
  editing: Client = { name: '', lastName: '', dni: '', email: '', phone: '' };
  editVisible = false;
  searchTerm = '';

  constructor(private readonly api: ApiService, private readonly messageService: MessageService, private readonly confirmationService: ConfirmationService) {}

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
      error: () => this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudo actualizar el cliente.' })
    });
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
      error: () => this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudo eliminar el cliente.' })
    });
  }

  onSearch(): void { if (!this.searchTerm.trim()) { this.reload(); return; } this.api.searchClients(this.searchTerm).subscribe((clients) => (this.clients = clients)); }
  private reload(): void { this.api.getClients().subscribe((clients) => (this.clients = clients.slice().reverse())); }
}
