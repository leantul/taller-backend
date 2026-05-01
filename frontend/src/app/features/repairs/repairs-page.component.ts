import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
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
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService } from 'primeng/api';
import { ApiService } from '../../core/services/api.service';
import { Repair } from '../../shared/models/repair.model';
import { Client } from '../../shared/models/client.model';
import { Device } from '../../shared/models/device.model';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-repairs-page',
  standalone: true,
  imports: [CommonModule, FormsModule, CardModule, InputTextModule, ButtonModule, TableModule, SelectModule, InputNumberModule, TagModule, DatePickerModule, DialogModule, ConfirmDialogModule],
  providers: [ConfirmationService],
  templateUrl: './repairs-page.component.html'
})
export class RepairsPageComponent implements OnInit {
  repairs: Repair[] = [];
  filteredRepairs: Repair[] = [];
  clients: Client[] = [];
  clientsById = new Map<string, Client>();
  clientDevices: Device[] = [];
  allDevices: Device[] = [];
  draftDevice: Device = { brand: '', model: '', serialNumber: '', clientId: '', deviceType: 'NOTEBOOK' };
  showClientModal = false;
  showDeviceModal = false;
  showStatusModal = false;
  showEditModal = false;
  clientSearch = '';
  selectedClientName = '';
  draft: Repair = { idDevice: '', idClient: '', orderNumber: '', description: '', status: 'POR_RECIBIR', price: 0, quotedAmount: 0, quoteNotes: '' };
  editingRepair: Repair = { idDevice: '', idClient: '', orderNumber: '', description: '', status: 'POR_RECIBIR', price: 0, quotedAmount: 0, quoteNotes: '' };
  statusEditingRepair: Repair | null = null;
  searchTerm = '';
  isSaving = false;
  isUpdating = false;
  isDeleting = false;
  fromDate: Date | null = null;
  toDate: Date | null = null;
  statusOptions = [
    { label: 'Por recibir', value: 'POR_RECIBIR' }, { label: 'Recibida', value: 'RECIBIDA' }, { label: 'Presupuestada', value: 'PRESUPUESTADA_ESPERANDO_RESPUESTA' }, { label: 'Haciendo', value: 'HACIENDO' }, { label: 'Esperando retiro', value: 'ESPERANDO_RETIRO' }, { label: 'Retirada', value: 'RETIRADA' }
  ];
  typeOptions = [
    { label: 'Desktop', value: 'DESKTOP' }, { label: 'Notebook', value: 'NOTEBOOK' }, { label: 'Tablet', value: 'TABLET' }, { label: 'Celular', value: 'CELULAR' }, { label: 'Otros', value: 'OTROS' }
  ];

  constructor(private readonly api: ApiService, private readonly messageService: MessageService, private readonly route: ActivatedRoute, private readonly confirmationService: ConfirmationService) {}
  ngOnInit(): void { this.reload(); this.api.getClients().subscribe(c => { this.clients = c; this.clientsById = new Map(c.filter(item => !!item.id).map(item => [item.id!, item])); }); this.api.getDevices().subscribe(d => this.allDevices = d); this.route.queryParamMap.subscribe((params) => { const q = params.get('q') || ''; if (q !== this.searchTerm) { this.searchTerm = q; this.applyFilters(); } }); }

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
      this.allDevices = [device, ...this.allDevices];
      this.draft.idDevice = device.id || '';
      this.draftDevice = { brand: '', model: '', serialNumber: '', clientId: this.draft.idClient, deviceType: 'NOTEBOOK' };
      this.showDeviceModal = false;
    });
  }

  save(): void {
    if (!this.draft.idClient || !this.draft.idDevice || !this.draft.description?.trim()) {
      this.messageService.add({ severity: 'warn', summary: 'Faltan datos', detail: 'Cliente, dispositivo y falla reportada son obligatorios.' });
      return;
    }
    const payload = { ...this.draft, orderNumber: this.draft.orderNumber || '' };
    this.isSaving = true;
    this.api.createRepair(payload).subscribe({
      next: () => {
        this.isSaving = false;
        this.messageService.add({ severity: 'success', summary: 'Reparación guardada', detail: 'Alta creada correctamente.' });
        this.draft = { idDevice: '', idClient: '', orderNumber: '', description: '', status: 'POR_RECIBIR', price: 0, quotedAmount: 0, quoteNotes: '' };
        this.selectedClientName='';
        this.clientDevices=[];
        this.reload();
      },
      error: (error) => { this.isSaving = false; this.messageService.add({ severity: 'error', summary: 'Error', detail: this.errorDetail(error, 'No se pudo guardar la reparación.') }); }
    });
  }

  openStatusModal(repair: Repair): void {
    this.statusEditingRepair = { ...repair };
    this.showStatusModal = true;
  }

  saveStatus(): void {
    if (!this.statusEditingRepair) return;
    this.isUpdating = true;
    this.api.updateRepair(this.statusEditingRepair).subscribe({
      next: () => {
        this.isUpdating = false; this.messageService.add({ severity: 'success', summary: 'Estado actualizado', detail: 'Se actualizó el estado.' }); this.showStatusModal = false; this.statusEditingRepair = null; this.reload(); },
      error: (error) => { this.isUpdating = false; this.messageService.add({ severity: 'error', summary: 'Error', detail: this.errorDetail(error, 'No se pudo actualizar el estado.') }); }
    });
  }

  openEditModal(repair: Repair): void {
    this.editingRepair = { ...repair };
    this.showEditModal = true;
  }

  saveRepairChanges(): void {
    this.isUpdating = true;
    this.api.updateRepair(this.editingRepair).subscribe({
      next: () => {
        this.isUpdating = false; this.messageService.add({ severity: 'success', summary: 'Reparación actualizada', detail: 'Los cambios fueron guardados.' }); this.showEditModal = false; this.reload(); },
      error: (error) => { this.isUpdating = false; this.messageService.add({ severity: 'error', summary: 'Error', detail: this.errorDetail(error, 'No se pudo actualizar la reparación.') }); }
    });
  }

  confirmDeleteRepair(repair: Repair): void {
    this.confirmationService.confirm({
      message: `¿Eliminar la orden #${repair.orderNumber}?`,
      header: 'Confirmar eliminación',
      acceptLabel: 'Eliminar',
      rejectLabel: 'Cancelar',
      accept: () => this.deleteRepair(repair)
    });
  }

  deleteRepair(repair: Repair): void {
    if (!repair.id) return;
    this.isDeleting = true;
    this.api.deleteRepair(repair.id).subscribe({
      next: () => {
        this.isDeleting = false; this.messageService.add({ severity: 'success', summary: 'Reparación eliminada', detail: `Orden #${repair.orderNumber} eliminada.` }); this.reload(); },
      error: (error) => { this.isDeleting = false; this.messageService.add({ severity: 'error', summary: 'Error', detail: this.errorDetail(error, 'No se pudo eliminar la reparación.') }); }
    });
  }

  private errorDetail(error: any, fallback: string): string {
    if (error?.status === 403) return 'No autorizado (403). Verificá permisos/token de sesión.';
    return `${fallback} (${error?.status || 'sin código'}).`;
  }

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

  clientLabel(repair: Repair): string {
    const client = this.clientsById.get(repair.idClient);
    return client ? `${client.name} ${client.lastName}`.trim() : repair.idClient;
  }

  whatsAppLink(phone: string): string {
    const digits = (phone || "").replace(/\D/g, "");
    return `https://wa.me/${digits}`;
  }



  get clientDeviceOptions(): { label: string; value: string }[] {
    return this.clientDevices.map((d) => ({
      label: `${d.brand || '-'} - ${d.model || '-'}` ,
      value: d.id || ''
    })).filter((d) => !!d.value);
  }

  get filteredClients(): Client[] {
    const term = this.clientSearch.trim().toLowerCase();
    if (!term) return this.clients;
    return this.clients.filter((c) => `${c.name} ${c.lastName} ${c.phone} ${c.email}`.toLowerCase().includes(term));
  }
}
