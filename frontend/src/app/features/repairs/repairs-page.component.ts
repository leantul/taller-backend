import { ChangeDetectorRef, Component, OnInit, TrackByFunction } from '@angular/core';
import { forkJoin } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { SelectModule } from 'primeng/select';
import { AutoCompleteModule } from 'primeng/autocomplete';
import { InputNumberModule } from 'primeng/inputnumber';
import { DatePickerModule } from 'primeng/datepicker';
import { DialogModule } from 'primeng/dialog';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService } from 'primeng/api';
import { ApiService } from '../../core/services/api.service';
import { Repair } from '../../shared/models/repair.model';
import { Client } from '../../shared/models/client.model';
import { Device } from '../../shared/models/device.model';
import { MessageService } from 'primeng/api';

type RepairTableRow = {
  repair: Repair;
  orderLabel: string;
  clientLabel: string;
  deviceLabel: string;
  statusLabel: string;
  statusClass: string;
  quotedAmountLabel: string;
  clientPhone: string;
};

@Component({
  selector: 'app-repairs-page',
  standalone: true,
  imports: [CommonModule, FormsModule, CardModule, InputTextModule, ButtonModule, SelectModule, AutoCompleteModule, InputNumberModule, DatePickerModule, DialogModule, ConfirmDialogModule],
  providers: [ConfirmationService],
  templateUrl: './repairs-page.component.html'
})
export class RepairsPageComponent implements OnInit {
  private readonly statusOrder: Record<Repair['status'], number> = {
    POR_RECIBIR: 0,
    RECIBIDA: 1,
    PRESUPUESTADA_ESPERANDO_RESPUESTA: 2,
    HACIENDO: 3,
    ESPERANDO_RETIRO: 4,
    RETIRADA: 5
  };

  repairs: Repair[] = [];
  filteredRepairs: Repair[] = [];
  visibleRepairRows: RepairTableRow[] = [];
  clients: Client[] = [];
  clientsById = new Map<string, Client>();
  devicesById = new Map<string, Device>();
  clientDevices: Device[] = [];
  allDevices: Device[] = [];
  clientOptions: { label: string; value: string }[] = [];
  deviceOptions: { label: string; value: string }[] = [];
  clientDeviceOptions: { label: string; value: string }[] = [];
  filteredClientsList: Client[] = [];
  selectedClientSummary = '';
  selectedDeviceSummary = '';
  selectedDevicePassword = '';
  editingDevicePassword = '';
  showSelectedDevicePassword = false;
  showEditingDevicePassword = false;
  showDraftDevicePassword = false;
  editClientOptions: { label: string; value: string }[] = [];
  editDeviceOptions: { label: string; value: string }[] = [];
  draftDevice: Device = { brand: '', model: '', serialNumber: '', clientId: '', deviceType: 'NOTEBOOK', currentPassword: '' };
  showClientModal = false;
  showDeviceModal = false;
  showStatusModal = false;
  showEditModal = false;
  showDetailModal = false;
  showNewClientModal = false;
  clientSearch = '';
  selectedClientName = '';
  clientSuggestions: { label: string; value: string }[] = [];
  draft: Repair = { idDevice: '', idClient: '', orderNumber: '', description: '', status: 'POR_RECIBIR', price: 0, quotedAmount: 0, quoteNotes: '', parts: [], observations: [] };
  editingRepair: Repair = { idDevice: '', idClient: '', orderNumber: '', description: '', status: 'POR_RECIBIR', price: 0, quotedAmount: 0, quoteNotes: '', parts: [], observations: [] };
  detailRepair: Repair | null = null;
  draftClient: Client = { name: '', lastName: '', dni: '', email: '', phone: '' };
  statusEditingRepair: Repair | null = null;
  searchTerm = '';
  currentPage = 1;
  pageSize = 10;
  totalPages = 1;
  paginationLabel = '0 reparaciones';
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

  constructor(private readonly api: ApiService, private readonly messageService: MessageService, private readonly route: ActivatedRoute, private readonly confirmationService: ConfirmationService, private readonly changeDetector: ChangeDetectorRef) {}
  ngOnInit(): void {
    forkJoin({ repairs: this.api.getRepairs(), clients: this.api.getClients(), devices: this.api.getDevices() }).subscribe(({ repairs, clients, devices }) => {
      this.repairs = repairs.slice().reverse();
      this.clients = clients;
      this.clientsById = new Map(clients.filter(item => !!item.id).map(item => [item.id!, item]));
      this.devicesById = new Map(devices.filter(item => !!item.id).map(item => [item.id!, item]));
      this.allDevices = devices;
      this.rebuildStaticOptions();
      this.updateFilteredClients();
      this.refreshSelectionSummaries();
      this.applyFilters();
      this.changeDetector.detectChanges();
    });

    this.route.queryParamMap.subscribe((params) => {
      const q = params.get('q') || '';
      if (q !== this.searchTerm) {
        this.searchTerm = q;
        this.applyFilters();
      }
    });
  }

  selectClient(client: Client): void {
    this.draft.idClient = client.id || '';
    this.selectedClientName = `${client.name} ${client.lastName}`.trim();
    this.showClientModal = false;
    this.clientDevices = this.allDevices.filter((device) => device.clientId === this.draft.idClient);
    this.rebuildClientDeviceOptions();
    this.refreshSelectionSummaries();
    this.changeDetector.detectChanges();
  }

  createDeviceInline(): void {
    this.draftDevice.clientId = this.draft.idClient;
    this.api.createDevice(this.draftDevice).subscribe((device) => {
      this.clientDevices = [device, ...this.clientDevices];
      this.allDevices = [device, ...this.allDevices];
      if (device.id) this.devicesById.set(device.id, device);
      this.rebuildStaticOptions();
      this.rebuildClientDeviceOptions();
      this.draft.idDevice = device.id || '';
      this.draftDevice = { brand: '', model: '', serialNumber: '', clientId: this.draft.idClient, deviceType: 'NOTEBOOK', currentPassword: '' };
      this.showDeviceModal = false;
      this.showDraftDevicePassword = false;
      this.refreshSelectionSummaries();
      this.changeDetector.detectChanges();
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
      this.draft = { idDevice: '', idClient: '', orderNumber: '', description: '', status: 'POR_RECIBIR', price: 0, quotedAmount: 0, quoteNotes: '', parts: [], observations: [] };
        this.selectedClientName='';
        this.clientDevices=[];
        this.rebuildClientDeviceOptions();
        this.refreshSelectionSummaries();
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
      this.clientDevices = this.allDevices.filter((device) => device.clientId === this.draft.idClient);
      this.rebuildClientDeviceOptions();
      this.refreshSelectionSummaries();
      this.changeDetector.detectChanges();
      return;
    }

    this.draft.idClient = '';
    this.draft.idDevice = '';
    this.clientDevices = [];
    this.rebuildClientDeviceOptions();
    this.refreshSelectionSummaries();
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
        this.rebuildStaticOptions();
        this.updateFilteredClients();
        this.selectClient(client);
        this.draftClient = { name: '', lastName: '', dni: '', email: '', phone: '' };
        this.showNewClientModal = false;
        this.changeDetector.detectChanges();
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

  addDraftObservationRow(): void {
    this.draft.observations = this.draft.observations || [];
    this.draft.observations.push({ note: '' });
  }

  removeDraftObservationRow(index: number): void {
    this.draft.observations = (this.draft.observations || []).filter((_, i) => i !== index);
  }

  addObservationRow(): void {
    this.editingRepair.observations = this.editingRepair.observations || [];
    this.editingRepair.observations.push({ note: '' });
  }

  removeObservationRow(index: number): void {
    this.editingRepair.observations = (this.editingRepair.observations || []).filter((_, i) => i !== index);
  }

  statusLabel(status: Repair['status']): string {
    return this.statusOptions.find((s) => s.value === status)?.label || status;
  }

  formatDateTime(value?: string): string {
    if (!value) return '-';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return '-';
    return new Intl.DateTimeFormat('es-AR', {
      dateStyle: 'short',
      timeStyle: 'short'
    }).format(date);
  }

  onRowActionClick(event: Event): void {
    event.stopPropagation();
  }

  openWhatsApp(event: Event, phone: string): void {
    event.stopPropagation();
    event.preventDefault();
    const url = this.whatsAppLink(phone);
    if (!url) {
      return;
    }
    window.open(url, '_blank', 'noopener');
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
    if (!repair.id) return;
    this.showEditModal = false;
    this.showEditingDevicePassword = false;
    this.isUpdating = true;
    this.api.getRepairById(repair.id).subscribe({
      next: (detail) => {
        this.isUpdating = false;
        this.editingRepair = {
          ...detail,
          parts: (detail.parts || []).map((part) => ({ ...part })),
          observations: (detail.observations || []).map((observation) => ({ ...observation }))
        };
        this.editClientOptions = [...this.clientOptions];
        this.editDeviceOptions = [...this.deviceOptions];
        this.refreshSelectionSummaries();
        this.refreshEditingDevicePassword();
        this.changeDetector.detectChanges();
        queueMicrotask(() => {
          this.showEditModal = true;
          this.changeDetector.detectChanges();
        });
      },
      error: (error) => {
        this.isUpdating = false;
        this.messageService.add({ severity: 'error', summary: 'Error', detail: this.errorDetail(error, 'No se pudo cargar el detalle de la reparación.') });
      }
    });
  }

  openDetailModal(repair: Repair): void {
    if (!repair.id) return;
    this.isUpdating = true;
    this.api.getRepairById(repair.id).subscribe({
      next: (detail) => {
        this.isUpdating = false;
        this.detailRepair = {
          ...detail,
          parts: (detail.parts || []).map((part) => ({ ...part })),
          observations: (detail.observations || []).map((observation) => ({ ...observation }))
        };
        this.showDetailModal = true;
        this.changeDetector.detectChanges();
      },
      error: (error) => {
        this.isUpdating = false;
        this.messageService.add({ severity: 'error', summary: 'Error', detail: this.errorDetail(error, 'No se pudo cargar el detalle de la reparación.') });
      }
    });
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
    this.filteredRepairs = this.repairs
      .filter((r) => {
        const matchesTerm = !term || [r.idClient, r.idDevice, this.clientLabel(r), this.deviceLabel(r), r.orderNumber, r.description].filter(Boolean).join(' ').toLowerCase().includes(term);
        const receive = r.receiveDateTime ? new Date(r.receiveDateTime) : null;
        const matchesFrom = !this.fromDate || (receive && receive >= this.fromDate);
        const matchesTo = !this.toDate || (receive && receive <= this.toDate);
        return Boolean(matchesTerm && matchesFrom && matchesTo);
      })
      .sort((left, right) => this.compareRepairs(left, right));

    this.currentPage = 1;
    this.updateVisibleRepairs();
  }
  private reload(): void { this.api.getRepairs().subscribe((repairs) => { this.repairs = repairs.slice().reverse(); this.applyFilters(); this.changeDetector.detectChanges(); }); }

  clientLabel(repair: Repair): string {
    const client = this.clientsById.get(repair.idClient);
    return client ? `${client.name} ${client.lastName}`.trim() : repair.idClient;
  }

  clientPhone(repair: Repair): string {
    return this.clientsById.get(repair.idClient)?.phone || '';
  }

  deviceLabel(repair: Repair): string {
    const device = this.devicesById.get(repair.idDevice);
    if (!device) return repair.idDevice || '-';
    return `${device.brand || '-'} - ${device.model || '-'}`.replace(/\s+/g, ' ').trim();
  }

  statusClass(status: Repair['status']): string {
    switch (status) {
      case 'POR_RECIBIR': return 'is-muted';
      case 'RECIBIDA': return 'is-info';
      case 'PRESUPUESTADA_ESPERANDO_RESPUESTA': return 'is-warning';
      case 'HACIENDO': return 'is-active';
      case 'ESPERANDO_RETIRO': return 'is-success';
      case 'RETIRADA': return 'is-closed';
      default: return 'is-muted';
    }
  }

  repairTrackBy: TrackByFunction<RepairTableRow> = (index, row) =>
    row.repair.id || row.repair.orderNumber || `${row.repair.idClient}-${row.repair.idDevice}-${row.repair.receiveDateTime || index}`;

  previousPage(): void {
    this.currentPage = Math.max(1, this.currentPage - 1);
    this.updateVisibleRepairs();
  }

  nextPage(): void {
    this.currentPage = Math.min(this.totalPages, this.currentPage + 1);
    this.updateVisibleRepairs();
  }

  whatsAppLink(phone: string): string {
    const digits = (phone || "").replace(/\D/g, "");
    return digits ? `https://wa.me/${digits}` : '';
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

  get editReturnDateTimeLocal(): string {
    if (!this.editingRepair.returnDateTime) {
      return '';
    }
    const date = new Date(this.editingRepair.returnDateTime);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}`;
  }

  set editReturnDateTimeLocal(value: string) {
    this.editingRepair.returnDateTime = value ? new Date(value).toISOString() : undefined;
  }

  get filteredClients(): Client[] {
    return this.filteredClientsList;
  }

  private updateVisibleRepairs(): void {
    this.totalPages = Math.max(1, Math.ceil(this.filteredRepairs.length / this.pageSize));
    const page = Math.min(this.currentPage, this.totalPages);
    const start = (page - 1) * this.pageSize;
    this.currentPage = page;
    this.visibleRepairRows = this.filteredRepairs
      .slice(start, start + this.pageSize)
      .map((repair) => this.toTableRow(repair));
    if (!this.filteredRepairs.length) {
      this.paginationLabel = '0 reparaciones';
    } else {
      const end = Math.min(start + this.pageSize, this.filteredRepairs.length);
      this.paginationLabel = `${start + 1}-${end} de ${this.filteredRepairs.length} reparaciones`;
    }
    this.changeDetector.detectChanges();
  }

  private compareRepairs(left: Repair, right: Repair): number {
    const statusDiff = this.statusOrder[left.status] - this.statusOrder[right.status];
    if (statusDiff !== 0) {
      return statusDiff;
    }

    const rightDate = this.sortTimestamp(right);
    const leftDate = this.sortTimestamp(left);
    if (rightDate !== leftDate) {
      return rightDate - leftDate;
    }

    return (right.orderNumber || '').localeCompare(left.orderNumber || '', undefined, { numeric: true, sensitivity: 'base' });
  }

  private sortTimestamp(repair: Repair): number {
    const raw = repair.receiveDateTime || repair.returnDateTime;
    return raw ? new Date(raw).getTime() : 0;
  }

  private toTableRow(repair: Repair): RepairTableRow {
    return {
      repair,
      orderLabel: `#${repair.orderNumber || '-'}`,
      clientLabel: this.clientLabel(repair),
      deviceLabel: this.deviceLabel(repair),
      statusLabel: this.statusLabel(repair.status),
      statusClass: this.statusClass(repair.status),
      quotedAmountLabel: this.formatMoney(repair.quotedAmount),
      clientPhone: this.clientPhone(repair)
    };
  }

  onDraftDeviceChange(): void {
    this.refreshSelectionSummaries();
  }

  onEditRepairClientChange(): void {
    if (!this.editClientOptions.some((option) => option.value === this.editingRepair.idClient)) {
      this.editingRepair.idClient = '';
    }
  }

  onEditRepairDeviceChange(): void {
    if (!this.editDeviceOptions.some((option) => option.value === this.editingRepair.idDevice)) {
      this.editingRepair.idDevice = '';
    }
    this.refreshEditingDevicePassword();
  }

  updateFilteredClients(): void {
    const term = this.clientSearch.trim().toLowerCase();
    this.filteredClientsList = !term
      ? this.clients
      : this.clients.filter((c) => `${c.name} ${c.lastName} ${c.phone} ${c.email}`.toLowerCase().includes(term));
  }

  private rebuildStaticOptions(): void {
    this.clientOptions = this.clients
      .map((c) => ({ label: `${c.name} ${c.lastName}`.trim(), value: c.id || '' }))
      .filter((c) => !!c.value);
    this.deviceOptions = this.allDevices
      .map((d) => ({ label: `${d.brand || '-'} - ${d.model || '-'}`, value: d.id || '' }))
      .filter((d) => !!d.value);
  }

  private rebuildClientDeviceOptions(): void {
    this.clientDeviceOptions = this.clientDevices
      .map((d) => ({ label: `${d.brand || '-'} - ${d.model || '-'}`, value: d.id || '' }))
      .filter((d) => !!d.value);
  }

  private refreshSelectionSummaries(): void {
    const client = this.clientsById.get(this.draft.idClient);
    this.selectedClientSummary = client ? `${client.name} ${client.lastName}`.trim() : '';
    const device = this.clientDevices.find((item) => item.id === this.draft.idDevice) || this.devicesById.get(this.draft.idDevice);
    this.selectedDeviceSummary = device ? `${device.deviceType} · ${device.brand} ${device.model}`.replace(/\s+/g, ' ').trim() : '';
    this.selectedDevicePassword = device?.currentPassword || '';
  }

  private refreshEditingDevicePassword(): void {
    const device = this.allDevices.find((item) => item.id === this.editingRepair.idDevice) || this.devicesById.get(this.editingRepair.idDevice);
    this.editingDevicePassword = device?.currentPassword || '';
  }

  private formatMoney(value: unknown): string {
    return new Intl.NumberFormat('es-AR', {
      style: 'currency',
      currency: 'ARS',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(this.asMoney(value));
  }

  private asMoney(value: unknown): number {
    if (typeof value === 'number') {
      return Number.isFinite(value) ? value : 0;
    }

    if (typeof value === 'string') {
      const sanitized = value.trim().replace(/\s+/g, '').replace(/[^0-9,.-]/g, '');
      if (!sanitized) {
        return 0;
      }

      let normalized = sanitized;
      if (sanitized.includes(',') && sanitized.includes('.')) {
        normalized = sanitized.lastIndexOf(',') > sanitized.lastIndexOf('.')
          ? sanitized.replace(/\./g, '').replace(',', '.')
          : sanitized.replace(/,/g, '');
      } else if (sanitized.includes(',')) {
        normalized = sanitized.replace(/\./g, '').replace(',', '.');
      }

      const parsed = Number(normalized);
      return Number.isFinite(parsed) ? parsed : 0;
    }

    if (value && typeof value === 'object') {
      const nestedValue = (value as { amount?: unknown; value?: unknown }).amount ?? (value as { value?: unknown }).value;
      return this.asMoney(nestedValue);
    }

    return 0;
  }
}
