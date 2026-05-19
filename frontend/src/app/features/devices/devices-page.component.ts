import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
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
  imports: [CommonModule, FormsModule, CardModule, InputTextModule, ButtonModule, TableModule, SelectModule, DialogModule, ConfirmDialogModule],
  providers: [ConfirmationService],
  template: `
    <p-confirmdialog></p-confirmdialog>
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
        <p-table [value]="filteredDevices" size="small" [paginator]="true" [rows]="10" sortMode="multiple">
          <ng-template pTemplate="header"><tr><th pSortableColumn="deviceType">Tipo <p-sortIcon field="deviceType"></p-sortIcon></th><th pSortableColumn="brand">Marca <p-sortIcon field="brand"></p-sortIcon></th><th pSortableColumn="model">Modelo <p-sortIcon field="model"></p-sortIcon></th><th pSortableColumn="serialNumber">Serie <p-sortIcon field="serialNumber"></p-sortIcon></th><th pSortableColumn="clientName">Cliente <p-sortIcon field="clientName"></p-sortIcon></th><th>Acciones</th></tr></ng-template>
          <ng-template pTemplate="body" let-d><tr><td>{{ d.deviceType }}</td><td>{{ d.brand }}</td><td>{{ d.model }}</td><td>{{ d.serialNumber }}</td><td>{{ getClientName(d.clientId) }}</td><td><button pButton type="button" class="p-button-text p-button-sm" icon="pi pi-pencil" ariaLabel="Editar dispositivo" (click)="openEdit(d)"></button><button pButton type="button" class="p-button-text p-button-sm p-button-danger" icon="pi pi-trash" ariaLabel="Eliminar dispositivo" (click)="confirmRemove(d)"></button></td></tr></ng-template>
        </p-table>
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
  }

  getClientName(clientId: string): string {
    const client = this.clients.find((c) => c.id === clientId);
    return client ? `${client.name} ${client.lastName}` : clientId;
  }

  private reload(): void { this.api.getDevices().subscribe((devices) => { this.devices = devices.slice().reverse(); this.applyFilters(); this.changeDetector.detectChanges(); }); }
}
