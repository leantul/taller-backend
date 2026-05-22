import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { SelectModule } from 'primeng/select';
import { DialogModule } from 'primeng/dialog';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ApiService } from '../../core/services/api.service';
import { Device, DevicePasswordHistory } from '../../shared/models/device.model';
import { Client } from '../../shared/models/client.model';

const ARGENTINA_TIME_ZONE = 'America/Argentina/Buenos_Aires';
const HISTORY_DATE_HAS_TIME_ZONE = /(?:z|[+-]\d{2}:?\d{2})$/i;
const HISTORY_DATE_FORMATTER = new Intl.DateTimeFormat('es-AR', {
  timeZone: ARGENTINA_TIME_ZONE,
  day: '2-digit',
  month: '2-digit',
  year: 'numeric',
  hour: '2-digit',
  minute: '2-digit',
  hour12: false
});

@Component({
  selector: 'app-devices-page',
  standalone: true,
  imports: [CommonModule, FormsModule, CardModule, InputTextModule, ButtonModule, SelectModule, DialogModule, ConfirmDialogModule],
  providers: [ConfirmationService, MessageService],
  template: `
    <p-confirmdialog></p-confirmdialog>
    <section class="page-heading">
      <div>
        <span class="eyebrow">Inventario</span>
        <h1>Dispositivos</h1>
      </div>
      <p>Relacion entre clientes y equipos, con acceso rapido a la clave actual y su historial.</p>
    </section>

    <div class="page-grid">
      <p-card header="Nuevo dispositivo">
        <form class="p-fluid" (ngSubmit)="save()">
          <div class="field">
            <label>Cliente</label>
            <div class="inline-row">
              <p-select [options]="clientOptions" optionLabel="label" optionValue="value" [(ngModel)]="draft.clientId" name="clientId" [filter]="true" filterBy="label" required></p-select>
              <button pButton type="button" size="small" class="p-button-sm" icon="pi pi-user-plus" [rounded]="true" [text]="true" (click)="openNewClientModal('draft')"></button>
            </div>
          </div>
          <div class="field"><label>Marca</label><input pInputText [(ngModel)]="draft.brand" name="brand" required /></div>
          <div class="field"><label>Modelo</label><input pInputText [(ngModel)]="draft.model" name="model" required /></div>
          <div class="field"><label>Serie / IMEI</label><input pInputText [(ngModel)]="draft.serialNumber" name="serialNumber" required /></div>
          <div class="field"><label>Tipo</label><p-select [options]="typeOptions" optionLabel="label" optionValue="value" [(ngModel)]="draft.deviceType" name="deviceType"></p-select></div>
          <div class="field">
            <label>Contraseña inicial</label>
            <div class="inline-row">
              <input class="control" [type]="showDraftPassword ? 'text' : 'password'" [(ngModel)]="draft.currentPassword" name="currentPassword" placeholder="Opcional" />
              <button class="icon-button" type="button" [attr.aria-label]="showDraftPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'" (click)="showDraftPassword = !showDraftPassword">
                <i [class]="showDraftPassword ? 'pi pi-eye-slash' : 'pi pi-eye'"></i>
              </button>
            </div>
          </div>
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
            <thead><tr><th>Tipo</th><th>Marca</th><th>Modelo</th><th>Serie</th><th>Cliente</th><th>Contraseña actual</th><th>Acciones</th></tr></thead>
            <tbody>
              @for (d of visibleDevices; track d.id || d.serialNumber) {
                <tr>
                  <td>{{ d.deviceType }}</td>
                  <td>{{ d.brand }}</td>
                  <td>{{ d.model }}</td>
                  <td>{{ d.serialNumber }}</td>
                  <td>{{ getClientName(d.clientId) }}</td>
                  <td>
                    <div class="inline-row">
                      <span>{{ formatPassword(d.currentPassword, isDevicePasswordVisible(d.id || d.serialNumber)) }}</span>
                      @if (d.currentPassword) {
                        <button class="icon-action" type="button" aria-label="Mostrar contraseña" (click)="toggleDevicePassword(d.id || d.serialNumber)">
                          <i [class]="isDevicePasswordVisible(d.id || d.serialNumber) ? 'pi pi-eye-slash' : 'pi pi-eye'"></i>
                        </button>
                      }
                    </div>
                  </td>
                  <td>
                    <div class="action-buttons">
                      <button class="icon-action" type="button" aria-label="Gestionar contraseñas" (click)="openPasswordManager(d)"><i class="pi pi-key"></i></button>
                      <button class="icon-action" type="button" aria-label="Editar dispositivo" (click)="openEdit(d)"><i class="pi pi-pencil"></i></button>
                      <button class="icon-action danger" type="button" aria-label="Eliminar dispositivo" (click)="confirmRemove(d)"><i class="pi pi-trash"></i></button>
                    </div>
                  </td>
                </tr>
              } @empty {
                <tr><td class="empty-cell" colspan="7">No hay dispositivos para mostrar.</td></tr>
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
      <div class="field">
        <label>Cliente</label>
        <div class="inline-row">
          <p-select [options]="clientOptions" optionLabel="label" optionValue="value" [(ngModel)]="editing.clientId" [filter]="true" filterBy="label"></p-select>
          <button pButton type="button" size="small" class="p-button-sm" icon="pi pi-user-plus" [rounded]="true" [text]="true" (click)="openNewClientModal('edit')"></button>
        </div>
      </div>
      <div class="field"><label>Marca</label><input pInputText [(ngModel)]="editing.brand" /></div>
      <div class="field"><label>Modelo</label><input pInputText [(ngModel)]="editing.model" /></div>
      <div class="field"><label>Serie / IMEI</label><input pInputText [(ngModel)]="editing.serialNumber" /></div>
      <div class="field"><label>Tipo</label><p-select [options]="typeOptions" optionLabel="label" optionValue="value" [(ngModel)]="editing.deviceType"></p-select></div>
      <div class="field">
        <label>Contraseña actual</label>
        <div class="inline-row">
          <input class="control" [type]="showEditPassword ? 'text' : 'password'" [value]="editing.currentPassword || ''" readonly />
          <button class="icon-button" type="button" [attr.aria-label]="showEditPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'" (click)="showEditPassword = !showEditPassword">
            <i [class]="showEditPassword ? 'pi pi-eye-slash' : 'pi pi-eye'"></i>
          </button>
          <button class="secondary-button" type="button" (click)="openPasswordManager(editing)">Gestionar</button>
        </div>
      </div>
      <button pButton type="button" label="Guardar cambios" icon="pi pi-check" (click)="update()"></button>
    </p-dialog>

    <p-dialog header="Contraseñas del dispositivo" [(visible)]="passwordVisible" [modal]="true" [style]="{width:'46rem'}">
      @if (passwordDevice) {
        <div class="ops-summary-grid">
          <div class="ops-item">
            <span>Contraseña actual</span>
            <div class="inline-row">
              <strong>{{ formatPassword(passwordDevice.currentPassword, showCurrentPassword) }}</strong>
              <button class="icon-action" type="button" (click)="showCurrentPassword = !showCurrentPassword">
                <i [class]="showCurrentPassword ? 'pi pi-eye-slash' : 'pi pi-eye'"></i>
              </button>
            </div>
          </div>
        </div>

        <div class="field" style="margin-top:1rem;">
          <label>Agregar nueva contraseña</label>
          <div class="inline-row">
            <input class="control" [type]="showNewPassword ? 'text' : 'password'" [(ngModel)]="newPasswordValue" placeholder="Nueva contraseña" />
            <button class="icon-button" type="button" (click)="showNewPassword = !showNewPassword">
              <i [class]="showNewPassword ? 'pi pi-eye-slash' : 'pi pi-eye'"></i>
            </button>
            <button class="primary-button" type="button" (click)="addPassword()">Agregar</button>
          </div>
        </div>

        <div class="native-table-wrap" style="margin-top:1rem;">
          <table class="native-table">
            <thead><tr><th>Contraseña</th><th>Alta</th><th>Actualizada</th><th>Estado</th><th>Acciones</th></tr></thead>
            <tbody>
              @for (entry of passwordDevice.passwordHistory || []; track entry.id) {
                <tr>
                  <td>
                    @if (editingPasswordId === entry.id) {
                      <div class="inline-row">
                        <input class="control" [(ngModel)]="editingPasswordValue" />
                      </div>
                    } @else {
                      <div class="inline-row">
                        <span>{{ formatPassword(entry.value, isHistoryPasswordVisible(entry.id || '')) }}</span>
                        <button class="icon-action" type="button" (click)="toggleHistoryPassword(entry.id || '')">
                          <i [class]="isHistoryPasswordVisible(entry.id || '') ? 'pi pi-eye-slash' : 'pi pi-eye'"></i>
                        </button>
                      </div>
                    }
                  </td>
                  <td>{{ formatHistoryDate(entry.createdAt) }}</td>
                  <td>{{ formatHistoryDate(entry.updatedAt) }}</td>
                  <td><span class="status-pill" [ngClass]="entry.isCurrent ? 'is-success' : 'is-muted'">{{ entry.isCurrent ? 'Actual' : 'Histórica' }}</span></td>
                  <td>
                    <div class="action-buttons">
                      @if (editingPasswordId === entry.id) {
                        <button class="icon-action" type="button" (click)="savePasswordEdit(entry)"><i class="pi pi-check"></i></button>
                        <button class="icon-action danger" type="button" (click)="cancelPasswordEdit()"><i class="pi pi-times"></i></button>
                      } @else {
                        <button class="icon-action" type="button" (click)="startPasswordEdit(entry)"><i class="pi pi-pencil"></i></button>
                        <button class="icon-action" type="button" [disabled]="entry.isCurrent" (click)="makeCurrent(entry)"><i class="pi pi-star"></i></button>
                        <button class="icon-action danger" type="button" (click)="removePassword(entry)"><i class="pi pi-trash"></i></button>
                      }
                    </div>
                  </td>
                </tr>
              } @empty {
                <tr><td class="empty-cell" colspan="5">No hay contraseñas cargadas para este dispositivo.</td></tr>
              }
            </tbody>
          </table>
        </div>
      }
    </p-dialog>

    <p-dialog header="Nuevo cliente" [(visible)]="showNewClientModal" [modal]="true" [style]="{width:'34rem'}">
      <div class="field"><label>Nombre</label><input pInputText [(ngModel)]="draftClient.name" /></div>
      <div class="field"><label>Apellido</label><input pInputText [(ngModel)]="draftClient.lastName" /></div>
      <div class="field"><label>DNI</label><input pInputText [(ngModel)]="draftClient.dni" /></div>
      <div class="field"><label>Email</label><input pInputText [(ngModel)]="draftClient.email" /></div>
      <div class="field"><label>Celular</label><input pInputText [(ngModel)]="draftClient.phone" /></div>
      <button pButton type="button" label="Guardar cliente" icon="pi pi-check" (click)="createClientInline()"></button>
    </p-dialog>
  `
})
export class DevicesPageComponent implements OnInit {
  devices: Device[] = [];
  filteredDevices: (Device & { clientName?: string })[] = [];
  clients: Client[] = [];
  draft: Device = { brand: '', model: '', serialNumber: '', clientId: '', deviceType: 'NOTEBOOK', currentPassword: '' };
  editing: Device = { brand: '', model: '', serialNumber: '', clientId: '', deviceType: 'NOTEBOOK', currentPassword: '', passwordHistory: [] };
  draftClient: Client = { name: '', lastName: '', dni: '', email: '', phone: '' };
  editVisible = false;
  passwordVisible = false;
  showNewClientModal = false;
  selectedClientId: string | null = null;
  searchTerm = '';
  currentPage = 1;
  pageSize = 10;
  showDraftPassword = false;
  showEditPassword = false;
  showCurrentPassword = false;
  showNewPassword = false;
  passwordDevice: Device | null = null;
  newPasswordValue = '';
  editingPasswordId: string | null = null;
  editingPasswordValue = '';
  visibleDevicePasswords = new Set<string>();
  visibleHistoryPasswords = new Set<string>();
  private newClientTarget: 'draft' | 'edit' = 'draft';
  typeOptions = [
    { label: 'Desktop', value: 'DESKTOP' }, { label: 'Notebook', value: 'NOTEBOOK' }, { label: 'Tablet', value: 'TABLET' }, { label: 'Celular', value: 'CELULAR' }, { label: 'Otros', value: 'OTROS' }
  ];

  constructor(
    private readonly api: ApiService,
    private readonly confirmationService: ConfirmationService,
    private readonly changeDetector: ChangeDetectorRef,
    private readonly messageService: MessageService
  ) {}

  ngOnInit(): void {
    this.api.getClients().subscribe((clients) => {
      this.clients = clients;
      this.reload();
    });
  }

  get clientOptions(): { label: string; value: string }[] {
    return this.clients.map((client) => ({ label: `${client.name} ${client.lastName}`.trim(), value: client.id! }));
  }

  save(): void {
    this.api.createDevice(this.draft).subscribe(() => {
      this.draft = { brand: '', model: '', serialNumber: '', clientId: '', deviceType: 'NOTEBOOK', currentPassword: '' };
      this.showDraftPassword = false;
      this.reload();
    });
  }

  openEdit(device: Device): void {
    if (!device.id) return;
    this.api.getDeviceById(device.id).subscribe((detail) => {
      this.editing = this.normalizeDevice(detail);
      this.showEditPassword = false;
      this.editVisible = true;
      this.changeDetector.detectChanges();
    });
  }

  update(): void {
    this.api.updateDevice(this.editing).subscribe((device) => {
      this.syncDevice(device);
      this.editVisible = false;
      this.reload();
    });
  }

  openNewClientModal(target: 'draft' | 'edit'): void {
    this.newClientTarget = target;
    this.draftClient = { name: '', lastName: '', dni: '', email: '', phone: '' };
    this.showNewClientModal = true;
  }

  createClientInline(): void {
    if (!this.draftClient.name?.trim() || !this.draftClient.lastName?.trim() || !this.draftClient.phone?.trim()) {
      this.messageService.add({ severity: 'warn', summary: 'Faltan datos', detail: 'Completá al menos nombre, apellido y teléfono.' });
      return;
    }

    this.api.createClient(this.draftClient).subscribe({
      next: (client) => {
        this.clients = [client, ...this.clients];
        if (this.newClientTarget === 'draft') {
          this.draft.clientId = client.id || '';
        } else {
          this.editing.clientId = client.id || '';
        }
        this.draftClient = { name: '', lastName: '', dni: '', email: '', phone: '' };
        this.showNewClientModal = false;
        this.applyFilters();
        this.changeDetector.detectChanges();
      },
      error: () => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudo crear el cliente.' });
      }
    });
  }

  openPasswordManager(device: Device): void {
    if (!device.id) return;
    this.api.getDeviceById(device.id).subscribe((detail) => {
      this.passwordDevice = this.normalizeDevice(detail);
      this.passwordVisible = true;
      this.showCurrentPassword = false;
      this.showNewPassword = false;
      this.newPasswordValue = '';
      this.editingPasswordId = null;
      this.editingPasswordValue = '';
      this.visibleHistoryPasswords.clear();
      this.changeDetector.detectChanges();
    });
  }

  addPassword(): void {
    if (!this.passwordDevice?.id || !this.newPasswordValue.trim()) return;
    this.api.addDevicePassword(this.passwordDevice.id, this.newPasswordValue.trim()).subscribe((device) => {
      this.applyPasswordDevice(device);
      this.newPasswordValue = '';
      this.showNewPassword = false;
    });
  }

  startPasswordEdit(entry: DevicePasswordHistory): void {
    this.editingPasswordId = entry.id || null;
    this.editingPasswordValue = entry.value;
  }

  cancelPasswordEdit(): void {
    this.editingPasswordId = null;
    this.editingPasswordValue = '';
  }

  savePasswordEdit(entry: DevicePasswordHistory): void {
    if (!this.passwordDevice?.id || !entry.id || !this.editingPasswordValue.trim()) return;
    this.api.updateDevicePassword(this.passwordDevice.id, entry.id, this.editingPasswordValue.trim()).subscribe((device) => {
      this.applyPasswordDevice(device);
      this.cancelPasswordEdit();
    });
  }

  makeCurrent(entry: DevicePasswordHistory): void {
    if (!this.passwordDevice?.id || !entry.id) return;
    this.api.makeCurrentDevicePassword(this.passwordDevice.id, entry.id).subscribe((device) => this.applyPasswordDevice(device));
  }

  removePassword(entry: DevicePasswordHistory): void {
    if (!this.passwordDevice?.id || !entry.id) return;
    this.confirmationService.confirm({
      message: '¿Eliminar esta contraseña del historial?',
      header: 'Eliminar contraseña',
      acceptLabel: 'Eliminar',
      rejectLabel: 'Cancelar',
      accept: () => {
        this.api.deleteDevicePassword(this.passwordDevice!.id!, entry.id!).subscribe((device) => this.applyPasswordDevice(device));
      }
    });
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
        const byTerm = !term || `${device.deviceType} ${device.brand} ${device.model} ${device.serialNumber} ${device.clientId} ${device.clientName} ${device.currentPassword || ''}`.toLowerCase().includes(term);
        return byClient && byTerm;
      });
    this.currentPage = 1;
  }

  getClientName(clientId: string): string {
    const client = this.clients.find((c) => c.id === clientId);
    return client ? `${client.name} ${client.lastName}` : clientId;
  }

  formatPassword(value: string | undefined, visible: boolean): string {
    if (!value) return 'Sin clave';
    return visible ? value : '•'.repeat(Math.max(6, value.length));
  }

  formatHistoryDate(value: string | undefined): string {
    const date = this.parseHistoryDate(value);
    if (!date) return '-';

    const parts = new Map(HISTORY_DATE_FORMATTER.formatToParts(date).map((part) => [part.type, part.value]));
    return `${parts.get('day')}/${parts.get('month')}/${parts.get('year')} ${parts.get('hour')}:${parts.get('minute')}`;
  }

  isDevicePasswordVisible(key: string): boolean {
    return this.visibleDevicePasswords.has(key);
  }

  toggleDevicePassword(key: string): void {
    if (this.visibleDevicePasswords.has(key)) {
      this.visibleDevicePasswords.delete(key);
    } else {
      this.visibleDevicePasswords.add(key);
    }
  }

  isHistoryPasswordVisible(key: string): boolean {
    return this.visibleHistoryPasswords.has(key);
  }

  toggleHistoryPassword(key: string): void {
    if (this.visibleHistoryPasswords.has(key)) {
      this.visibleHistoryPasswords.delete(key);
    } else {
      this.visibleHistoryPasswords.add(key);
    }
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

  private reload(): void {
    this.api.getDevices().subscribe((devices) => {
      this.devices = devices.slice().reverse().map((device) => this.normalizeDevice(device));
      this.applyFilters();
      this.changeDetector.detectChanges();
    });
  }

  private normalizeDevice(device: Device): Device {
    return {
      ...device,
      currentPassword: device.currentPassword || '',
      passwordHistory: (device.passwordHistory || [])
        .slice()
        .sort((left, right) => this.historyTimestamp(right.createdAt) - this.historyTimestamp(left.createdAt))
    };
  }

  private historyTimestamp(value: string | undefined): number {
    return this.parseHistoryDate(value)?.getTime() || 0;
  }

  private parseHistoryDate(value: string | undefined): Date | null {
    const trimmed = value?.trim();
    if (!trimmed) return null;

    const normalized = trimmed.includes('T') ? trimmed : trimmed.replace(' ', 'T');
    const withTime = /^\d{4}-\d{2}-\d{2}$/.test(normalized) ? `${normalized}T00:00:00` : normalized;
    const isoValue = HISTORY_DATE_HAS_TIME_ZONE.test(withTime) ? withTime : `${withTime}Z`;
    const date = new Date(isoValue);

    return Number.isNaN(date.getTime()) ? null : date;
  }

  private applyPasswordDevice(device: Device): void {
    const normalized = this.normalizeDevice(device);
    this.passwordDevice = normalized;
    this.syncDevice(normalized);
    if (this.editing.id && normalized.id === this.editing.id) {
      this.editing = { ...this.editing, currentPassword: normalized.currentPassword, passwordHistory: normalized.passwordHistory };
    }
    this.applyFilters();
    this.changeDetector.detectChanges();
  }

  private syncDevice(device: Device): void {
    const normalized = this.normalizeDevice(device);
    this.devices = this.devices.map((item) => item.id === normalized.id ? { ...item, ...normalized } : item);
  }
}
