import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { SelectModule } from 'primeng/select';
import { DialogModule } from 'primeng/dialog';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService } from 'primeng/api';
import { ApiService } from '../../core/services/api.service';
import { Device } from '../../shared/models/device.model';
import { Client } from '../../shared/models/client.model';

@Component({
  selector: 'app-devices-page',
  standalone: true,
  imports: [CommonModule, FormsModule, CardModule, InputTextModule, ButtonModule, SelectModule, DialogModule, ConfirmDialogModule],
  providers: [ConfirmationService],
  template: `
    <p-confirmdialog></p-confirmdialog>
    <section class="page-heading">
      <div>
        <span class="eyebrow">Inventario</span>
        <h1>Dispositivos</h1>
      </div>
      <p>Relacion entre clientes y equipos, con filtros rapidos para trabajar sobre el parque activo.</p>
    </section>

    <div class="page-grid">
      <p-card header="Nuevo dispositivo">
        <form class="p-fluid" (ngSubmit)="save()">
          <div class="field"><label>Cliente</label><p-select [options]="clientOptions" optionLabel="label" optionValue="value" [(ngModel)]="draft.clientId" name="clientId" [filter]="true" filterBy="label" required></p-select></div>
          <div class="field"><label>Marca</label><input pInputText [(ngModel)]="draft.brand" name="brand" required /></div>
          <div class="field"><label>Modelo</label><input pInputText [(ngModel)]="draft.model" name="model" required /></div>
          <div class="field"><label>Serie / IMEI</label><input pInputText [(ngModel)]="draft.serialNumber" name="serialNumber" required /></div>
          <div class="field"><label>Tipo</label><p-select [options]="typeOptions" optionLabel="label" optionValue="value" [(ngModel)]="draft.deviceType" name="deviceType"></p-select></div>
          <button pButton type="submit" label="Guardar dispositivo" icon="pi pi-check"></button>
        </form>
      </p-card>

      <p-card header="Dispositivos">
        <div class="table-toolbar multi repairs-filters">
          <span class="p-input-icon-left filter-search"><i class="pi pi-search"></i><input pInputText [(ngModel)]="searchTerm" (ngModelChange)="applyFilters()" placeholder="Buscar por cualquier campo" /></span>
          <p-select styleClass="compact-filter" [options]="clientOptions" optionLabel="label" optionValue="value" [(ngModel)]="selectedClientId" (ngModelChange)="applyFilters()" placeholder="Filtrar por cliente" [showClear]="true" appendTo="body"></p-select>
        </div>
        <div class="native-table-wrap">
          <table class="native-table">
            <thead><tr><th>Tipo</th><th>Marca</th><th>Modelo</th><th>Serie</th><th>Cliente</th><th>Acciones</th></tr></thead>
            <tbody>
              @for (d of visibleDevices; track d.id || d.serialNumber) {
                <tr>
                  <td>{{ d.deviceType }}</td>
                  <td>{{ d.brand }}</td>
                  <td>{{ d.model }}</td>
                  <td>{{ d.serialNumber }}</td>
                  <td>{{ getClientName(d.clientId) }}</td>
                  <td>
                    <div class="action-buttons">
                      <button class="icon-action" type="button" aria-label="Editar dispositivo" (click)="openEdit(d)"><i class="pi pi-pencil"></i></button>
                      <button class="icon-action danger" type="button" aria-label="Eliminar dispositivo" (click)="confirmRemove(d)"><i class="pi pi-trash"></i></button>
                    </div>
                  </td>
                </tr>
              } @empty {
                <tr><td class="empty-cell" colspan="6">No hay dispositivos para mostrar.</td></tr>
              }
            </tbody>
          </table>
        </div>
        <div class="table-pager" aria-label="Paginación de dispositivos">
          <span>{{ paginationLabel }}</span>
          <div class="pager-actions">
            <button class="pager-button" type="button" [disabled]="currentPage === 1" (click)="previousPage()"><i class="pi pi-chevron-left"></i></button>
            <span>Página {{ currentPage }} de {{ totalPages }}</span>
            <button class="pager-button" type="button" [disabled]="currentPage === totalPages" (click)="nextPage()"><i class="pi pi-chevron-right"></i></button>
          </div>
        </div>
      </p-card>
    </div>

    <p-dialog header="Editar dispositivo" [(visible)]="editVisible" [modal]="true" [style]="{width:'34rem'}">
      <div class="field"><label>Cliente</label><p-select [options]="clientOptions" optionLabel="label" optionValue="value" [(ngModel)]="editing.clientId" [filter]="true" filterBy="label"></p-select></div>
      <div class="field"><label>Marca</label><input pInputText [(ngModel)]="editing.brand" /></div>
      <div class="field"><label>Modelo</label><input pInputText [(ngModel)]="editing.model" /></div>
      <div class="field"><label>Serie / IMEI</label><input pInputText [(ngModel)]="editing.serialNumber" /></div>
      <div class="field"><label>Tipo</label><p-select [options]="typeOptions" optionLabel="label" optionValue="value" [(ngModel)]="editing.deviceType"></p-select></div>
      <button pButton type="button" label="Guardar cambios" icon="pi pi-check" (click)="update()"></button>
    </p-dialog>
  `
})
export class DevicesPageComponent implements OnInit {
  devices: Device[] = [];
  filteredDevices: (Device & { clientName?: string })[] = [];
  clients: Client[] = [];
  draft: Device = { brand: '', model: '', serialNumber: '', clientId: '', deviceType: 'NOTEBOOK' };
  editing: Device = { brand: '', model: '', serialNumber: '', clientId: '', deviceType: 'NOTEBOOK' };
  editVisible = false;
  selectedClientId: string | null = null;
  searchTerm = '';
  currentPage = 1;
  pageSize = 10;
  typeOptions = [
    { label: 'Desktop', value: 'DESKTOP' }, { label: 'Notebook', value: 'NOTEBOOK' }, { label: 'Tablet', value: 'TABLET' }, { label: 'Celular', value: 'CELULAR' }, { label: 'Otros', value: 'OTROS' }
  ];

  constructor(private readonly api: ApiService, private readonly confirmationService: ConfirmationService, private readonly changeDetector: ChangeDetectorRef) {}

  ngOnInit(): void { this.api.getClients().subscribe((clients) => { this.clients = clients; this.reload(); }); }

  get clientOptions(): { label: string; value: string }[] {
    return this.clients.map((client) => ({ label: `${client.name} ${client.lastName}`.trim(), value: client.id! }));
  }

  save(): void {
    this.api.createDevice(this.draft).subscribe(() => {
      this.draft = { brand: '', model: '', serialNumber: '', clientId: '', deviceType: 'NOTEBOOK' };
      this.reload();
    });
  }

  openEdit(device: Device): void { this.editing = { ...device }; this.editVisible = true; }

  update(): void {
    this.api.updateDevice(this.editing).subscribe(() => { this.editVisible = false; this.reload(); });
  }

  confirmRemove(device: Device): void {
    this.confirmationService.confirm({
      message: `¿Eliminar dispositivo ${device.brand} ${device.model}?`,
      header: 'Confirmar eliminación',
      acceptLabel: 'Eliminar',
      rejectLabel: 'Cancelar',
      accept: () => this.remove(device)
    });
  }

  remove(device: Device): void {
    if (!device.id) return;
    this.api.deleteDevice(device.id).subscribe(() => this.reload());
  }

  applyFilters(): void {
    const term = this.searchTerm.trim().toLowerCase();
    this.filteredDevices = this.devices
      .map((device) => ({ ...device, clientName: this.getClientName(device.clientId) }))
      .filter((device) => {
        const byClient = !this.selectedClientId || device.clientId === this.selectedClientId;
        const byTerm = !term || `${device.deviceType} ${device.brand} ${device.model} ${device.serialNumber} ${device.clientId} ${device.clientName}`.toLowerCase().includes(term);
        return byClient && byTerm;
      });
    this.currentPage = 1;
  }

  getClientName(clientId: string): string {
    const client = this.clients.find((c) => c.id === clientId);
    return client ? `${client.name} ${client.lastName}` : clientId;
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.filteredDevices.length / this.pageSize));
  }

  get visibleDevices(): (Device & { clientName?: string })[] {
    const page = Math.min(this.currentPage, this.totalPages);
    const start = (page - 1) * this.pageSize;
    return this.filteredDevices.slice(start, start + this.pageSize);
  }

  get paginationLabel(): string {
    if (!this.filteredDevices.length) return '0 dispositivos';
    const start = (Math.min(this.currentPage, this.totalPages) - 1) * this.pageSize + 1;
    const end = Math.min(start + this.pageSize - 1, this.filteredDevices.length);
    return `${start}-${end} de ${this.filteredDevices.length} dispositivos`;
  }

  previousPage(): void {
    this.currentPage = Math.max(1, this.currentPage - 1);
  }

  nextPage(): void {
    this.currentPage = Math.min(this.totalPages, this.currentPage + 1);
  }

  private reload(): void { this.api.getDevices().subscribe((devices) => { this.devices = devices.slice().reverse(); this.applyFilters(); this.changeDetector.detectChanges(); }); }
}
