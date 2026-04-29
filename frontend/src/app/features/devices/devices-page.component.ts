import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { SelectModule } from 'primeng/select';
import { ApiService } from '../../core/services/api.service';
import { Device } from '../../shared/models/device.model';

@Component({
  selector: 'app-devices-page',
  standalone: true,
  imports: [CommonModule, FormsModule, CardModule, InputTextModule, ButtonModule, TableModule, SelectModule],
  template: `
    <div class="page-grid">
      <p-card header="Nuevo dispositivo">
        <form class="p-fluid" (ngSubmit)="save()">
          <div class="field"><label>ID Cliente</label><input pInputText [(ngModel)]="draft.clientId" name="clientId" required /></div>
          <div class="field"><label>Marca</label><input pInputText [(ngModel)]="draft.brand" name="brand" required /></div>
          <div class="field"><label>Modelo</label><input pInputText [(ngModel)]="draft.model" name="model" required /></div>
          <div class="field"><label>Serie / IMEI</label><input pInputText [(ngModel)]="draft.serialNumber" name="serialNumber" required /></div>
          <div class="field"><label>Tipo</label><p-select [options]="typeOptions" optionLabel="label" optionValue="value" [(ngModel)]="draft.deviceType" name="deviceType"></p-select></div>
          <button pButton type="submit" label="Guardar dispositivo" icon="pi pi-check"></button>
        </form>
      </p-card>

      <p-card header="Dispositivos">
        <div class="table-toolbar">
          <span class="p-input-icon-left"><i class="pi pi-search"></i><input pInputText [(ngModel)]="searchTerm" (ngModelChange)="onSearch()" placeholder="Buscar por marca, modelo o serie" /></span>
        </div>
        <p-table [value]="devices" size="small" [paginator]="true" [rows]="10">
          <ng-template pTemplate="header"><tr><th>Tipo</th><th>Marca</th><th>Modelo</th><th>Serie</th></tr></ng-template>
          <ng-template pTemplate="body" let-d><tr><td>{{ d.deviceType }}</td><td>{{ d.brand }}</td><td>{{ d.model }}</td><td>{{ d.serialNumber }}</td></tr></ng-template>
        </p-table>
      </p-card>
    </div>
  `
})
export class DevicesPageComponent implements OnInit {
  devices: Device[] = [];
  draft: Device = { brand: '', model: '', serialNumber: '', clientId: '', deviceType: 'NOTEBOOK' };
  typeOptions = [
    { label: 'Desktop', value: 'DESKTOP' },
    { label: 'Notebook', value: 'NOTEBOOK' },
    { label: 'Tablet', value: 'TABLET' },
    { label: 'Celular', value: 'CELULAR' },
    { label: 'Otros', value: 'OTROS' }
  ];
  searchTerm = '';

  constructor(private readonly api: ApiService) {}

  ngOnInit(): void { this.reload(); }

  save(): void {
    this.api.createDevice(this.draft).subscribe(() => {
      this.draft = { brand: '', model: '', serialNumber: '', clientId: '', deviceType: 'NOTEBOOK' };
      this.reload();
    });
  }

  onSearch(): void {
    if (!this.searchTerm.trim()) {
      this.reload();
      return;
    }
    this.api.searchDevices(this.searchTerm).subscribe((devices) => (this.devices = devices));
  }

  private reload(): void { this.api.getDevices().subscribe((devices) => (this.devices = devices.slice().reverse())); }
}
