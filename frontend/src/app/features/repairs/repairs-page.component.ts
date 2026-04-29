import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { SelectModule } from 'primeng/select';
import { InputNumberModule } from 'primeng/inputnumber';
import { TagModule } from 'primeng/tag';
import { DatePickerModule } from 'primeng/datepicker';
import { DialogModule } from 'primeng/dialog';
import { ApiService } from '../../core/services/api.service';
import { Repair } from '../../shared/models/repair.model';
import { Client } from '../../shared/models/client.model';
import { Device } from '../../shared/models/device.model';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-repairs-page',
  standalone: true,
  imports: [CommonModule, FormsModule, CardModule, InputTextModule, ButtonModule, TableModule, SelectModule, InputNumberModule, TagModule, DatePickerModule, DialogModule],
  templateUrl: './repairs-page.component.html'
})
export class RepairsPageComponent implements OnInit {
  repairs: Repair[] = [];
  filteredRepairs: Repair[] = [];
  clients: Client[] = [];
  clientDevices: Device[] = [];
  draftDevice: Device = { brand: '', model: '', serialNumber: '', clientId: '', deviceType: 'NOTEBOOK' };
  showClientModal = false;
  showDeviceModal = false;
  clientSearch = '';
  selectedClientName = '';
  draft: Repair = { idDevice: '', idClient: '', orderNumber: '', description: '', status: 'POR_RECIBIR', price: 0, quotedAmount: 0, quoteNotes: '' };
  searchTerm = '';
  fromDate: Date | null = null;
  toDate: Date | null = null;
  statusOptions = [
    { label: 'Por recibir', value: 'POR_RECIBIR' }, { label: 'Recibida', value: 'RECIBIDA' }, { label: 'Presupuestada', value: 'PRESUPUESTADA_ESPERANDO_RESPUESTA' }, { label: 'Haciendo', value: 'HACIENDO' }, { label: 'Esperando retiro', value: 'ESPERANDO_RETIRO' }, { label: 'Retirada', value: 'RETIRADA' }
  ];
  typeOptions = [
    { label: 'Desktop', value: 'DESKTOP' }, { label: 'Notebook', value: 'NOTEBOOK' }, { label: 'Tablet', value: 'TABLET' }, { label: 'Celular', value: 'CELULAR' }, { label: 'Otros', value: 'OTROS' }
  ];

  constructor(private readonly api: ApiService, private readonly messageService: MessageService) {}
  ngOnInit(): void { this.reload(); this.api.getClients().subscribe(c => this.clients = c); }

  selectClient(client: Client): void {
    this.draft.idClient = client.id || '';
    this.selectedClientName = `${client.name} ${client.lastName}`.trim();
    this.showClientModal = false;
    this.api.getDevices().subscribe((devices) => this.clientDevices = devices.filter(d => d.clientId === this.draft.idClient));
  }

  createDeviceInline(): void {
    this.draftDevice.clientId = this.draft.idClient;
    this.api.createDevice(this.draftDevice).subscribe((device) => {
      this.clientDevices = [device, ...this.clientDevices];
      this.draft.idDevice = device.id || '';
      this.draftDevice = { brand: '', model: '', serialNumber: '', clientId: this.draft.idClient, deviceType: 'NOTEBOOK' };
      this.showDeviceModal = false;
    });
  }

  save(): void {
    const nextOrder = (this.repairs.length + 1).toString();
    const payload = { ...this.draft, orderNumber: nextOrder };
    this.api.createRepair(payload).subscribe({
      next: () => {
        this.messageService.add({ severity: 'success', summary: 'Reparación guardada', detail: `Alta creada con orden #${nextOrder}.` });
        this.draft = { idDevice: '', idClient: '', orderNumber: '', description: '', status: 'POR_RECIBIR', price: 0, quotedAmount: 0, quoteNotes: '' };
        this.selectedClientName='';
        this.clientDevices=[];
        this.reload();
      },
      error: () => this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudo guardar la reparación.' })
    });
  }
  updateBudget(repair: Repair): void { this.api.updateRepair(repair).subscribe({ next: () => { this.messageService.add({ severity: 'success', summary: 'Actualizado', detail: 'Presupuesto actualizado.' }); this.reload(); }, error: () => this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudo actualizar.' }) }); }
  applyFilters(): void {
    const term = this.searchTerm.trim().toLowerCase();
    this.filteredRepairs = this.repairs.filter((r) => {
      const matchesTerm = !term || [r.idClient, r.idDevice, r.orderNumber, r.description].filter(Boolean).join(' ').toLowerCase().includes(term);
      const receive = r.receiveDateTime ? new Date(r.receiveDateTime) : null;
      const matchesFrom = !this.fromDate || (receive && receive >= this.fromDate);
      const matchesTo = !this.toDate || (receive && receive <= this.toDate);
      return Boolean(matchesTerm && matchesFrom && matchesTo);
    });
  }
  private reload(): void { this.api.getRepairs().subscribe((repairs) => { this.repairs = repairs.slice().reverse(); this.applyFilters(); }); }

  get filteredClients(): Client[] {
    const term = this.clientSearch.trim().toLowerCase();
    if (!term) return this.clients;
    return this.clients.filter((c) => `${c.name} ${c.lastName} ${c.phone} ${c.email}`.toLowerCase().includes(term));
  }
}
