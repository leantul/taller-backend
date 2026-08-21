import { ChangeDetectorRef, Component, OnDestroy, OnInit, TrackByFunction, ViewChild } from '@angular/core';
import { catchError, debounceTime, distinctUntilChanged, finalize, map, of, Subject, Subscription, switchMap, timer } from 'rxjs';
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
import { beginColumnResize, persistColumnWidths, resolveColumnWidth, restoreColumnWidths } from '../../shared/utils/resizable-columns.util';
import { phoneDigits } from '../../shared/utils/contact.util';

type RepairTableRow = {
  repair: Repair;
  orderLabel: string;
  clientLabel: string;
  deviceLabel: string;
  statusLabel: string;
  statusClass: string;
  finalAmountLabel: string;
  clientPhone: string;
};

type RepairTableColumnKey = 'orderLabel' | 'clientLabel' | 'deviceLabel' | 'statusLabel' | 'finalAmountValue' | 'actions';
type RepairSortField = 'orderNumber' | 'clientName' | 'deviceLabel' | 'status' | 'price';
type RepairStatusFilter = Repair['status'] | '';
type ClientSearchTarget = 'create' | 'modal' | 'edit';

type ClientOption = {
  label: string;
  value: string;
  client: Client;
};

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
export class RepairsPageComponent implements OnInit, OnDestroy {
  @ViewChild(RepairDetailDialogComponent) private repairDetailDialog?: RepairDetailDialogComponent;
  @ViewChild(DeliveryReportDialogComponent) private deliveryReportDialog?: DeliveryReportDialogComponent;
  repairs: Repair[] = [];
  filteredRepairs: Repair[] = [];
  visibleRepairRows: RepairTableRow[] = [];
  devicesById = new Map<string, Device>();
  clientDevices: Device[] = [];
  allDevices: Device[] = [];
  deviceOptions: { label: string; value: string }[] = [];
  clientDeviceOptions: { label: string; value: string }[] = [];
  filteredClientsList: Client[] = [];
  selectedClient: Client | null = null;
  selectedClientSummary = '';
  selectedDeviceSummary = '';
  selectedDevicePassword = '';
  editingDevicePassword = '';
  showSelectedDevicePassword = false;
  showEditingDevicePassword = false;
  showDraftDevicePassword = false;
  editingClientName = '';
  editingSelectedClientLabel = '';
  editClientSuggestions: ClientOption[] = [];
  editDeviceOptions: { label: string; value: string }[] = [];
  draftDevice: Device = { brand: '', model: '', serialNumber: '', clientId: '', deviceTypeId: '', currentPassword: '' };
  showClientModal = false;
  showDeviceModal = false;
  showStatusModal = false;
  showEditModal = false;
  showNewClientModal = false;
  clientSearch = '';
  selectedClientName = '';
  clientSuggestions: ClientOption[] = [];
  isClientAutocompleteLoading = false;
  isClientModalLoading = false;
  isEditClientLoading = false;
  clientModalSearchFailed = false;
  draft: Repair = { idDevice: '', idClient: '', orderNumber: '', description: '', status: 'POR_RECIBIR', price: 0, laborAmount: null, quotedAmount: 0, quoteNotes: '', repairNotes: '', parts: [], observations: [] };
  editingRepair: Repair = { idDevice: '', idClient: '', orderNumber: '', description: '', status: 'POR_RECIBIR', price: 0, laborAmount: null, quotedAmount: 0, quoteNotes: '', parts: [], observations: [] };
  draftClient: Client = { name: '', lastName: '', reference: '', email: '', phone: '' };
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
  selectedStatusFilter: RepairStatusFilter = '';
  statusOptions = [...REPAIR_STATUS_OPTIONS];
  statusFilterOptions: { label: string; value: RepairStatusFilter }[] = [
    { label: 'Todas', value: '' },
    ...REPAIR_STATUS_OPTIONS
  ];
  typeOptions: DeviceType[] = [];
  showCreatePanel = true;
  readonly repairColumns: RepairTableColumn[] = [
    { key: 'orderLabel', label: 'Orden', width: '9rem', sortable: true },
    { key: 'clientLabel', label: 'Cliente', width: '16rem', sortable: true },
    { key: 'deviceLabel', label: 'Dispositivo', width: '16rem', sortable: true },
    { key: 'statusLabel', label: 'Estado', width: '12rem', sortable: true },
    { key: 'finalAmountValue', label: 'Monto final', width: '11rem', sortable: true },
    { key: 'actions', label: 'Acción', width: '14rem', sortable: false }
  ];
  sortColumn: Exclude<RepairTableColumnKey, 'actions'> | null = null;
  sortDirection: 'asc' | 'desc' = 'desc';
  private readonly columnWidthStorageKey = 'taller.repairs.columnWidths';
  private pageRequest?: Subscription;
  private readonly searchChanges = new Subject<string>();
  private readonly clientSearchChanges = new Subject<{ target: ClientSearchTarget; term: string }>();
  private searchSubscription?: Subscription;
  private clientSearchSubscription?: Subscription;

  constructor(private readonly api: ApiService, private readonly messageService: MessageService, private readonly route: ActivatedRoute, private readonly confirmationService: ConfirmationService, private readonly changeDetector: ChangeDetectorRef) {}
  ngOnInit(): void {
    this.restoreColumnWidths();
    this.searchSubscription = this.searchChanges.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(() => this.applyFilters());
    this.clientSearchSubscription = this.clientSearchChanges.pipe(
      switchMap((request) => {
        const term = request.term.trim();
        if (term.length < 2) {
          return of({ request, clients: [] as Client[], failed: false });
        }
        this.setClientSearchLoading(request.target, true);
        return timer(300).pipe(
          switchMap(() => this.api.searchClients(term, 20)),
          map((clients) => ({ request, clients, failed: false })),
          catchError(() => of({ request, clients: [] as Client[], failed: true })),
          finalize(() => this.setClientSearchLoading(request.target, false))
        );
      })
    ).subscribe(({ request, clients, failed }) => {
      this.setClientSearchResults(request.target, clients, failed);
      this.changeDetector.detectChanges();
    });
    this.reload();
    this.api.getDeviceTypes().subscribe((deviceTypes) => {
      this.typeOptions = deviceTypes;
      this.draftDevice.deviceTypeId = this.defaultDeviceTypeId();
      this.refreshSelectionSummaries();
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

  ngOnDestroy(): void {
    this.pageRequest?.unsubscribe();
    this.searchSubscription?.unsubscribe();
    this.clientSearchSubscription?.unsubscribe();
  }

  onSearchTermChange(): void {
    this.searchChanges.next(this.searchTerm.trim());
  }

  selectClient(client: Client): void {
    this.selectedClient = client;
    this.draft.idClient = client.id || '';
    this.selectedClientName = this.clientOptionLabel(client);
    this.showClientModal = false;
    this.draft.idDevice = '';
    this.loadClientDevices(this.draft.idClient);
  }

  createDeviceInline(): void {
    this.draftDevice.clientId = this.draft.idClient;
    this.api.createDevice(this.draftDevice).subscribe((device) => {
      this.clientDevices = [device, ...this.clientDevices];
      this.allDevices = [device, ...this.allDevices.filter((item) => item.id !== device.id)];
      if (device.id) this.devicesById.set(device.id, device);
      this.rebuildDeviceOptions();
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
    if (this.draft.laborAmount == null) {
      this.showLaborAmountRequiredMessage();
      return;
    }
    const payload = { ...this.draft, orderNumber: this.draft.orderNumber || '' };
    this.isSaving = true;
    this.api.createRepair(payload).subscribe({
      next: () => {
        this.isSaving = false;
        this.messageService.add({ severity: 'success', summary: 'Reparación guardada', detail: 'Alta creada correctamente.' });
        this.draft = { idDevice: '', idClient: '', orderNumber: '', description: '', status: 'POR_RECIBIR', price: 0, laborAmount: null, quotedAmount: 0, quoteNotes: '', repairNotes: '', parts: [], observations: [] };
        this.selectedClient = null;
        this.selectedClientName = '';
        this.clientDevices = [];
        this.rebuildClientDeviceOptions();
        this.refreshSelectionSummaries();
        this.reload();
      },
      error: (error) => { this.isSaving = false; this.messageService.add({ severity: 'error', summary: 'Error', detail: this.errorDetail(error, 'No se pudo guardar la reparación.') }); }
    });
  }



  onClientInputChange(value: string | ClientOption): void {
    if (typeof value !== 'string') return;
    this.selectedClientName = value;
    if (this.selectedClient && this.clientOptionLabel(this.selectedClient) === value.trim()) return;

    this.selectedClient = null;
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
        this.selectClient(client);
        this.draftClient = { name: '', lastName: '', reference: '', email: '', phone: '' };
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
    this.onStatusModalStatusChange();
    this.showStatusModal = true;
  }

  onStatusModalStatusChange(): void {
    if (!this.statusEditingRepair) return;
    if (this.statusEditingRepair.status === 'RECIBIDA' && !this.statusEditingRepair.receiveDateTime) {
      this.statusEditingRepair.receiveDateTime = fromDateTimeLocal(toDateTimeLocal());
    }
    if (this.statusEditingRepair.status === 'RETIRADA' && !this.statusEditingRepair.returnDateTime) {
      this.statusEditingRepair.returnDateTime = fromDateTimeLocal(toDateTimeLocal());
    }
    if (this.statusEditingRepair.status !== 'RETIRADA') {
      this.statusEditingRepair.returnDateTime = undefined;
    }
  }

  saveStatus(): void {
    if (!this.statusEditingRepair?.id) return;
    this.isUpdating = true;
    this.api.updateRepairStatus(this.statusEditingRepair.id, {
      status: this.statusEditingRepair.status,
      receiveDateTime: this.statusEditingRepair.status === 'RECIBIDA' ? this.statusEditingRepair.receiveDateTime : undefined,
      returnDateTime: this.statusEditingRepair.status === 'RETIRADA' ? this.statusEditingRepair.returnDateTime : undefined
    }).subscribe({
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
        this.editingClientName = repair.clientName || detail.idClient;
        this.editingSelectedClientLabel = this.editingClientName;
        this.editClientSuggestions = [];
        this.loadEditDevices(detail.idClient, () => {
          queueMicrotask(() => {
            this.showEditModal = true;
            this.changeDetector.detectChanges();
          });
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

  canOpenDeliveryReport(repair: Repair): boolean {
    return repair.status === 'ESPERANDO_RETIRO' || repair.status === 'RETIRADA';
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
    if (!this.editingRepair.idClient || !this.editingRepair.idDevice) {
      this.messageService.add({ severity: 'warn', summary: 'Faltan datos', detail: 'Seleccioná un cliente y uno de sus dispositivos.' });
      return;
    }
    if (this.editingRepair.laborAmount == null) {
      this.showLaborAmountRequiredMessage();
      return;
    }
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
    if (error?.error?.error) return error.error.error;
    return `${fallback} (${error?.status || 'sin código'}).`;
  }

  private showLaborAmountRequiredMessage(): void {
    this.messageService.add({
      severity: 'warn',
      summary: 'Falta mano de obra',
      detail: 'Completá la mano de obra. Si no corresponde, ingresá $0'
    });
  }

  applyFilters(): void {
    this.currentPage = 1;
    this.reload();
  }
  private reload(): void {
    this.pageRequest?.unsubscribe();
    this.pageRequest = this.api.getRepairPage(
      this.currentPage - 1,
      this.pageSize,
      this.searchTerm.trim(),
      this.toApiDateTime(this.fromDate),
      this.toApiDateTime(this.toDate, true),
      this.selectedStatusFilter,
      this.currentSortField(),
      this.sortDirection
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
    return repair.clientName || repair.idClient;
  }

  clientPhone(repair: Repair): string {
    return repair.clientPhone || '';
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
    const digits = phoneDigits(phone);
    return digits ? `https://wa.me/${digits}` : '';
  }
  filterClientSuggestions(query: string): void {
    this.requestClientSearch('create', query);
  }

  onClientAutocompleteSelect(selection: ClientOption): void {
    this.selectClient(selection.client);
  }

  get draftReceiveDateTimeLocal(): string {
    return toDateTimeLocal(this.draft.receiveDateTime);
  }

  set draftReceiveDateTimeLocal(value: string) {
    this.draft.receiveDateTime = fromDateTimeLocal(value);
  }

  get statusReceiveDateTimeLocal(): string {
    return this.statusEditingRepair?.receiveDateTime ? toDateTimeLocal(this.statusEditingRepair.receiveDateTime) : '';
  }

  set statusReceiveDateTimeLocal(value: string) {
    if (this.statusEditingRepair) this.statusEditingRepair.receiveDateTime = fromDateTimeLocal(value);
  }

  get statusReturnDateTimeLocal(): string {
    return this.statusEditingRepair?.returnDateTime ? toDateTimeLocal(this.statusEditingRepair.returnDateTime) : '';
  }

  set statusReturnDateTimeLocal(value: string) {
    if (this.statusEditingRepair) this.statusEditingRepair.returnDateTime = fromDateTimeLocal(value);
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
    this.visibleRepairRows = this.repairs.map((repair) => this.toTableRow(repair));
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
      finalAmountLabel: this.formatMoney(repair.price),
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
      this.sortDirection = column === 'orderLabel' || column === 'finalAmountValue' ? 'desc' : 'asc';
    }
    this.currentPage = 1;
    this.reload();
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
    return resolveColumnWidth(this.repairColumns, columnKey);
  }

  startColumnResize(event: MouseEvent, columnKey: RepairTableColumnKey): void {
    beginColumnResize(event, columnKey, this.repairColumns, () => {
      this.persistColumnWidths();
      this.changeDetector.detectChanges();
    });
  }

  onDraftDeviceChange(): void {
    this.refreshSelectionSummaries();
  }

  filterEditClientSuggestions(query: string): void {
    this.requestClientSearch('edit', query);
  }

  onEditClientInputChange(value: string | ClientOption): void {
    if (typeof value !== 'string') return;
    this.editingClientName = value;
    if (this.editingSelectedClientLabel === value.trim()) return;
    this.editingSelectedClientLabel = '';
    this.editingRepair.idClient = '';
    this.editingRepair.idDevice = '';
    this.editDeviceOptions = [];
    this.refreshEditingDevicePassword();
  }

  onEditClientAutocompleteSelect(selection: ClientOption): void {
    const clientId = selection.client.id || '';
    if (!clientId) return;
    const clientChanged = clientId !== this.editingRepair.idClient;
    this.editingRepair.idClient = clientId;
    this.editingClientName = selection.label;
    this.editingSelectedClientLabel = selection.label;
    if (clientChanged) {
      this.editingRepair.idDevice = '';
      this.editDeviceOptions = [];
    }
    this.loadEditDevices(clientId);
  }

  onEditRepairDeviceChange(): void {
    if (!this.editDeviceOptions.some((option) => option.value === this.editingRepair.idDevice)) {
      this.editingRepair.idDevice = '';
    }
    this.refreshEditingDevicePassword();
  }

  openClientSearchModal(): void {
    this.clientSearch = '';
    this.filteredClientsList = [];
    this.clientModalSearchFailed = false;
    this.showClientModal = true;
  }

  onClientModalSearchChange(): void {
    this.requestClientSearch('modal', this.clientSearch);
  }

  get hasClientModalSearchTerm(): boolean {
    return this.clientSearch.trim().length >= 2;
  }

  private rebuildDeviceOptions(): void {
    this.deviceOptions = this.allDevices
      .map((d) => ({ label: `${d.brand || '-'} - ${d.model || '-'}`, value: d.id || '' }))
      .filter((d) => !!d.value);
  }

  private loadClientDevices(clientId: string): void {
    if (!clientId) {
      this.clientDevices = [];
      this.rebuildClientDeviceOptions();
      this.refreshSelectionSummaries();
      return;
    }
    this.api.getDevicesByClientId(clientId).subscribe((devices) => {
      this.clientDevices = devices;
      this.allDevices = devices;
      this.devicesById = new Map(devices.filter((item) => !!item.id).map((item) => [item.id!, item]));
      this.rebuildDeviceOptions();
      this.rebuildClientDeviceOptions();
      this.refreshSelectionSummaries();
      this.changeDetector.detectChanges();
    });
  }

  private loadEditDevices(clientId: string, done?: () => void): void {
    if (!clientId) {
      this.editDeviceOptions = [];
      done?.();
      return;
    }
    this.api.getDevicesByClientId(clientId).subscribe((devices) => {
      this.allDevices = devices;
      this.devicesById = new Map(devices.filter((item) => !!item.id).map((item) => [item.id!, item]));
      this.deviceOptions = devices
        .map((device) => ({ label: `${device.brand || '-'} - ${device.model || '-'}`, value: device.id || '' }))
        .filter((device) => !!device.value);
      this.editDeviceOptions = [...this.deviceOptions];
      if (!this.editDeviceOptions.some((option) => option.value === this.editingRepair.idDevice)) {
        this.editingRepair.idDevice = '';
      }
      this.refreshEditingDevicePassword();
      this.changeDetector.detectChanges();
      done?.();
    });
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
    this.selectedClientSummary = this.selectedClient && this.selectedClient.id === this.draft.idClient
      ? this.clientOptionLabel(this.selectedClient)
      : '';
    const device = this.clientDevices.find((item) => item.id === this.draft.idDevice) || this.devicesById.get(this.draft.idDevice);
    this.selectedDeviceSummary = device ? `${device.deviceTypeName || '-'} · ${device.brand} ${device.model}`.replace(/\s+/g, ' ').trim() : '';
    this.selectedDevicePassword = device?.currentPassword || '';
  }

  private refreshEditingDevicePassword(): void {
    const device = this.allDevices.find((item) => item.id === this.editingRepair.idDevice) || this.devicesById.get(this.editingRepair.idDevice);
    this.editingDevicePassword = device?.currentPassword || '';
  }

  private requestClientSearch(target: ClientSearchTarget, term: string): void {
    this.clientSearchChanges.next({ target, term: term || '' });
  }

  private setClientSearchResults(target: ClientSearchTarget, clients: Client[], failed: boolean): void {
    if (target === 'create') {
      this.clientSuggestions = this.toClientOptions(clients);
      return;
    }
    if (target === 'edit') {
      this.editClientSuggestions = this.toClientOptions(clients);
      return;
    }
    this.filteredClientsList = clients;
    this.clientModalSearchFailed = failed;
  }

  private setClientSearchLoading(target: ClientSearchTarget, loading: boolean): void {
    if (target === 'create') this.isClientAutocompleteLoading = loading;
    if (target === 'modal') this.isClientModalLoading = loading;
    if (target === 'edit') this.isEditClientLoading = loading;
  }

  private toClientOptions(clients: Client[]): ClientOption[] {
    return clients
      .filter((client) => !!client.id)
      .map((client) => ({ label: this.clientOptionLabel(client), value: client.id!, client }));
  }

  private clientOptionLabel(client: Client): string {
    return `${client.name || ''} ${client.lastName || ''}`.trim();
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

  private currentSortField(): RepairSortField | undefined {
    switch (this.sortColumn) {
      case 'clientLabel':
        return 'clientName';
      case 'deviceLabel':
        return 'deviceLabel';
      case 'statusLabel':
        return 'status';
      case 'finalAmountValue':
        return 'price';
      case 'orderLabel':
        return 'orderNumber';
      default:
        return undefined;
    }
  }

  private restoreColumnWidths(): void {
    restoreColumnWidths(this.repairColumns, this.columnWidthStorageKey);
  }

  private persistColumnWidths(): void {
    persistColumnWidths(this.repairColumns, this.columnWidthStorageKey);
  }
}
