import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/services/api.service';
import { Device } from '../../shared/models/device.model';

@Component({
  selector: 'app-devices-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <h2>Dispositivos</h2>
    <form (ngSubmit)="save()">
      <input [(ngModel)]="draft.clientId" name="clientId" placeholder="ID Cliente" required />
      <input [(ngModel)]="draft.brand" name="brand" placeholder="Marca" required />
      <input [(ngModel)]="draft.model" name="model" placeholder="Modelo" required />
      <input [(ngModel)]="draft.serialNumber" name="serialNumber" placeholder="Serie / IMEI" required />
      <select [(ngModel)]="draft.deviceType" name="deviceType" required>
        <option value="DESKTOP">Desktop</option>
        <option value="NOTEBOOK">Notebook</option>
        <option value="TABLET">Tablet</option>
        <option value="CELULAR">Celular</option>
        <option value="OTROS">Otros</option>
      </select>
      <button type="submit">Agregar dispositivo</button>
    </form>
    <ul>
      @for (device of devices; track device.serialNumber) {
        <li>{{ device.deviceType }} · {{ device.brand }} {{ device.model }} · {{ device.serialNumber }}</li>
      }
    </ul>
  `
})
export class DevicesPageComponent implements OnInit {
  devices: Device[] = [];
  draft: Device = { brand: '', model: '', serialNumber: '', clientId: '', deviceType: 'NOTEBOOK' };

  constructor(private readonly api: ApiService) {}

  ngOnInit(): void {
    this.reload();
  }

  save(): void {
    this.api.createDevice(this.draft).subscribe(() => {
      this.draft = { brand: '', model: '', serialNumber: '', clientId: '', deviceType: 'NOTEBOOK' };
      this.reload();
    });
  }

  private reload(): void {
    this.api.getDevices().subscribe((devices) => (this.devices = devices));
  }
}
