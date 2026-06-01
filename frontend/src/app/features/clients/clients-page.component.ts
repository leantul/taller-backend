import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ApiService } from '../../core/services/api.service';
import { Client } from '../../shared/models/client.model';
import { Repair } from '../../shared/models/repair.model';

@Component({
  selector: 'app-clients-page',
  standalone: true,
  imports: [CommonModule, FormsModule, CardModule, InputTextModule, ButtonModule, DialogModule, ConfirmDialogModule],
  providers: [ConfirmationService],
  template: `
    <p-confirmdialog></p-confirmdialog>
    <section class="page-heading">
      <div>
        <span class="eyebrow">Personas</span>
        <h1>Clientes</h1>
      </div>
      <p>Alta, edicion y consulta del historial de reparaciones de cada cliente.</p>
    </section>

    <div class="page-grid">
      <p-card header="Nuevo cliente">
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
          <span class="p-input-icon-left filter-search"><i class="pi pi-search"></i><input pInputText [(ngModel)]="searchTerm" (ngModelChange)="onSearch()" placeholder="Buscar por nombre, DNI o email" /></span>
        </div>
        <div class="native-table-wrap">
          <table class="native-table">
            <thead><tr><th>Nombre</th><th>DNI</th><th>Email</th><th>Teléfono</th><th>Acciones</th></tr></thead>
            <tbody>
              @for (c of visibleClients; track c.id || (c.dni + c.email)) {
                <tr>
                  <td>{{ c.name }} {{ c.lastName }}</td>
                  <td>{{ c.dni }}</td>
                  <td>{{ c.email }}</td>
                  <td>{{ c.phone }} <a [href]="whatsAppLink(c.phone)" target="_blank" rel="noopener" class="wa-link"><i class="pi pi-whatsapp"></i></a></td>
                  <td>
                    <div class="action-buttons">
                      <button class="icon-action" type="button" aria-label="Editar cliente" (click)="openEdit(c)"><i class="pi pi-pencil"></i></button>
                      <button class="icon-action" type="button" aria-label="Ver reparaciones del cliente" (click)="openRepairs(c)"><i class="pi pi-history"></i></button>
                      <button class="icon-action danger" type="button" aria-label="Eliminar cliente" (click)="confirmDelete(c)"><i class="pi pi-trash"></i></button>
                    </div>
                  </td>
                </tr>
              } @empty {
                <tr><td class="empty-cell" colspan="5">No hay clientes para mostrar.</td></tr>
              }
            </tbody>
          </table>
        </div>
        <div class="table-pager" aria-label="Paginación de clientes">
          <span>{{ paginationLabel }}</span>
          <div class="pager-actions">
            <button class="pager-button" type="button" [disabled]="currentPage === 1" (click)="previousPage()"><i class="pi pi-chevron-left"></i></button>
            <span>Página {{ currentPage }} de {{ totalPages }}</span>
            <button class="pager-button" type="button" [disabled]="currentPage === totalPages" (click)="nextPage()"><i class="pi pi-chevron-right"></i></button>
          </div>
        </div>
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
      <div class="native-table-wrap">
        <table class="native-table">
          <thead><tr><th>Orden</th><th>Estado</th><th>Ingreso</th><th>Entrega</th><th>Detalle</th></tr></thead>
          <tbody>
            @for (r of clientRepairs; track r.id || r.orderNumber) {
              <tr>
                <td>#{{ r.orderNumber }}</td>
                <td>{{ r.status }}</td>
                <td>{{ r.receiveDateTime ? (r.receiveDateTime | date:'dd/MM/yyyy HH:mm') : '-' }}</td>
                <td>{{ r.returnDateTime ? (r.returnDateTime | date:'dd/MM/yyyy HH:mm') : '-' }}</td>
                <td><button class="icon-action" type="button" aria-label="Ver reparación" (click)="goToRepair(r)"><i class="pi pi-eye"></i></button></td>
              </tr>
            } @empty {
              <tr><td class="empty-cell" colspan="5">Este cliente no tiene reparaciones registradas.</td></tr>
            }
          </tbody>
        </table>
      </div>
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
  currentPage = 1;
  pageSize = 10;
  repairsVisible = false;
  clientRepairs: Repair[] = [];
  selectedRepairsClient: Client | null = null;
  selectedRepair: Repair | null = null;

  constructor(private readonly api: ApiService, private readonly messageService: MessageService, private readonly confirmationService: ConfirmationService, private readonly router: Router, private readonly changeDetector: ChangeDetectorRef) {}

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
      this.selectedRepairsClient = client;
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
    const term = this.selectedRepairsClient
      ? `${this.selectedRepairsClient.name || ''} ${this.selectedRepairsClient.lastName || ''}`.trim()
      : '';
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

  onSearch(): void { if (!this.searchTerm.trim()) { this.reload(); return; } this.api.searchClients(this.searchTerm).subscribe((clients) => { this.clients = clients; this.currentPage = 1; this.changeDetector.detectChanges(); }); }
  whatsAppLink(phone: string): string { const digits = (phone || "").replace(/\D/g, ""); return `https://wa.me/${digits}`; }
  get totalPages(): number { return Math.max(1, Math.ceil(this.clients.length / this.pageSize)); }
  get visibleClients(): Client[] {
    const page = Math.min(this.currentPage, this.totalPages);
    const start = (page - 1) * this.pageSize;
    return this.clients.slice(start, start + this.pageSize);
  }
  get paginationLabel(): string {
    if (!this.clients.length) return '0 clientes';
    const start = (Math.min(this.currentPage, this.totalPages) - 1) * this.pageSize + 1;
    const end = Math.min(start + this.pageSize - 1, this.clients.length);
    return `${start}-${end} de ${this.clients.length} clientes`;
  }
  previousPage(): void { this.currentPage = Math.max(1, this.currentPage - 1); }
  nextPage(): void { this.currentPage = Math.min(this.totalPages, this.currentPage + 1); }
  private reload(): void { this.api.getClients().subscribe((clients) => { this.clients = clients.slice().reverse(); this.currentPage = 1; this.changeDetector.detectChanges(); }); }
}
