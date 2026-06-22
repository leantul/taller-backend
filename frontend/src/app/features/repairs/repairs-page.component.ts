import { ChangeDetectorRef, Component, OnInit, TrackByFunction, ViewChild } from '@angular/core';
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
import { Device, DeviceType } from '../../shared/models/device.model';
import { MessageService } from 'primeng/api';
import { RepairDetailDialogComponent } from '../../shared/components/repair-detail-dialog.component';
import { DeliveryReportDialogComponent } from '../../shared/components/delivery-report-dialog.component';
import { fromDateTimeLocal, REPAIR_STATUS_OPTIONS, repairStatusClass, repairStatusLabel, toDateTimeLocal } from '../../shared/utils/repair-status.util';

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

type RepairTableColumnKey = 'orderLabel' | 'clientLabel' | 'deviceLabel' | 'statusLabel' | 'quotedAmountValue' | 'actions';

type RepairTableColumn = {
  key: RepairTableColumnKey;
  label: string;
  width: string;
  sortable: boolean;
};

@Component({
  selector: 'app-repairs-page',
  standalone: true,
  imports: [CommonModule, FormsModule, CardModule, InputTextModule, ButtonModule, SelectModule, AutoCompleteModule, InputNumberModule, DatePickerModule, DialogModule, ConfirmDialogModule, RepairDetailDialogComponent, DeliveryReportDialogComponent],
  providers: [ConfirmationService],
  templateUrl: './repairs-page.component.html'
})
export class RepairsPageComponent implements OnInit {
  @ViewChild(RepairDetailDialogComponent) private repairDetailDialog?: RepairDetailDialogComponent;
  @ViewChild(DeliveryReportDialogComponent) private deliveryReportDialog?: DeliveryReportDialogComponent;
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
  draftDevice: Device = { brand: '', model: '', serialNumber: '', clientId: '', deviceTypeId: '', currentPassword: '' };
  showClientModal = false;
  showDeviceModal = false;
  showStatusModal = false;
  showEditModal = false;
  showNewClientModal = false;
  clientSearch = '';
  selectedClientName = '';
  clientSuggestions: { label: string; value: string }[] = [];
  draft: Repair = { idDevice: '', idClient: '', orderNumber: '', description: '', status: 'POR_RECIBIR', price: 0, quotedAmount: 0, quoteNotes: '', parts: [], observations: [] };
  editingRepair: Repair = { idDevice: '', idClient: '', orderNumber: '', description: '', status: 'POR_RECIBIR', price: 0, quotedAmount: 0, quoteNotes: '', parts: [], observations: [] };
  draftClient: Client = { name: '', lastName: '', dni: '', email: '', phone: '' };
  statusEditingRepair: Repair | null = null;
  searchTerm = '';
  currentPage = 1;
  pageSize = 10;
  totalPages = 1;
  totalElements = 0;
  paginationLabel = '0 reparaciones';
  isSaving = false;
  isUpdating = false;
  isDeleting = false;
  fromDate: Date | null = null;
  toDate: Date | null = null;
  statusOptions = [...REPAIR_STATUS_OPTIONS];
  typeOptions: DeviceType[] = [];
  readonly repairColumns: RepairTableColumn[] = [
    { key: 'orderLabel', label: 'Orden', width: '9rem', sortable: true },
    { key: 'clientLabel', label: 'Cliente', width: '16rem', sortable: true },
    { key: 'deviceLabel', label: 'Dispositivo', width: '16rem', sortable: true },
    { key: 'statusLabel', label: 'Estado', width: '12rem', sortable: true },
    { key: 'quotedAmountValue', label: 'Presupuesto', width: '11rem', sortable: true },
    { key: 'actions', label: 'Acción', width: '14rem', sortable: false }
  ];
  sortColumn: Exclude<RepairTableColumnKey, 'actions'> = 'orderLabel';
  sortDirection: 'asc' | 'desc' = 'desc';
  private resizingColumnKey: RepairTableColumnKey | null = null;
  private resizeStartX = 0;
  private resizeStartWidth = 0;

  constructor(private readonly api: ApiService, private readonly messageService: MessageService, private readonly route: ActivatedRoute, private readonly confirmationService: ConfirmationService, private readonly changeDetector: ChangeDetectorRef) {}
  ngOnInit(): void {
    forkJoin({ clients: this.api.getClients(), devices: this.api.getDevices(), deviceTypes: this.api.getDeviceTypes() }).subscribe(({ clients, devices, deviceTypes }) => {
      this.clients = clients;
      this.clientsById = new Map(clients.filter(item => !!item.id).map(item => [item.id!, item]));
      this.devicesById = new Map(devices.filter(item => !!item.id).map(item => [item.id!, item]));
      this.allDevices = devices;
      this.typeOptions = deviceTypes;
      this.draftDevice.deviceTypeId = this.defaultDeviceTypeId();
      this.rebuildStaticOptions();
      this.updateFilteredClients();
      this.refreshSelectionSummaries();
      this.reload();
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
      this.draftDevice = { brand: '', model: '', serialNumber: '', clientId: this.draft.idClient, deviceTypeId: this.defaultDeviceTypeId(), currentPassword: '' };
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
    return repairStatusLabel(status);
  }

  onRowActionClick(event: Event): void {
    event.stopPropagation();
  }

  onDraftStatusChange(): void {
    if (this.draft.status === 'RECIBIDA' && !this.draft.receiveDateTime) {
      this.draft.receiveDateTime = fromDateTimeLocal(toDateTimeLocal());
      return;
    }
    if (this.draft.status !== 'RECIBIDA') this.draft.receiveDateTime = undefined;
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
    if (!this.statusEditingRepair?.id) return;
    this.isUpdating = true;
    this.api.updateRepairStatus(this.statusEditingRepair.id, this.statusEditingRepair.status).subscribe({
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
    this.repairDetailDialog?.open(repair.id, this.clientLabel(repair), this.deviceLabel(repair));
  }

  openDeliveryReport(event: Event, repair: Repair): void {
    event.stopPropagation();
    event.preventDefault();
    this.deliveryReportDialog?.open(repair);
  }

  onEditStatusChange(): void {
    if (this.editingRepair.status === 'RETIRADA' && !this.editingRepair.returnDateTime) {
      this.editingRepair.returnDateTime = fromDateTimeLocal(toDateTimeLocal());
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
    this.currentPage = 1;
    this.reload();
  }
  private reload(): void {
    this.api.getRepairPage(
      this.currentPage - 1,
      this.pageSize,
      this.searchTerm.trim(),
      this.toApiDateTime(this.fromDate),
      this.toApiDateTime(this.toDate, true)
    ).subscribe((page) => {
      this.repairs = page.content;
      this.filteredRepairs = page.content;
      this.currentPage = page.page + 1;
      this.totalElements = page.totalElements;
      this.totalPages = Math.max(1, page.totalPages);
      this.updateVisibleRepairs();
      this.changeDetector.detectChanges();
    });
  }

  clientLabel(repair: Repair): string {
    if (repair.clientName) return repair.clientName;
    const client = this.clientsById.get(repair.idClient);
    return client ? `${client.name} ${client.lastName}`.trim() : repair.idClient;
  }

  clientPhone(repair: Repair): string {
    if (repair.clientPhone) return repair.clientPhone;
    return this.clientsById.get(repair.idClient)?.phone || '';
  }

  deviceLabel(repair: Repair): string {
    if (repair.deviceLabel) return repair.deviceLabel;
    const device = this.devicesById.get(repair.idDevice);
    if (!device) return repair.idDevice || '-';
    return `${device.brand || '-'} - ${device.model || '-'}`.replace(/\s+/g, ' ').trim();
  }

  statusClass(status: Repair['status']): string {
    return repairStatusClass(status);
  }

  repairTrackBy: TrackByFunction<RepairTableRow> = (index, row) =>
    row.repair.id || row.repair.orderNumber || `${row.repair.idClient}-${row.repair.idDevice}-${row.repair.receiveDateTime || index}`;

  previousPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.reload();
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.reload();
    }
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

  get draftReceiveDateTimeLocal(): string {
    return toDateTimeLocal(this.draft.receiveDateTime);
  }

  set draftReceiveDateTimeLocal(value: string) {
    this.draft.receiveDateTime = fromDateTimeLocal(value);
  }

  get editReturnDateTimeLocal(): string {
    return this.editingRepair.returnDateTime ? toDateTimeLocal(this.editingRepair.returnDateTime) : '';
  }

  set editReturnDateTimeLocal(value: string) {
    this.editingRepair.returnDateTime = fromDateTimeLocal(value);
  }

  get filteredClients(): Client[] {
    return this.filteredClientsList;
  }

  private updateVisibleRepairs(): void {
    const start = (this.currentPage - 1) * this.pageSize;
    this.visibleRepairRows = this.sortRows(this.repairs.map((repair) => this.toTableRow(repair)));
    if (!this.totalElements) {
      this.paginationLabel = '0 reparaciones';
    } else {
      const end = Math.min(start + this.repairs.length, this.totalElements);
      this.paginationLabel = `${start + 1}-${end} de ${this.totalElements} reparaciones`;
    }
    this.changeDetector.detectChanges();
  }

  private toApiDateTime(date: Date | null, endOfDay = false): string | undefined {
    if (!date) return undefined;
    const value = new Date(date);
    if (endOfDay) {
      value.setHours(23, 59, 59, 999);
    } else {
      value.setHours(0, 0, 0, 0);
    }
    const pad = (input: number) => String(input).padStart(2, '0');
    return `${value.getFullYear()}-${pad(value.getMonth() + 1)}-${pad(value.getDate())}T${pad(value.getHours())}:${pad(value.getMinutes())}:${pad(value.getSeconds())}`;
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

  sortByColumn(column: RepairTableColumnKey): void {
    if (column === 'actions') {
      return;
    }
    if (this.sortColumn === column) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortColumn = column;
      this.sortDirection = column === 'orderLabel' || column === 'quotedAmountValue' ? 'desc' : 'asc';
    }
    this.visibleRepairRows = this.sortRows([...this.visibleRepairRows]);
  }

  sortIcon(column: RepairTableColumnKey): string {
    if (column === 'actions') {
      return 'pi pi-sort-alt';
    }
    if (this.sortColumn !== column) {
      return 'pi pi-sort-alt';
    }
    return this.sortDirection === 'asc' ? 'pi pi-sort-amount-up-alt' : 'pi pi-sort-amount-down';
  }

  columnWidth(columnKey: RepairTableColumnKey): string {
    return this.repairColumns.find((column) => column.key === columnKey)?.width || 'auto';
  }

  startColumnResize(event: MouseEvent, columnKey: RepairTableColumnKey): void {
    event.preventDefault();
    event.stopPropagation();
    const header = (event.currentTarget as HTMLElement).closest('th');
    if (!header) {
      return;
    }

    this.resizingColumnKey = columnKey;
    this.resizeStartX = event.clientX;
    this.resizeStartWidth = header.getBoundingClientRect().width;

    const onMouseMove = (moveEvent: MouseEvent) => {
      if (!this.resizingColumnKey) {
        return;
      }
      const nextWidth = Math.max(96, Math.round(this.resizeStartWidth + (moveEvent.clientX - this.resizeStartX)));
      const column = this.repairColumns.find((item) => item.key === this.resizingColumnKey);
      if (column) {
        column.width = `${nextWidth}px`;
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

  private defaultDeviceTypeId(): string {
    return this.typeOptions.find((type) => type.name.toLowerCase() === 'notebook')?.id
      || this.typeOptions[0]?.id
      || '';
  }

  private refreshSelectionSummaries(): void {
    const client = this.clientsById.get(this.draft.idClient);
    this.selectedClientSummary = client ? `${client.name} ${client.lastName}`.trim() : '';
    const device = this.clientDevices.find((item) => item.id === this.draft.idDevice) || this.devicesById.get(this.draft.idDevice);
    this.selectedDeviceSummary = device ? `${device.deviceTypeName || '-'} · ${device.brand} ${device.model}`.replace(/\s+/g, ' ').trim() : '';
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

  private sortRows(rows: RepairTableRow[]): RepairTableRow[] {
    return rows.sort((left, right) => {
      const direction = this.sortDirection === 'asc' ? 1 : -1;
      return this.compareRows(left, right) * direction;
    });
  }

  private compareRows(left: RepairTableRow, right: RepairTableRow): number {
    switch (this.sortColumn) {
      case 'orderLabel':
        return this.numericCompare(left.repair.orderNumber, right.repair.orderNumber);
      case 'quotedAmountValue':
        return this.asMoney(left.repair.quotedAmount) - this.asMoney(right.repair.quotedAmount);
      case 'clientLabel':
        return left.clientLabel.localeCompare(right.clientLabel, 'es', { sensitivity: 'base' });
      case 'deviceLabel':
        return left.deviceLabel.localeCompare(right.deviceLabel, 'es', { sensitivity: 'base' });
      case 'statusLabel':
        return left.statusLabel.localeCompare(right.statusLabel, 'es', { sensitivity: 'base' });
      default:
        return 0;
    }
  }

  private numericCompare(left: unknown, right: unknown): number {
    const leftValue = Number(left || 0);
    const rightValue = Number(right || 0);
    if (Number.isFinite(leftValue) && Number.isFinite(rightValue)) {
      return leftValue - rightValue;
    }
    return String(left || '').localeCompare(String(right || ''), 'es', { numeric: true, sensitivity: 'base' });
  }
}
