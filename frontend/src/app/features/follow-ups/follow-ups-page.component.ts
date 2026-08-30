import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';
import { ConfirmationService, MessageService } from 'primeng/api';
import { AutoCompleteModule } from 'primeng/autocomplete';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { ApiService } from '../../core/services/api.service';
import { Client } from '../../shared/models/client.model';
import { CommitmentOutcome, FollowUpCommitment, FollowUpDetail, FollowUpListItem, FollowUpSave, FollowUpStatus } from '../../shared/models/follow-up.model';
import { phoneDigits } from '../../shared/utils/contact.util';

type ClientSuggestion = { label: string; client: Client };

@Component({
  selector: 'app-follow-ups-page',
  standalone: true,
  imports: [CommonModule, FormsModule, AutoCompleteModule, ButtonModule, CardModule, ConfirmDialogModule, DialogModule, InputTextModule, SelectModule],
  providers: [ConfirmationService],
  template: `
    <p-confirmdialog></p-confirmdialog>
    <section class="page-heading">
      <div><span class="eyebrow">Oportunidades</span><h1>Seguimientos</h1></div>
      <p>Registrá promesas de ingreso sin convertir a la persona en cliente hasta que corresponda.</p>
    </section>

    <div class="page-grid" [class.create-panel-hidden]="!showForm">
      @if (showForm) {
        <div class="create-panel-shell">
          <button class="icon-action form-toggle-button create-panel-toggle" type="button" title="Ocultar formulario" (click)="showForm = false"><i class="pi pi-eye-slash"></i></button>
          <p-card [header]="draft.id ? 'Editar seguimiento' : 'Nuevo seguimiento'">
            <form class="p-fluid" (ngSubmit)="save()">
              <div class="field">
                <label>Cliente existente (opcional)</label>
                <p-autoComplete [(ngModel)]="selectedClient" name="selectedClient" [suggestions]="clientSuggestions"
                  (completeMethod)="searchClients($event.query)" (onSelect)="selectClient($event.value)"
                  (onClear)="clearClient()" optionLabel="label" [minLength]="2" [forceSelection]="true"
                  [showClear]="true" placeholder="Buscar por nombre, teléfono o email"></p-autoComplete>
              </div>
              @if (!draft.clientId) {
                <div class="field"><label>Nombre o alias</label><input pInputText [(ngModel)]="draft.contactName" name="contactName" required /></div>
                <div class="field"><label>Canal</label><p-select [(ngModel)]="draft.contactChannel" name="contactChannel" [options]="channelOptions" optionLabel="label" optionValue="value" placeholder="Elegí un canal"></p-select></div>
                <div class="field"><label>Usuario, teléfono o contacto</label><input pInputText [(ngModel)]="draft.contactValue" name="contactValue" required /></div>
              }
              <div class="field"><label>Equipo</label><input pInputText [(ngModel)]="draft.deviceDescription" name="deviceDescription" placeholder="Notebook Lenovo IdeaPad" required /></div>
              <div class="field"><label>Problema comentado</label><textarea class="p-inputtext" rows="3" [(ngModel)]="draft.reportedProblem" name="reportedProblem"></textarea></div>
              @if (!draft.id) {
                <div class="field"><label>Fecha prometida (opcional)</label><input class="control" type="date" [(ngModel)]="draft.initialPromisedDate" name="initialPromisedDate" /></div>
                <div class="field"><label>Nota de la promesa</label><input pInputText [(ngModel)]="draft.initialPromiseNotes" name="initialPromiseNotes" placeholder="Ej.: dijo que pasa por la tarde" /></div>
              }
              <div class="field"><label>Estado</label><p-select [(ngModel)]="draft.status" name="status" [options]="statusOptions" optionLabel="label" optionValue="value"></p-select></div>
              <div class="field"><label>Notas internas</label><textarea class="p-inputtext" rows="3" [(ngModel)]="draft.notes" name="notes"></textarea></div>
              <button pButton type="submit" [label]="draft.id ? 'Guardar cambios' : 'Crear seguimiento'" icon="pi pi-check"></button>
              @if (draft.id) { <button class="secondary-button follow-up-cancel-edit" type="button" (click)="resetForm()">Cancelar edición</button> }
            </form>
          </p-card>
        </div>
      }

      <p-card header="Seguimientos activos e históricos">
        <div class="table-toolbar">
          <span class="p-input-icon-left filter-search"><i class="pi pi-search"></i><input pInputText [(ngModel)]="searchTerm" (ngModelChange)="onSearch($event)" placeholder="Buscar persona, contacto o equipo" /></span>
          @if (!showForm) { <button class="icon-action form-toggle-button" type="button" title="Mostrar formulario" (click)="showForm = true"><i class="pi pi-eye"></i></button> }
        </div>
        <div class="native-table-wrap">
          <table class="native-table follow-ups-table">
            <thead><tr><th>Persona</th><th>Equipo</th><th>Promesa actual</th><th>Historial</th><th>Estado</th><th>Acciones</th></tr></thead>
            <tbody>
              @for (item of items; track item.id) {
                <tr class="clickable-row" (click)="openDetail(item.id)">
                  <td><strong>{{ item.displayName }}</strong><small class="table-secondary">{{ item.clientId ? 'Cliente' : (item.contactChannel || 'Contacto') }}</small></td>
                  <td>{{ item.deviceDescription }}</td>
                  <td [class.follow-up-overdue]="isOverdue(item.currentPromisedDate)">{{ item.currentPromisedDate ? (item.currentPromisedDate | date:'dd/MM/yyyy') : '-' }}</td>
                  <td><span [class.follow-up-risk]="item.missedCommitmentCount >= 2">{{ item.commitmentCount }} promesa(s) · {{ item.missedCommitmentCount }} no concretada(s)</span></td>
                  <td><span class="status-pill" [ngClass]="statusClass(item.status)">{{ statusLabel(item.status) }}</span></td>
                  <td><div class="action-buttons">
                    @if (item.contactValue && isPhoneChannel(item.contactChannel)) { <a class="icon-action" [href]="whatsAppLink(item.contactValue)" target="_blank" rel="noopener" title="Contactar por WhatsApp" (click)="$event.stopPropagation()"><i class="pi pi-whatsapp"></i></a> }
                    <button class="icon-action" type="button" title="Editar" (click)="$event.stopPropagation(); edit(item.id)"><i class="pi pi-pencil"></i></button>
                    <button class="icon-action danger" type="button" title="Eliminar" (click)="$event.stopPropagation(); confirmDelete(item)"><i class="pi pi-trash"></i></button>
                  </div></td>
                </tr>
              } @empty { <tr><td class="empty-cell" colspan="6">No hay seguimientos para mostrar.</td></tr> }
            </tbody>
          </table>
        </div>
        <div class="table-pager"><span>{{ paginationLabel }}</span><div class="pager-actions">
          <button class="pager-button" type="button" [disabled]="page === 0" (click)="previousPage()"><i class="pi pi-chevron-left"></i></button>
          <span>Página {{ page + 1 }} de {{ displayTotalPages }}</span>
          <button class="pager-button" type="button" [disabled]="page + 1 >= displayTotalPages" (click)="nextPage()"><i class="pi pi-chevron-right"></i></button>
        </div></div>
      </p-card>
    </div>

    <p-dialog header="Seguimiento y promesas" [(visible)]="detailVisible" [modal]="true" [style]="{width:'58rem', maxWidth:'95vw'}">
      @if (detail) {
        <div class="detail-grid">
          <div class="detail-item"><label>Persona</label><strong>{{ detail.clientName || detail.contactName }}</strong></div>
          <div class="detail-item"><label>Tipo</label><strong>{{ detail.clientId ? 'Cliente existente' : 'Contacto potencial' }}</strong></div>
          <div class="detail-item"><label>Equipo</label><strong>{{ detail.deviceDescription }}</strong></div>
          @if (detail.reportedProblem) { <div class="detail-item detail-wide"><label>Problema comentado</label><div>{{ detail.reportedProblem }}</div></div> }
        </div>
        <div class="follow-up-commitment-form">
          <div class="field"><label>Nueva fecha prometida</label><input class="control" type="date" [(ngModel)]="newCommitment.promisedDate" /></div>
          <div class="field"><label>Nota</label><input pInputText [(ngModel)]="newCommitment.notes" placeholder="Ej.: dijo que pasa por la tarde" /></div>
          <button pButton type="button" label="Registrar promesa" icon="pi pi-plus" (click)="addCommitment()"></button>
        </div>
        <div class="native-table-wrap"><table class="native-table compact-native-table">
          <thead><tr><th>Registrada</th><th>Fecha prometida</th><th>Resultado</th><th>Nota</th><th>Acción</th></tr></thead>
          <tbody>
            @for (commitment of detail.commitments; track commitment.id) {
              <tr><td>{{ commitment.createdAt | date:'dd/MM/yyyy HH:mm' }}</td><td>{{ commitment.promisedDate | date:'dd/MM/yyyy' }}</td><td>{{ outcomeLabel(commitment.outcome) }}</td><td>{{ commitment.notes || '-' }}</td><td>
                @if (commitment.outcome === 'PENDING') {
                  <button class="icon-action" type="button" title="Marcar como concretada" (click)="setOutcome(commitment, 'COMPLETED')"><i class="pi pi-check"></i></button>
                  <button class="icon-action danger" type="button" title="Marcar como no concretada" (click)="setOutcome(commitment, 'NOT_COMPLETED')"><i class="pi pi-times"></i></button>
                }
              </td></tr>
            } @empty { <tr><td class="empty-cell" colspan="5">Todavía no hay promesas registradas.</td></tr> }
          </tbody>
        </table></div>
      }
    </p-dialog>
  `
})
export class FollowUpsPageComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();
  private readonly search$ = new Subject<string>();
  items: FollowUpListItem[] = [];
  draft: FollowUpSave = this.emptyDraft();
  detail: FollowUpDetail | null = null;
  newCommitment: FollowUpCommitment = { promisedDate: '', notes: '' };
  clientSuggestions: ClientSuggestion[] = [];
  selectedClient: ClientSuggestion | null = null;
  showForm = true;
  detailVisible = false;
  searchTerm = '';
  page = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;
  readonly channelOptions = [
    { label: 'WhatsApp', value: 'WHATSAPP' }, { label: 'Instagram', value: 'INSTAGRAM' },
    { label: 'Facebook', value: 'FACEBOOK' }, { label: 'Teléfono', value: 'PHONE' }, { label: 'Otro', value: 'OTHER' }
  ];
  readonly statusOptions = [
    { label: 'Pendiente de confirmar', value: 'PENDING' }, { label: 'Confirmó fecha', value: 'CONFIRMED' },
    { label: 'No responde', value: 'NO_RESPONSE' }, { label: 'Cancelado', value: 'CANCELLED' }, { label: 'Concretado', value: 'COMPLETED' }
  ];

  constructor(private readonly api: ApiService, private readonly messages: MessageService, private readonly confirmations: ConfirmationService) {}

  ngOnInit(): void {
    this.search$.pipe(debounceTime(300), distinctUntilChanged(), takeUntil(this.destroy$)).subscribe(() => { this.page = 0; this.reload(); });
    this.reload();
  }
  ngOnDestroy(): void { this.destroy$.next(); this.destroy$.complete(); }

  save(): void {
    this.api.saveFollowUp(this.draft).subscribe({
      next: () => { this.messages.add({ severity: 'success', summary: 'Seguimiento guardado', detail: 'Los datos quedaron registrados.' }); this.resetForm(); this.reload(); },
      error: () => this.messages.add({ severity: 'error', summary: 'Error', detail: 'No se pudo guardar el seguimiento.' })
    });
  }
  edit(id: string): void {
    this.api.getFollowUpById(id).subscribe((detail) => {
      this.draft = { ...detail };
      this.selectedClient = detail.clientId ? { label: detail.clientName || 'Cliente', client: { id: detail.clientId, name: detail.clientName || '', lastName: '', reference: '', email: '', phone: '' } } : null;
      this.showForm = true;
    });
  }
  openDetail(id: string): void { this.loadDetail(id, true); }
  addCommitment(): void {
    if (!this.detail || !this.newCommitment.promisedDate) return;
    this.api.addFollowUpCommitment(this.detail.id, this.newCommitment).subscribe(() => {
      this.newCommitment = { promisedDate: '', notes: '' };
      this.loadDetail(this.detail!.id, false); this.reload();
    });
  }
  setOutcome(commitment: FollowUpCommitment, outcome: CommitmentOutcome): void {
    if (!this.detail || !commitment.id) return;
    this.api.updateFollowUpCommitmentOutcome(this.detail.id, commitment.id, outcome).subscribe(() => { this.loadDetail(this.detail!.id, false); this.reload(); });
  }
  searchClients(term: string): void {
    this.api.searchClients(term, 20).subscribe((clients) => {
      this.clientSuggestions = clients.map((client) => ({ client, label: `${client.name} ${client.lastName}`.trim() }));
    });
  }
  selectClient(suggestion: ClientSuggestion): void { this.draft.clientId = suggestion.client.id; }
  clearClient(): void { this.selectedClient = null; this.draft.clientId = null; }
  confirmDelete(item: FollowUpListItem): void {
    this.confirmations.confirm({ message: `¿Eliminar el seguimiento de ${item.displayName}?`, header: 'Confirmar eliminación', acceptLabel: 'Eliminar', rejectLabel: 'Cancelar', accept: () => this.delete(item.id) });
  }
  delete(id: string): void { this.api.deleteFollowUp(id).subscribe(() => { this.messages.add({ severity: 'success', summary: 'Seguimiento eliminado' }); this.reload(); }); }
  onSearch(term: string): void { this.search$.next(term.trim()); }
  previousPage(): void { if (this.page > 0) { this.page--; this.reload(); } }
  nextPage(): void { if (this.page + 1 < this.totalPages) { this.page++; this.reload(); } }
  resetForm(): void { this.draft = this.emptyDraft(); this.selectedClient = null; }
  whatsAppLink(contact: string): string { return `https://wa.me/${phoneDigits(contact)}`; }
  isPhoneChannel(channel?: string): boolean { return channel === 'WHATSAPP' || channel === 'PHONE'; }
  isOverdue(date?: string): boolean { return !!date && date < this.today() && !!date; }
  statusLabel(status: FollowUpStatus): string { return this.statusOptions.find((item) => item.value === status)?.label || status; }
  statusClass(status: FollowUpStatus): string { return status === 'COMPLETED' ? 'is-success' : status === 'CANCELLED' || status === 'NO_RESPONSE' ? 'is-closed' : status === 'CONFIRMED' ? 'is-info' : 'is-warning'; }
  outcomeLabel(outcome?: CommitmentOutcome): string { return ({ PENDING: 'Pendiente', RESCHEDULED: 'Reprogramada', NOT_COMPLETED: 'No concretada', COMPLETED: 'Concretada' } as Record<string, string>)[outcome || 'PENDING']; }
  get displayTotalPages(): number { return Math.max(1, this.totalPages); }
  get paginationLabel(): string { if (!this.totalElements) return '0 seguimientos'; const start = this.page * this.pageSize + 1; return `${start}-${Math.min(start + this.items.length - 1, this.totalElements)} de ${this.totalElements} seguimientos`; }

  private reload(): void {
    this.api.getFollowUpPage(this.page, this.pageSize, this.searchTerm.trim()).subscribe((result) => {
      this.items = result.content; this.page = result.page; this.totalElements = result.totalElements; this.totalPages = result.totalPages;
    });
  }
  private loadDetail(id: string, open: boolean): void {
    this.api.getFollowUpById(id).subscribe((detail) => { this.detail = detail; if (open) this.detailVisible = true; });
  }
  private emptyDraft(): FollowUpSave { return { clientId: null, contactName: '', contactChannel: 'WHATSAPP', contactValue: '', deviceDescription: '', reportedProblem: '', status: 'PENDING', notes: '', initialPromisedDate: null, initialPromiseNotes: '' }; }
  private today(): string {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
