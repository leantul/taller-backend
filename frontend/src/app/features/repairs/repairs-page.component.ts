import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { SelectModule } from 'primeng/select';
import { AutoCompleteModule } from 'primeng/autocomplete';
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
  imports: [CommonModule, FormsModule, CardModule, InputTextModule, ButtonModule, TableModule, SelectModule, AutoCompleteModule, InputNumberModule, TagModule, DatePickerModule, DialogModule, ConfirmDialogModule],
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
  showNewClientModal = false;
  clientSearch = '';
  selectedClientName = '';
  clientSuggestions: { label: string; value: string }[] = [];
  draft: Repair = { idDevice: '', idClient: '', orderNumber: '', description: '', status: 'POR_RECIBIR', price: 0, quotedAmount: 0, quoteNotes: '', parts: [] };
  editingRepair: Repair = { idDevice: '', idClient: '', orderNumber: '', description: '', status: 'POR_RECIBIR', price: 0, quotedAmount: 0, quoteNotes: '', parts: [] };
  draftClient: Client = { name: '', lastName: '', dni: '', email: '', phone: '' };
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
        this.draft = { idDevice: '', idClient: '', orderNumber: '', description: '', status: 'POR_RECIBIR', price: 0, quotedAmount: 0, quoteNotes: '', parts: [] };
        this.selectedClientName='';
        this.clientDevices=[];
        this.reload();
      },
      error: (error) => { this.isSaving = false; this.messageService.add({ severity: 'error', summary: 'Error', detail: this.errorDetail(error, 'No se pudo guardar la reparación.') }); }
    });
  }



  onClientInputChange(value: string): void {
    this.selectedClientName = value;
    const exactClient = this.clients.find((c) => `${c.name} ${c.lastName}`.trim().toLowerCase() === value.trim().toLowerCase());
    if (exactClient?.id) {
      this.draft.idClient = exactClient.id;
      this.api.getDevices().subscribe((devices) => this.clientDevices = devices.filter(d => d.clientId === this.draft.idClient));
      return;
    }

    this.draft.idClient = '';
    this.draft.idDevice = '';
    this.clientDevices = [];
  }

  createClientInline(): void {
    if (!this.draftClient.name?.trim() || !this.draftClient.lastName?.trim() || !this.draftClient.phone?.trim()) {
      this.messageService.add({ severity: 'warn', summary: 'Faltan datos', detail: 'Completá al menos nombre, apellido y teléfono.' });
      return;
    }

    this.api.createClient(this.draftClient).subscribe({
      next: (client) => {
        this.clients = [client, ...this.clients];
        if (client.id) this.clientsById.set(client.id, client);
        this.selectClient(client);
        this.draftClient = { name: '', lastName: '', dni: '', email: '', phone: '' };
        this.showNewClientModal = false;
      },
      error: (error) => this.messageService.add({ severity: 'error', summary: 'Error', detail: this.errorDetail(error, 'No se pudo crear el cliente.') })
    });
  }

  addPartRow(): void {
    this.editingRepair.parts = this.editingRepair.parts || [];
    this.editingRepair.parts.push({ name: '', quantity: 1, provider: '', cost: 0, salePrice: 0 });
  }

  removePartRow(index: number): void {
    this.editingRepair.parts = (this.editingRepair.parts || []).filter((_, i) => i !== index);
  }

  statusSeverity(status: Repair['status']): "success" | "info" | "warn" | "danger" | "secondary" | "contrast" {
    switch (status) {
      case 'POR_RECIBIR': return 'secondary';
      case 'RECIBIDA': return 'info';
      case 'PRESUPUESTADA_ESPERANDO_RESPUESTA': return 'warn';
      case 'HACIENDO': return 'contrast';
      case 'ESPERANDO_RETIRO': return 'success';
      case 'RETIRADA': return 'danger';
      default: return 'secondary';
    }
  }

  statusLabel(status: Repair['status']): string {
    return this.statusOptions.find((s) => s.value === status)?.label || status;
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
    this.editingRepair = {
      ...repair,
      parts: (repair.parts || []).map((part) => ({ ...part }))
    };
    this.showEditModal = true;
  }

  onEditStatusChange(): void {
    if (this.editingRepair.status === 'RETIRADA' && !this.editingRepair.returnDateTime) {
      this.editingRepair.returnDateTime = new Date().toISOString();
    }
    if (this.editingRepair.status !== 'RETIRADA') {
      this.editingRepair.returnDateTime = undefined;
    }
  }

  saveRepairChanges(): void {
    const payload: Repair = { ...this.editingRepair };
    if (payload.status !== 'RETIRADA') {
      payload.returnDateTime = undefined;
    }

    this.isUpdating = true;
    this.api.updateRepair(payload).subscribe({
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



  get clientOptions(): { label: string; value: string }[] {
    return this.clients.map((c) => ({ label: `${c.name} ${c.lastName}`.trim(), value: c.id || '' })).filter((c) => !!c.value);
  }

  get deviceOptions(): { label: string; value: string }[] {
    return this.allDevices.map((d) => ({ label: `${d.brand || '-'} - ${d.model || '-'}` , value: d.id || '' })).filter((d) => !!d.value);
  }

  get clientDeviceOptions(): { label: string; value: string }[] {
    return this.clientDevices.map((d) => ({
      label: `${d.brand || '-'} - ${d.model || '-'}` ,
      value: d.id || ''
    })).filter((d) => !!d.value);
  }

  filterClientSuggestions(query: string): void {
    const term = (query || '').trim().toLowerCase();
    if (term.length < 3) {
      this.clientSuggestions = [];
      return;
    }

    this.clientSuggestions = this.clients
      .filter((c) => `${c.name} ${c.lastName}`.toLowerCase().includes(term))
      .slice(0, 10)
      .map((c) => ({ label: `${c.name} ${c.lastName}`.trim(), value: c.id || '' }))
      .filter((c) => !!c.value);
  }

  onClientAutocompleteSelect(selection: { label: string; value: string }): void {
    const client = this.clients.find((c) => c.id === selection.value);
    if (client) this.selectClient(client);
  }

  get editReturnDate(): Date | null {
    return this.editingRepair.returnDateTime ? new Date(this.editingRepair.returnDateTime) : null;
  }

  set editReturnDate(value: Date | null) {
    this.editingRepair.returnDateTime = value ? value.toISOString() : undefined;
  }

  get filteredClients(): Client[] {
    const term = this.clientSearch.trim().toLowerCase();
    if (!term) return this.clients;
    return this.clients.filter((c) => `${c.name} ${c.lastName} ${c.phone} ${c.email}`.toLowerCase().includes(term));
  }
}
