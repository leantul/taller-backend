import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ApiService } from '../../core/services/api.service';
import { Client, ClientListItem, ClientRepairHistoryItem } from '../../shared/models/client.model';
import { Repair } from '../../shared/models/repair.model';
import { RepairDetailDialogComponent } from '../../shared/components/repair-detail-dialog.component';
import { repairStatusClass, repairStatusLabel } from '../../shared/utils/repair-status.util';

type ClientTableColumnKey = 'name' | 'deviceCount' | 'repairCount' | 'phone' | 'actions';
type ClientSortColumn = Exclude<ClientTableColumnKey, 'actions'>;
type ClientTableColumn = {
  key: ClientTableColumnKey;
  label: string;
  width: string;
  sortable: boolean;
};

@Component({
  selector: 'app-clients-page',
  standalone: true,
  imports: [CommonModule, FormsModule, CardModule, InputTextModule, ButtonModule, DialogModule, ConfirmDialogModule, RepairDetailDialogComponent],
  providers: [ConfirmationService],
  template: `
    <p-confirmdialog></p-confirmdialog>
    <section class="page-heading">
      <div><span class="eyebrow">Personas</span><h1>Clientes</h1></div>
      <p>Alta, edición y consulta del historial de reparaciones de cada cliente.</p>
    </section>

    <div class="page-grid">
      <p-card header="Nuevo cliente">
        <form class="p-fluid" (ngSubmit)="save()">
          <div class="field"><label>Nombre</label><input pInputText [(ngModel)]="draft.name" name="name" required /></div>
          <div class="field"><label>Apellido</label><input pInputText [(ngModel)]="draft.lastName" name="lastName" required /></div>
          <div class="field"><label>Referencia</label><textarea class="p-inputtext" rows="3" [(ngModel)]="draft.reference" name="reference" placeholder="Amigo de..., hermano de..."></textarea></div>
          <div class="field"><label>Email</label><input pInputText [(ngModel)]="draft.email" name="email" type="email" required /></div>
          <div class="field"><label>Celular</label><input pInputText [(ngModel)]="draft.phone" name="phone" required /></div>
          <button pButton type="submit" label="Guardar cliente" icon="pi pi-check"></button>
        </form>
      </p-card>

      <p-card header="Clientes">
        <div class="table-toolbar">
          <span class="p-input-icon-left filter-search"><i class="pi pi-search"></i><input pInputText [(ngModel)]="searchTerm" (ngModelChange)="onSearch($event)" placeholder="Buscar por nombre, referencia, teléfono o email" /></span>
        </div>
        <div class="native-table-wrap">
          <table class="native-table resizable-table clients-table">
            <thead>
              <tr>
                @for (column of clientColumns; track column.key) {
                  <th [style.width]="columnWidth(column.key)">
                    @if (column.sortable) {
                      <button class="sortable-th" type="button" (click)="sortByColumn(column.key)">
                        <span>{{ column.label }}</span>
                        <i [ngClass]="sortIcon(column.key)"></i>
                      </button>
                    } @else {
                      <span class="table-head-label">{{ column.label }}</span>
                    }
                    <button
                      class="column-resize-handle"
                      type="button"
                      tabindex="-1"
                      aria-hidden="true"
                      (click)="$event.stopPropagation()"
                      (mousedown)="startColumnResize($event, column.key)">
                    </button>
                  </th>
                }
              </tr>
            </thead>
            <tbody>
              @for (client of clients; track client.id) {
                <tr class="clickable-row" (click)="openClientDialog(client, true)">
                  <td>{{ client.name }} {{ client.lastName }}</td>
                  <td>{{ client.deviceCount }}</td>
                  <td>{{ client.repairCount }}</td>
                  <td>{{ client.phone || '-' }} @if (client.phone) { <a [href]="whatsAppLink(client.phone)" target="_blank" rel="noopener" class="wa-link" (click)="$event.stopPropagation()"><i class="pi pi-whatsapp"></i></a> }</td>
                  <td>
                    <div class="action-buttons">
                      <button class="icon-action" type="button" aria-label="Editar cliente" (click)="stop($event); openEdit(client)"><i class="pi pi-pencil"></i></button>
                      <button class="icon-action" type="button" aria-label="Ver reparaciones del cliente" (click)="stop($event); openClientDialog(client, false)"><i class="pi pi-history"></i></button>
                      <button class="icon-action danger" type="button" aria-label="Eliminar cliente" (click)="stop($event); confirmDelete(client)"><i class="pi pi-trash"></i></button>
                    </div>
                  </td>
                </tr>
              } @empty { <tr><td class="empty-cell" colspan="5">No hay clientes para mostrar.</td></tr> }
            </tbody>
          </table>
        </div>
        <div class="table-pager" aria-label="Paginación de clientes">
          <span>{{ paginationLabel }}</span>
          <div class="pager-actions">
            <button class="pager-button" type="button" [disabled]="currentPage === 0" (click)="previousPage()"><i class="pi pi-chevron-left"></i></button>
            <span>Página {{ currentPage + 1 }} de {{ displayTotalPages }}</span>
            <button class="pager-button" type="button" [disabled]="currentPage + 1 >= displayTotalPages" (click)="nextPage()"><i class="pi pi-chevron-right"></i></button>
          </div>
        </div>
      </p-card>
    </div>

    <p-dialog header="Editar cliente" [(visible)]="editVisible" [modal]="true" [style]="{width:'32rem'}">
      <div class="field"><label>Nombre</label><input pInputText [(ngModel)]="editing.name" /></div>
      <div class="field"><label>Apellido</label><input pInputText [(ngModel)]="editing.lastName" /></div>
      <div class="field"><label>Referencia</label><textarea class="p-inputtext" rows="3" [(ngModel)]="editing.reference" placeholder="Amigo de..., hermano de..."></textarea></div>
      <div class="field"><label>Email</label><input pInputText [(ngModel)]="editing.email" /></div>
      <div class="field"><label>Celular</label><input pInputText [(ngModel)]="editing.phone" /></div>
      <button pButton type="button" label="Guardar cambios" icon="pi pi-check" (click)="updateClient()"></button>
    </p-dialog>

    <p-dialog [header]="showClientData ? 'Detalle del cliente' : 'Reparaciones del cliente'" [(visible)]="historyVisible" [modal]="true" [style]="{width:'70rem', maxWidth:'95vw'}">
      @if (showClientData && selectedClient) {
        <div class="detail-grid client-detail-grid">
          @if (fullName) { <div class="detail-item client-detail-name"><label>Nombre</label><strong>{{ fullName }}</strong></div> }
          @if (selectedClient.phone) { <div class="detail-item client-detail-phone"><label>Teléfono</label><strong>{{ selectedClient.phone }}</strong></div> }
          @if (selectedClient.reference) { <div class="detail-item detail-wide client-detail-reference"><label>Referencia</label><div>{{ selectedClient.reference }}</div></div> }
          @if (selectedClient.email) { <div class="detail-item client-detail-email"><label>Email</label><strong>{{ selectedClient.email }}</strong></div> }
          @if (selectedClient.address) { <div class="detail-item"><label>Dirección</label><strong>{{ selectedClient.address }}</strong></div> }
          @if (selectedClient.birthDate) { <div class="detail-item"><label>Nacimiento</label><strong>{{ selectedClient.birthDate | date:'dd/MM/yyyy' }}</strong></div> }
          @if (selectedClient.phones?.length) { <div class="detail-item"><label>Otros teléfonos</label><strong>{{ selectedClient.phones?.join(', ') }}</strong></div> }
          @if (selectedClient.emails?.length) { <div class="detail-item"><label>Otros emails</label><strong>{{ selectedClient.emails?.join(', ') }}</strong></div> }
          @if (selectedClient.notes) { <div class="detail-item detail-wide"><label>Notas</label><div>{{ selectedClient.notes }}</div></div> }
        </div>
      }
      <div class="native-table-wrap client-history-table">
        <table class="native-table">
          <thead><tr><th>Orden</th><th>Estado</th><th>Dispositivo</th><th>Ingreso</th><th>Entrega</th><th>Detalle</th></tr></thead>
          <tbody>
            @for (repair of clientRepairs; track repair.id) {
              <tr>
                <td>#{{ repair.orderNumber || '-' }}</td>
                <td><span class="status-pill" [ngClass]="statusClass(repair.status)">{{ statusLabel(repair.status) }}</span></td>
                <td>{{ deviceLabel(repair) }}</td>
                <td>{{ repair.receiveDateTime ? (repair.receiveDateTime | date:'dd/MM/yyyy HH:mm') : '-' }}</td>
                <td>{{ repair.returnDateTime ? (repair.returnDateTime | date:'dd/MM/yyyy HH:mm') : '-' }}</td>
                <td><button class="icon-action" type="button" aria-label="Ver reparación" (click)="openRepairDetail(repair)"><i class="pi pi-eye"></i></button></td>
              </tr>
            } @empty { <tr><td class="empty-cell" colspan="6">Este cliente no tiene reparaciones registradas.</td></tr> }
          </tbody>
        </table>
      </div>
      <div class="table-pager" aria-label="Paginación del historial">
        <span>{{ historyPaginationLabel }}</span>
        <div class="pager-actions">
          <button class="pager-button" type="button" [disabled]="historyPage === 0" (click)="previousHistoryPage()"><i class="pi pi-chevron-left"></i></button>
          <span>Página {{ historyPage + 1 }} de {{ displayHistoryTotalPages }}</span>
          <button class="pager-button" type="button" [disabled]="historyPage + 1 >= displayHistoryTotalPages" (click)="nextHistoryPage()"><i class="pi pi-chevron-right"></i></button>
        </div>
      </div>
    </p-dialog>
    <app-repair-detail-dialog></app-repair-detail-dialog>
  `
})
export class ClientsPageComponent implements OnInit, OnDestroy {
  @ViewChild(RepairDetailDialogComponent) private repairDetailDialog?: RepairDetailDialogComponent;
  private readonly destroy$ = new Subject<void>();
  private readonly search$ = new Subject<string>();
  clients: ClientListItem[] = [];
  draft: Client = this.emptyClient();
  editing: Client = this.emptyClient();
  editVisible = false;
  searchTerm = '';
  sortBy: 'createdAt' | ClientSortColumn = 'createdAt';
  sortDir: 'asc' | 'desc' = 'desc';
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;
  historyVisible = false;
  showClientData = true;
  selectedClientId = '';
  selectedClient: Client | null = null;
  clientRepairs: ClientRepairHistoryItem[] = [];
  historyPage = 0;
  historyPageSize = 5;
  historyTotalElements = 0;
  historyTotalPages = 0;
  readonly clientColumns: ClientTableColumn[] = [
    { key: 'name', label: 'Nombre', width: '16rem', sortable: true },
    { key: 'deviceCount', label: 'Cantidad de dispositivos', width: '13rem', sortable: true },
    { key: 'repairCount', label: 'Reparaciones', width: '10rem', sortable: true },
    { key: 'phone', label: 'Teléfono', width: '12rem', sortable: true },
    { key: 'actions', label: 'Acciones', width: '11rem', sortable: false }
  ];
  private readonly columnWidthStorageKey = 'taller.clients.columnWidths';
  private resizingColumnKey: ClientTableColumnKey | null = null;
  private resizeStartX = 0;
  private resizeStartWidth = 0;

  constructor(private readonly api: ApiService, private readonly messages: MessageService, private readonly confirmations: ConfirmationService, private readonly changeDetector: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.restoreColumnWidths();
    this.search$.pipe(debounceTime(300), distinctUntilChanged(), takeUntil(this.destroy$)).subscribe(() => { this.currentPage = 0; this.reload(); });
    this.reload();
  }
  ngOnDestroy(): void { this.destroy$.next(); this.destroy$.complete(); }

  save(): void {
    this.api.createClient(this.draft).subscribe({
      next: () => { this.messages.add({ severity: 'success', summary: 'Cliente guardado', detail: 'El cliente se creó correctamente.' }); this.draft = this.emptyClient(); this.reload(); },
      error: () => this.messages.add({ severity: 'error', summary: 'Error', detail: 'No se pudo guardar el cliente.' })
    });
  }
  openEdit(client: ClientListItem): void {
    this.api.getClientById(client.id).subscribe((detail) => { this.editing = { ...detail }; this.editVisible = true; });
  }
  updateClient(): void {
    this.api.createClient(this.editing).subscribe({
      next: () => { this.messages.add({ severity: 'success', summary: 'Cliente actualizado', detail: 'Cambios guardados.' }); this.editVisible = false; this.reload(); },
      error: () => this.messages.add({ severity: 'error', summary: 'Error', detail: 'No se pudo actualizar el cliente.' })
    });
  }
  openClientDialog(client: ClientListItem, includeClient: boolean): void {
    this.selectedClientId = client.id;
    this.showClientData = includeClient;
    this.selectedClient = { id: client.id, name: client.name, lastName: client.lastName, reference: '', email: '', phone: client.phone || '' };
    this.historyPage = 0;
    this.loadHistory();
  }
  openRepairDetail(repair: ClientRepairHistoryItem): void {
    this.repairDetailDialog?.open(repair.id, this.fullName || '-', this.deviceLabel(repair));
  }
  confirmDelete(client: ClientListItem): void {
    this.confirmations.confirm({ message: `¿Eliminar a ${client.name} ${client.lastName}?`, header: 'Confirmar eliminación', acceptLabel: 'Eliminar', rejectLabel: 'Cancelar', accept: () => this.deleteClient(client.id) });
  }
  deleteClient(id: string): void {
    this.api.deleteClient(id).subscribe({ next: () => { this.messages.add({ severity: 'success', summary: 'Cliente eliminado', detail: 'Se eliminó correctamente.' }); this.reload(); }, error: () => this.messages.add({ severity: 'error', summary: 'Error', detail: 'No se pudo eliminar el cliente.' }) });
  }
  onSearch(term: string): void { this.search$.next(term.trim()); }
  sortByColumn(column: ClientTableColumnKey): void {
    if (column === 'actions') return;
    if (this.sortBy === column) {
      this.sortDir = this.sortDir === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = column;
      this.sortDir = column === 'name' ? 'asc' : 'desc';
    }
    this.currentPage = 0;
    this.reload();
  }
  sortIcon(column: ClientTableColumnKey): string {
    if (column === 'actions' || this.sortBy !== column) return 'pi pi-sort-alt';
    return this.sortDir === 'asc' ? 'pi pi-sort-amount-up-alt' : 'pi pi-sort-amount-down';
  }
  previousPage(): void { if (this.currentPage > 0) { this.currentPage--; this.reload(); } }
  nextPage(): void { if (this.currentPage + 1 < this.totalPages) { this.currentPage++; this.reload(); } }
  previousHistoryPage(): void { if (this.historyPage > 0) { this.historyPage--; this.loadHistory(); } }
  nextHistoryPage(): void { if (this.historyPage + 1 < this.historyTotalPages) { this.historyPage++; this.loadHistory(); } }
  stop(event: Event): void { event.stopPropagation(); }
  whatsAppLink(phone: string): string { return `https://wa.me/${(phone || '').replace(/\D/g, '')}`; }
  deviceLabel(repair: ClientRepairHistoryItem): string { return [repair.deviceBrand, repair.deviceModel].filter(Boolean).join(' ') || '-'; }
  statusLabel(status: Repair['status']): string { return repairStatusLabel(status); }
  statusClass(status: Repair['status']): string { return repairStatusClass(status); }
  get fullName(): string { return this.selectedClient ? `${this.selectedClient.name || ''} ${this.selectedClient.lastName || ''}`.trim() : ''; }
  get displayTotalPages(): number { return Math.max(1, this.totalPages); }
  get displayHistoryTotalPages(): number { return Math.max(1, this.historyTotalPages); }
  get paginationLabel(): string { if (!this.totalElements) return '0 clientes'; const start = this.currentPage * this.pageSize + 1; return `${start}-${Math.min(start + this.clients.length - 1, this.totalElements)} de ${this.totalElements} clientes`; }
  get historyPaginationLabel(): string { if (!this.historyTotalElements) return '0 reparaciones'; const start = this.historyPage * this.historyPageSize + 1; return `${start}-${Math.min(start + this.clientRepairs.length - 1, this.historyTotalElements)} de ${this.historyTotalElements} reparaciones`; }

  columnWidth(columnKey: ClientTableColumnKey): string {
    return this.clientColumns.find((column) => column.key === columnKey)?.width || 'auto';
  }

  startColumnResize(event: MouseEvent, columnKey: ClientTableColumnKey): void {
    event.preventDefault();
    event.stopPropagation();
    const header = (event.currentTarget as HTMLElement).closest('th');
    if (!header) return;

    this.resizingColumnKey = columnKey;
    this.resizeStartX = event.clientX;
    this.resizeStartWidth = header.getBoundingClientRect().width;

    const onMouseMove = (moveEvent: MouseEvent) => {
      if (!this.resizingColumnKey) return;
      const nextWidth = Math.max(96, Math.round(this.resizeStartWidth + (moveEvent.clientX - this.resizeStartX)));
      const column = this.clientColumns.find((item) => item.key === this.resizingColumnKey);
      if (column) {
        column.width = `${nextWidth}px`;
        this.persistColumnWidths();
        this.changeDetector.detectChanges();
      }
    };

    const onMouseUp = () => {
      this.resizingColumnKey = null;
      window.removeEventListener('mousemove', onMouseMove);
      window.removeEventListener('mouseup', onMouseUp);
    };

    window.addEventListener('mousemove', onMouseMove);
    window.addEventListener('mouseup', onMouseUp);
  }

  private reload(): void {
    this.api.getClientPage(this.currentPage, this.pageSize, this.searchTerm.trim(), this.sortBy, this.sortDir).subscribe((page) => {
      this.clients = page.content; this.currentPage = page.page; this.totalElements = page.totalElements; this.totalPages = page.totalPages; this.changeDetector.detectChanges();
    });
  }
  private loadHistory(): void {
    this.api.getClientHistory(this.selectedClientId, this.historyPage, this.historyPageSize, this.showClientData).subscribe((result) => {
      if (result.client) this.selectedClient = result.client;
      this.clientRepairs = result.repairs.content;
      this.historyPage = result.repairs.page;
      this.historyTotalElements = result.repairs.totalElements;
      this.historyTotalPages = result.repairs.totalPages;
      this.historyVisible = true;
      this.changeDetector.detectChanges();
    });
  }
  private restoreColumnWidths(): void {
    const stored = localStorage.getItem(this.columnWidthStorageKey);
    if (!stored) return;
    try {
      const widths = JSON.parse(stored) as Partial<Record<ClientTableColumnKey, string>>;
      this.clientColumns.forEach((column) => {
        if (widths[column.key]) column.width = widths[column.key]!;
      });
    } catch {
      localStorage.removeItem(this.columnWidthStorageKey);
    }
  }

  private persistColumnWidths(): void {
    localStorage.setItem(
      this.columnWidthStorageKey,
      JSON.stringify(Object.fromEntries(this.clientColumns.map((column) => [column.key, column.width])))
    );
  }

  private emptyClient(): Client { return { name: '', lastName: '', reference: '', email: '', phone: '' }; }
}
