import { ChangeDetectorRef, Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { SelectModule } from 'primeng/select';
import { AutoCompleteModule } from 'primeng/autocomplete';
import { DialogModule } from 'primeng/dialog';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService, MessageService } from 'primeng/api';
import { debounceTime, distinctUntilChanged, forkJoin, Subject, Subscription } from 'rxjs';
import { ApiService } from '../../core/services/api.service';
import { Device, DeviceObservation, DevicePasswordHistory, DeviceRepairHistoryItem, DeviceType } from '../../shared/models/device.model';
import { Client } from '../../shared/models/client.model';
import { RepairDetailDialogComponent } from '../../shared/components/repair-detail-dialog.component';
import { repairStatusClass, repairStatusLabel } from '../../shared/utils/repair-status.util';

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

type DeviceTableColumnKey = 'deviceType' | 'brand' | 'model' | 'client' | 'observations' | 'password' | 'actions';
type DeviceSortColumn = Exclude<DeviceTableColumnKey, 'actions'>;
type DeviceTableColumn = {
  key: DeviceTableColumnKey;
  label: string;
  width: string;
  sortable: boolean;
};

@Component({
  selector: 'app-devices-page',
  standalone: true,
  imports: [CommonModule, FormsModule, CardModule, InputTextModule, ButtonModule, SelectModule, AutoCompleteModule, DialogModule, ConfirmDialogModule, RepairDetailDialogComponent],
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

    <div class="page-grid" [class.create-panel-hidden]="!showCreatePanel">
      @if (showCreatePanel) {
      <div class="create-panel-shell">
        <button class="icon-action form-toggle-button create-panel-toggle" type="button" aria-label="Ocultar formulario de nuevo dispositivo" title="Ocultar formulario" (click)="showCreatePanel = false">
          <i class="pi pi-eye-slash"></i>
        </button>
        <p-card header="Nuevo dispositivo">
          <form class="p-fluid" autocomplete="off" (ngSubmit)="save()">
            <div class="field">
              <label>Cliente</label>
              <div class="inline-row">
                <p-select [options]="clientOptions" optionLabel="label" optionValue="value" [(ngModel)]="draft.clientId" name="deviceClientId" [filter]="true" filterBy="label" required></p-select>
                <button pButton type="button" size="small" class="p-button-sm" icon="pi pi-user-plus" [rounded]="true" [text]="true" (click)="openNewClientModal('draft')"></button>
              </div>
            </div>
            <div class="field"><label>Marca</label><input pInputText [(ngModel)]="draft.brand" name="deviceBrand" autocomplete="off" required /></div>
            <div class="field"><label>Modelo</label><input pInputText [(ngModel)]="draft.model" name="deviceModel" autocomplete="off" required /></div>
            <div class="field"><label>Serie / IMEI</label><input pInputText [(ngModel)]="draft.serialNumber" name="deviceSerialNumber" autocomplete="off" required /></div>
            <div class="field"><label>Tipo</label><p-select [options]="typeOptions" optionLabel="name" optionValue="id" [(ngModel)]="draft.deviceTypeId" name="deviceTypeId"></p-select></div>
            <div class="field"><label>Características</label><textarea class="p-inputtext" rows="5" [(ngModel)]="draft.technicalDetails" name="deviceTechnicalDetails" placeholder="Memoria, disco, procesador, placa, detalles internos"></textarea></div>
            <div class="field">
              <label>Contraseña inicial</label>
              <div class="inline-row">
                <input class="control" [type]="showDraftPassword ? 'text' : 'password'" [(ngModel)]="draft.currentPassword" name="deviceCurrentPassword" autocomplete="new-password" placeholder="Opcional" />
                <button class="icon-button" type="button" [attr.aria-label]="showDraftPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'" (click)="showDraftPassword = !showDraftPassword">
                  <i [class]="showDraftPassword ? 'pi pi-eye-slash' : 'pi pi-eye'"></i>
                </button>
              </div>
            </div>
            <button pButton type="submit" label="Guardar dispositivo" icon="pi pi-check"></button>
          </form>
        </p-card>
      </div>
      }

      <p-card header="Dispositivos">
        <div class="table-toolbar multi repairs-filters">
          <span class="p-input-icon-left filter-search"><i class="pi pi-search"></i><input pInputText [(ngModel)]="searchTerm" (ngModelChange)="onSearchTermChange()" placeholder="Buscar por cualquier campo" /></span>
          <p-autoComplete
            [(ngModel)]="selectedClientTerm"
            [suggestions]="clientFilterSuggestions"
            (completeMethod)="filterClientOptions($event.query)"
            (ngModelChange)="onClientFilterChange($event)"
            (onSelect)="onClientFilterSelect($event.value)"
            optionLabel="label"
            placeholder="Filtrar por cliente"
            styleClass="compact-filter client-filter-autocomplete"
            appendTo="body"></p-autoComplete>
          @if (!showCreatePanel) {
            <button class="icon-action form-toggle-button" type="button" aria-label="Mostrar formulario de nuevo dispositivo" title="Mostrar formulario" (click)="showCreatePanel = true">
              <i class="pi pi-eye"></i>
            </button>
          }
        </div>
        <div class="native-table-wrap">
          <table class="native-table resizable-table devices-table">
            <thead>
              <tr>
                @for (column of deviceColumns; track column.key) {
                  <th [style.width]="columnWidth(column.key)">
                    @if (column.sortable) {
                      <button class="sortable-th" type="button" (click)="sortByColumn(column.key)">
                        <span>{{ column.label }}</span>
                        <i [ngClass]="sortIcon(column.key)"></i>
                      </button>
                    } @else {
                      <span class="table-head-label">{{ column.label }}</span>
                    }
                    <button
                      class="column-resize-handle"
                      type="button"
                      tabindex="-1"
                      aria-hidden="true"
                      (click)="$event.stopPropagation()"
                      (mousedown)="startColumnResize($event, column.key)">
                    </button>
                  </th>
                }
              </tr>
            </thead>
            <tbody>
              @for (d of visibleDevices; track d.id || d.serialNumber) {
                <tr class="clickable-row" (click)="openDeviceDetail(d)">
                  <td>{{ d.deviceTypeName || '-' }}</td>
                  <td>{{ d.brand }}</td>
                  <td>{{ d.model }}</td>
                  <td>{{ d.clientName || getClientName(d.clientId) }}</td>
                  <td>
                    <button class="observation-summary-button" type="button" (click)="stop($event); openObservationManager(d)">
                      <i class="pi pi-flag"></i>
                      <span>{{ observationSummary(d) }}</span>
                    </button>
                  </td>
                  <td class="cell-actions">
                    <div class="inline-row">
                      <span>{{ formatPassword(d.currentPassword, isDevicePasswordVisible(d.id || d.serialNumber)) }}</span>
                      @if (d.currentPassword) {
                        <button class="icon-action" type="button" aria-label="Mostrar contraseña" (click)="stop($event); toggleDevicePassword(d.id || d.serialNumber)">
                          <i [class]="isDevicePasswordVisible(d.id || d.serialNumber) ? 'pi pi-eye-slash' : 'pi pi-eye'"></i>
                        </button>
                      }
                    </div>
                  </td>
                  <td (click)="$event.stopPropagation()">
                    <div class="action-buttons">
                      <button class="icon-action" type="button" aria-label="Gestionar observaciones" (click)="openObservationManager(d)"><i class="pi pi-flag"></i></button>
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

    <p-dialog
      header="Detalle del dispositivo"
      [(visible)]="detailVisible"
      [modal]="true"
      [style]="{width:'70rem', maxWidth:'95vw'}"
      [contentStyle]="{overflow:'hidden', padding:'0.35rem'}">
      @if (detailDevice) {
        <div class="detail-dialog-body">
          <div class="detail-grid device-detail-grid">
            <div class="detail-item"><label>Tipo</label><strong>{{ detailDevice.deviceTypeName || '-' }}</strong></div>
            <div class="detail-item"><label>Cliente</label><strong>{{ detailDevice.clientName || getClientName(detailDevice.clientId) || '-' }}</strong></div>
            <div class="detail-item"><label>Marca</label><strong>{{ detailDevice.brand || '-' }}</strong></div>
            <div class="detail-item"><label>Modelo</label><strong>{{ detailDevice.model || '-' }}</strong></div>
            @if (detailDevice.serialNumber) {
              <div class="detail-item detail-wide device-serial-detail"><label>Serie / IMEI</label><strong>{{ detailDevice.serialNumber }}</strong></div>
            }
            <div class="detail-item detail-wide">
              <label>Contraseña actual</label>
              <div class="inline-row readonly-password-row">
                <strong>{{ formatPassword(detailDevice.currentPassword, showDetailPassword) }}</strong>
                @if (detailDevice.currentPassword) {
                  <button class="icon-action" type="button" [attr.aria-label]="showDetailPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'" (click)="showDetailPassword = !showDetailPassword">
                    <i [class]="showDetailPassword ? 'pi pi-eye-slash' : 'pi pi-eye'"></i>
                  </button>
                }
              </div>
            </div>
            @if (detailDevice.technicalDetails) {
              <div class="detail-item detail-wide detail-text-block"><label>Características</label><div class="detail-scrollable">{{ detailDevice.technicalDetails }}</div></div>
            }
          </div>

          <div class="detail-section-heading">
            <div><span class="eyebrow">Trazabilidad</span><h3>Historial de reparaciones</h3></div>
            <span class="detail-count">{{ detailHistoryTotalElements }} {{ detailHistoryTotalElements === 1 ? 'reparación' : 'reparaciones' }}</span>
          </div>
          <div class="native-table-wrap device-history-table">
            <table class="native-table">
              <thead><tr><th>Orden</th><th>Estado</th><th>Falla reportada</th><th>Ingreso</th><th>Entrega</th><th>Detalle</th></tr></thead>
              <tbody>
                @for (repair of detailRepairs; track repair.id) {
                  <tr>
                    <td>#{{ repair.orderNumber || '-' }}</td>
                    <td><span class="status-pill" [ngClass]="statusClass(repair.status)">{{ statusLabel(repair.status) }}</span></td>
                    <td>{{ repair.description || '-' }}</td>
                    <td>{{ repair.receiveDateTime ? (repair.receiveDateTime | date:'dd/MM/yyyy HH:mm') : '-' }}</td>
                    <td>{{ repair.returnDateTime ? (repair.returnDateTime | date:'dd/MM/yyyy HH:mm') : '-' }}</td>
                    <td><button class="icon-action" type="button" aria-label="Ver reparación" (click)="openRepairDetail(repair)"><i class="pi pi-eye"></i></button></td>
                  </tr>
                } @empty {
                  <tr><td class="empty-cell" colspan="6">Este dispositivo no tiene reparaciones registradas.</td></tr>
                }
              </tbody>
            </table>
          </div>
          <div class="table-pager" aria-label="Paginación del historial del dispositivo">
            <span>{{ detailPaginationLabel }}</span>
            <div class="pager-actions">
              <button class="pager-button" type="button" [disabled]="detailHistoryPage === 0" (click)="previousDetailHistoryPage()"><i class="pi pi-chevron-left"></i></button>
              <span>Página {{ detailHistoryPage + 1 }} de {{ displayDetailHistoryTotalPages }}</span>
              <button class="pager-button" type="button" [disabled]="detailHistoryPage + 1 >= displayDetailHistoryTotalPages" (click)="nextDetailHistoryPage()"><i class="pi pi-chevron-right"></i></button>
            </div>
          </div>
        </div>
      }
    </p-dialog>

    <p-dialog header="Editar dispositivo" [(visible)]="editVisible" [modal]="true" [style]="{width:'34rem'}">
      <div class="field">
        <label>Cliente</label>
        <div class="inline-row">
          <p-select [options]="clientOptions" optionLabel="label" optionValue="value" [(ngModel)]="editing.clientId" [filter]="true" filterBy="label"></p-select>
          <button pButton type="button" size="small" class="p-button-sm" icon="pi pi-user-plus" [rounded]="true" [text]="true" (click)="openNewClientModal('edit')"></button>
        </div>
      </div>
      <div class="field"><label>Marca</label><input pInputText [(ngModel)]="editing.brand" autocomplete="off" /></div>
      <div class="field"><label>Modelo</label><input pInputText [(ngModel)]="editing.model" autocomplete="off" /></div>
      <div class="field"><label>Serie / IMEI</label><input pInputText [(ngModel)]="editing.serialNumber" autocomplete="off" /></div>
      <div class="field"><label>Tipo</label><p-select [options]="typeOptions" optionLabel="name" optionValue="id" [(ngModel)]="editing.deviceTypeId"></p-select></div>
      <div class="field"><label>Características</label><textarea class="p-inputtext" rows="6" [(ngModel)]="editing.technicalDetails" placeholder="Memoria, disco, procesador, placa, detalles internos"></textarea></div>
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
            <input class="control" [type]="showNewPassword ? 'text' : 'password'" [(ngModel)]="newPasswordValue" autocomplete="new-password" placeholder="Nueva contraseña" />
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
                        <input class="control" [(ngModel)]="editingPasswordValue" autocomplete="off" />
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

    <p-dialog header="Observaciones del dispositivo" [(visible)]="observationVisible" [modal]="true" [style]="{width:'52rem'}">
      @if (observationDevice) {
        <div class="ops-summary-grid">
          <div class="ops-item">
            <span>Equipo</span>
            <strong>{{ observationDevice.brand }} {{ observationDevice.model }}</strong>
          </div>
          <div class="ops-item">
            <span>Pendientes</span>
            <strong>{{ activeObservations(observationDevice).length }}</strong>
          </div>
        </div>

        <div class="field" style="margin-top:1rem;">
          <label>Agregar observación</label>
          <div class="inline-row">
            <input class="control" [(ngModel)]="newObservationNote" autocomplete="off" placeholder="Ej: batería para reemplazar" />
            <button class="primary-button" type="button" (click)="addObservation()">Agregar</button>
          </div>
        </div>

        <div class="native-table-wrap" style="margin-top:1rem;">
          <table class="native-table">
            <thead><tr><th>Observación</th><th>Observada</th><th>Seguimiento</th><th>Estado</th><th>Acciones</th></tr></thead>
            <tbody>
              @for (entry of observationDevice.observations || []; track entry.id || $index) {
                <tr>
                  <td>
                    @if (editingObservationId === entry.id) {
                      <input class="control" [(ngModel)]="editingObservationNote" autocomplete="off" />
                    } @else {
                      {{ entry.note }}
                    }
                  </td>
                  <td>{{ formatHistoryDate(entry.observedAt) }}</td>
                  <td>{{ formatHistoryDate(entry.followUpAt) }}</td>
                  <td><span class="status-pill" [ngClass]="entry.resolvedAt ? 'is-success' : 'is-warning'">{{ entry.resolvedAt ? 'Resuelta' : 'Pendiente' }}</span></td>
                  <td>
                    <div class="action-buttons">
                      @if (editingObservationId === entry.id) {
                        <button class="icon-action" type="button" (click)="saveObservationEdit(entry)"><i class="pi pi-check"></i></button>
                        <button class="icon-action danger" type="button" (click)="cancelObservationEdit()"><i class="pi pi-times"></i></button>
                      } @else {
                        <button class="icon-action" type="button" [disabled]="!!entry.resolvedAt" (click)="startObservationEdit(entry)"><i class="pi pi-pencil"></i></button>
                        <button class="icon-action" type="button" [disabled]="!!entry.resolvedAt" (click)="resolveObservation(entry)"><i class="pi pi-check-circle"></i></button>
                        <button class="icon-action danger" type="button" (click)="deleteObservation(entry)"><i class="pi pi-trash"></i></button>
                      }
                    </div>
                  </td>
                </tr>
              } @empty {
                <tr><td class="empty-cell" colspan="5">No hay observaciones cargadas para este dispositivo.</td></tr>
              }
            </tbody>
          </table>
        </div>
      }
    </p-dialog>

    <p-dialog header="Nuevo cliente" [(visible)]="showNewClientModal" [modal]="true" [style]="{width:'34rem'}">
      <div class="field"><label>Nombre</label><input pInputText [(ngModel)]="draftClient.name" autocomplete="off" /></div>
      <div class="field"><label>Apellido</label><input pInputText [(ngModel)]="draftClient.lastName" autocomplete="off" /></div>
      <div class="field"><label>Referencia</label><textarea class="p-inputtext" rows="3" [(ngModel)]="draftClient.reference" placeholder="Amigo de..., hermano de..."></textarea></div>
      <div class="field"><label>Email</label><input pInputText [(ngModel)]="draftClient.email" autocomplete="off" /></div>
      <div class="field"><label>Celular</label><input pInputText [(ngModel)]="draftClient.phone" autocomplete="off" /></div>
      <button pButton type="button" label="Guardar cliente" icon="pi pi-check" (click)="createClientInline()"></button>
    </p-dialog>
    <app-repair-detail-dialog></app-repair-detail-dialog>
  `
})
export class DevicesPageComponent implements OnInit, OnDestroy {
  @ViewChild(RepairDetailDialogComponent) private repairDetailDialog?: RepairDetailDialogComponent;
  devices: Device[] = [];
  filteredDevices: (Device & { clientName?: string })[] = [];
  clients: Client[] = [];
  draft: Device = { brand: '', model: '', serialNumber: '', clientId: '', deviceTypeId: '', technicalDetails: '', currentPassword: '' };
  editing: Device = { brand: '', model: '', serialNumber: '', clientId: '', deviceTypeId: '', technicalDetails: '', currentPassword: '', passwordHistory: [] };
  draftClient: Client = { name: '', lastName: '', reference: '', email: '', phone: '' };
  editVisible = false;
  passwordVisible = false;
  observationVisible = false;
  detailVisible = false;
  showNewClientModal = false;
  selectedClientTerm: string | { label: string; value: string } = '';
  selectedClientId: string | null = null;
  searchTerm = '';
  currentPage = 1;
  pageSize = 10;
  totalPages = 1;
  totalElements = 0;
  showCreatePanel = true;
  sortBy: 'createdAt' | DeviceSortColumn = 'createdAt';
  sortDir: 'asc' | 'desc' = 'desc';
  showDraftPassword = false;
  showEditPassword = false;
  showCurrentPassword = false;
  showNewPassword = false;
  passwordDevice: Device | null = null;
  observationDevice: Device | null = null;
  detailDevice: Device | null = null;
  detailRepairs: DeviceRepairHistoryItem[] = [];
  detailHistoryPage = 0;
  detailHistoryPageSize = 5;
  detailHistoryTotalElements = 0;
  detailHistoryTotalPages = 0;
  showDetailPassword = false;
  newPasswordValue = '';
  newObservationNote = '';
  editingPasswordId: string | null = null;
  editingPasswordValue = '';
  editingObservationId: string | null = null;
  editingObservationNote = '';
  visibleDevicePasswords = new Set<string>();
  visibleHistoryPasswords = new Set<string>();
  clientFilterSuggestions: { label: string; value: string }[] = [];
  private newClientTarget: 'draft' | 'edit' = 'draft';
  typeOptions: DeviceType[] = [];
  readonly deviceColumns: DeviceTableColumn[] = [
    { key: 'deviceType', label: 'Tipo', width: '10rem', sortable: true },
    { key: 'brand', label: 'Marca', width: '10rem', sortable: true },
    { key: 'model', label: 'Modelo', width: '12rem', sortable: true },
    { key: 'client', label: 'Cliente', width: '16rem', sortable: true },
    { key: 'observations', label: 'Observaciones', width: '14rem', sortable: true },
    { key: 'password', label: 'Contraseña actual', width: '14rem', sortable: true },
    { key: 'actions', label: 'Acciones', width: '13rem', sortable: false }
  ];
  private readonly columnWidthStorageKey = 'taller.devices.columnWidths';
  private resizingColumnKey: DeviceTableColumnKey | null = null;
  private resizeStartX = 0;
  private resizeStartWidth = 0;
  private pageRequest?: Subscription;
  private readonly searchChanges = new Subject<string>();
  private searchSubscription?: Subscription;

  constructor(
    private readonly api: ApiService,
    private readonly confirmationService: ConfirmationService,
    private readonly changeDetector: ChangeDetectorRef,
    private readonly messageService: MessageService
  ) {}

  ngOnInit(): void {
    this.restoreColumnWidths();
    this.searchSubscription = this.searchChanges.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(() => this.applyFilters());
    this.reload();
    forkJoin({ clients: this.api.getClients(), deviceTypes: this.api.getDeviceTypes() }).subscribe(({ clients, deviceTypes }) => {
      this.clients = clients;
      this.typeOptions = deviceTypes;
      this.draft.deviceTypeId = this.defaultDeviceTypeId();
    });
  }

  ngOnDestroy(): void {
    this.pageRequest?.unsubscribe();
    this.searchSubscription?.unsubscribe();
  }

  onSearchTermChange(): void {
    this.searchChanges.next(this.searchTerm.trim());
  }

  get clientOptions(): { label: string; value: string }[] {
    return this.clients.map((client) => ({ label: `${client.name} ${client.lastName}`.trim(), value: client.id! }));
  }

  private defaultDeviceTypeId(): string {
    return this.typeOptions.find((type) => type.name.toLowerCase() === 'notebook')?.id
      || this.typeOptions[0]?.id
      || '';
  }

  filterClientOptions(query: string): void {
    const term = (query || '').trim().toLowerCase();
    this.clientFilterSuggestions = this.clientOptions
      .filter((client) => !term || client.label.toLowerCase().includes(term))
      .slice(0, 10);
  }

  onClientFilterChange(value: string | { label: string; value: string } | null): void {
    this.selectedClientTerm = value || '';
    if (!value || typeof value === 'string') {
      this.selectedClientId = null;
    }
    this.searchChanges.next(`${this.searchTerm.trim()}|${this.selectedClientSearchText()}`);
  }

  onClientFilterSelect(selection: { label: string; value: string }): void {
    this.selectedClientTerm = selection;
    this.selectedClientId = selection?.value || null;
    this.searchChanges.next(`${this.searchTerm.trim()}|${this.selectedClientId || ''}`);
  }

  save(): void {
    this.api.createDevice(this.draft).subscribe(() => {
      this.draft = { brand: '', model: '', serialNumber: '', clientId: '', deviceTypeId: this.defaultDeviceTypeId(), technicalDetails: '', currentPassword: '' };
      this.showDraftPassword = false;
      this.reload();
    });
  }

  openDeviceDetail(device: Device): void {
    if (!device.id) return;
    this.detailDevice = this.normalizeDevice(device);
    this.detailRepairs = [];
    this.detailHistoryPage = 0;
    this.detailHistoryTotalElements = 0;
    this.detailHistoryTotalPages = 0;
    this.showDetailPassword = false;

    forkJoin({
      device: this.api.getDeviceById(device.id),
      repairs: this.api.getDeviceRepairHistory(device.id, this.detailHistoryPage, this.detailHistoryPageSize)
    }).subscribe({
      next: ({ device: detail, repairs }) => {
        this.detailDevice = this.normalizeDevice({
          ...detail,
          clientName: detail.clientName || device.clientName || this.getClientName(detail.clientId)
        });
        this.applyDetailHistoryPage(repairs);
        this.detailVisible = true;
        this.changeDetector.detectChanges();
      },
      error: () => this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudo cargar el detalle del dispositivo.' })
    });
  }

  openRepairDetail(repair: DeviceRepairHistoryItem): void {
    if (!this.detailDevice) return;
    this.repairDetailDialog?.open(
      repair.id,
      this.detailDevice.clientName || this.getClientName(this.detailDevice.clientId) || '-',
      `${this.detailDevice.brand} ${this.detailDevice.model}`.trim() || '-');
  }

  previousDetailHistoryPage(): void {
    if (this.detailHistoryPage === 0) return;
    this.detailHistoryPage--;
    this.loadDetailHistory();
  }

  nextDetailHistoryPage(): void {
    if (this.detailHistoryPage + 1 >= this.detailHistoryTotalPages) return;
    this.detailHistoryPage++;
    this.loadDetailHistory();
  }

  statusLabel(status: DeviceRepairHistoryItem['status']): string {
    return repairStatusLabel(status);
  }

  statusClass(status: DeviceRepairHistoryItem['status']): string {
    return repairStatusClass(status);
  }

  stop(event: Event): void {
    event.stopPropagation();
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
    this.draftClient = { name: '', lastName: '', reference: '', email: '', phone: '' };
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
        this.draftClient = { name: '', lastName: '', reference: '', email: '', phone: '' };
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

  openObservationManager(device: Device): void {
    if (!device.id) return;
    this.api.getDeviceById(device.id).subscribe((detail) => {
      this.observationDevice = this.normalizeDevice(detail);
      this.observationVisible = true;
      this.newObservationNote = '';
      this.editingObservationId = null;
      this.editingObservationNote = '';
      this.changeDetector.detectChanges();
    });
  }

  addObservation(): void {
    if (!this.observationDevice?.id || !this.newObservationNote.trim()) return;
    this.api.addDeviceObservation(this.observationDevice.id, { note: this.newObservationNote.trim() }).subscribe((device) => {
      this.applyObservationDevice(device);
      this.newObservationNote = '';
    });
  }

  startObservationEdit(entry: DeviceObservation): void {
    this.editingObservationId = entry.id || null;
    this.editingObservationNote = entry.note;
  }

  cancelObservationEdit(): void {
    this.editingObservationId = null;
    this.editingObservationNote = '';
  }

  saveObservationEdit(entry: DeviceObservation): void {
    if (!this.observationDevice?.id || !entry.id || !this.editingObservationNote.trim()) return;
    this.api.updateDeviceObservation(this.observationDevice.id, entry.id, { ...entry, note: this.editingObservationNote.trim() }).subscribe((device) => {
      this.applyObservationDevice(device);
      this.cancelObservationEdit();
    });
  }

  resolveObservation(entry: DeviceObservation): void {
    if (!this.observationDevice?.id || !entry.id) return;
    this.api.resolveDeviceObservation(this.observationDevice.id, entry.id).subscribe((device) => this.applyObservationDevice(device));
  }

  deleteObservation(entry: DeviceObservation): void {
    if (!this.observationDevice?.id || !entry.id) return;
    this.confirmationService.confirm({
      message: '¿Eliminar esta observación?',
      header: 'Eliminar observación',
      acceptLabel: 'Eliminar',
      rejectLabel: 'Cancelar',
      accept: () => {
        this.api.deleteDeviceObservation(this.observationDevice!.id!, entry.id!).subscribe((device) => this.applyObservationDevice(device));
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
    this.currentPage = 1;
    this.reload();
  }

  sortByColumn(column: DeviceTableColumnKey): void {
    if (column === 'actions') return;
    if (this.sortBy === column) {
      this.sortDir = this.sortDir === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = column;
      this.sortDir = ['deviceType', 'brand', 'model', 'client'].includes(column) ? 'asc' : 'desc';
    }
    this.currentPage = 1;
    this.reload();
  }

  sortIcon(column: DeviceTableColumnKey): string {
    if (column === 'actions' || this.sortBy !== column) return 'pi pi-sort-alt';
    return this.sortDir === 'asc' ? 'pi pi-sort-amount-up-alt' : 'pi pi-sort-amount-down';
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

  activeObservations(device: Device): DeviceObservation[] {
    return (device.observations || []).filter((observation) => !observation.resolvedAt);
  }

  observationSummary(device: Device): string {
    const observations = this.activeObservations(device);
    if (!observations.length) return 'Sin pendientes';
    const first = observations[0]?.note || 'Observación pendiente';
    return observations.length === 1 ? first : `${observations.length} pendientes`;
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

  get visibleDevices(): (Device & { clientName?: string })[] {
    return this.devices;
  }

  get paginationLabel(): string {
    if (!this.totalElements) return '0 dispositivos';
    const start = (this.currentPage - 1) * this.pageSize + 1;
    const end = Math.min(start + this.devices.length - 1, this.totalElements);
    return `${start}-${end} de ${this.totalElements} dispositivos`;
  }

  get displayDetailHistoryTotalPages(): number {
    return Math.max(1, this.detailHistoryTotalPages);
  }

  get detailPaginationLabel(): string {
    if (!this.detailHistoryTotalElements) return '0 reparaciones';
    const start = this.detailHistoryPage * this.detailHistoryPageSize + 1;
    const end = Math.min(start + this.detailRepairs.length - 1, this.detailHistoryTotalElements);
    return `${start}-${end} de ${this.detailHistoryTotalElements} reparaciones`;
  }

  columnWidth(columnKey: DeviceTableColumnKey): string {
    return this.deviceColumns.find((column) => column.key === columnKey)?.width || 'auto';
  }

  startColumnResize(event: MouseEvent, columnKey: DeviceTableColumnKey): void {
    event.preventDefault();
    event.stopPropagation();
    const header = (event.currentTarget as HTMLElement).closest('th');
    if (!header) return;

    this.resizingColumnKey = columnKey;
    this.resizeStartX = event.clientX;
    this.resizeStartWidth = header.getBoundingClientRect().width;

    const onMouseMove = (moveEvent: MouseEvent) => {
      if (!this.resizingColumnKey) return;
      const nextWidth = Math.max(96, Math.round(this.resizeStartWidth + (moveEvent.clientX - this.resizeStartX)));
      const column = this.deviceColumns.find((item) => item.key === this.resizingColumnKey);
      if (column) {
        column.width = `${nextWidth}px`;
        this.persistColumnWidths();
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

  private reload(): void {
    this.pageRequest?.unsubscribe();
    this.pageRequest = this.api.getDevicePage(
      this.currentPage - 1,
      this.pageSize,
      this.searchTerm.trim(),
      this.selectedClientId || '',
      this.selectedClientId ? '' : this.selectedClientSearchText(),
      this.sortBy,
      this.sortDir
    ).subscribe((page) => {
      this.devices = page.content.map((device) => this.normalizeDevice(device));
      this.filteredDevices = this.devices;
      this.currentPage = page.page + 1;
      this.totalElements = page.totalElements;
      this.totalPages = Math.max(1, page.totalPages);
      this.changeDetector.detectChanges();
    });
  }

  private loadDetailHistory(): void {
    if (!this.detailDevice?.id) return;
    this.api.getDeviceRepairHistory(this.detailDevice.id, this.detailHistoryPage, this.detailHistoryPageSize).subscribe({
      next: (page) => {
        this.applyDetailHistoryPage(page);
        this.changeDetector.detectChanges();
      },
      error: () => this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudo cargar el historial de reparaciones.' })
    });
  }

  private applyDetailHistoryPage(page: { content: DeviceRepairHistoryItem[]; page: number; totalElements: number; totalPages: number }): void {
    this.detailRepairs = page.content;
    this.detailHistoryPage = page.page;
    this.detailHistoryTotalElements = page.totalElements;
    this.detailHistoryTotalPages = page.totalPages;
  }

  private normalizeDevice(device: Device): Device {
    return {
      ...device,
      currentPassword: device.currentPassword || '',
      observations: (device.observations || [])
        .slice()
        .sort((left, right) => this.historyTimestamp(right.observedAt) - this.historyTimestamp(left.observedAt)),
      passwordHistory: (device.passwordHistory || [])
        .slice()
        .sort((left, right) => this.historyTimestamp(right.createdAt) - this.historyTimestamp(left.createdAt))
    };
  }

  private historyTimestamp(value: string | undefined): number {
    return this.parseHistoryDate(value)?.getTime() || 0;
  }

  private restoreColumnWidths(): void {
    const stored = localStorage.getItem(this.columnWidthStorageKey);
    if (!stored) return;
    try {
      const widths = JSON.parse(stored) as Partial<Record<DeviceTableColumnKey, string>>;
      this.deviceColumns.forEach((column) => {
        if (widths[column.key]) column.width = widths[column.key]!;
      });
    } catch {
      localStorage.removeItem(this.columnWidthStorageKey);
    }
  }

  private persistColumnWidths(): void {
    localStorage.setItem(
      this.columnWidthStorageKey,
      JSON.stringify(Object.fromEntries(this.deviceColumns.map((column) => [column.key, column.width])))
    );
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

  private selectedClientSearchText(): string {
    const raw = typeof this.selectedClientTerm === 'string'
      ? this.selectedClientTerm
      : this.selectedClientTerm?.label || '';
    return raw.trim().toLowerCase();
  }

  private applyPasswordDevice(device: Device): void {
    const normalized = this.normalizeDevice(device);
    this.passwordDevice = normalized;
    this.syncDevice(normalized);
    if (this.editing.id && normalized.id === this.editing.id) {
      this.editing = { ...this.editing, currentPassword: normalized.currentPassword, passwordHistory: normalized.passwordHistory };
    }
    this.reload();
    this.changeDetector.detectChanges();
  }

  private applyObservationDevice(device: Device): void {
    const normalized = this.normalizeDevice(device);
    this.observationDevice = normalized;
    this.syncDevice(normalized);
    if (this.editing.id && normalized.id === this.editing.id) {
      this.editing = { ...this.editing, observations: normalized.observations };
    }
    this.reload();
    this.changeDetector.detectChanges();
  }

  private syncDevice(device: Device): void {
    const normalized = this.normalizeDevice(device);
    this.devices = this.devices.map((item) => item.id === normalized.id ? { ...item, ...normalized } : item);
  }
}
